# DoneBot

An offline-first Android task manager with built-in pomodoro timer and
on-time, draw-over-other-apps reminder overlays. Works fully without an
account; signs in to a Spring Kotlin backend to sync across devices.

---

## At a glance

- **Offline-first.** Every write goes to Room first; remote calls are best-effort. Guest users accumulate pending rows that drain on sign-in.
- **Background-resilient reminders.** AlarmManager wakes the device at the exact scheduled time, then either shows a system-alert overlay or posts a heads-up notification (depending on the user's overlay permission).
- **Pomodoro with a persistent banner.** A countdown engine lives at the application scope; a top-bar banner mirrors its state across every screen except the Pomodoro screen itself.
- **Reactive UI throughout.** Compose + StateFlow, MVI per feature, one `onAction(UiAction)` dispatcher per ViewModel.
- **Hilt + WorkManager-driven sync.** A `SyncWorker` pushes pending mutations and a `FetchTasksWorker` pulls server state; both are serialized through a shared mutex inside the repository.
- **Two-NavHost architecture.** Cold-start swaps between `AuthNavHost` and `MainNavHost` based on a Splash-resolved auth state, so guests and authenticated users get clean back-stacks with no auth screens leaking into the main app.

---

## Tech stack

| Layer | Library |
|---|---|
| Language | Kotlin 2.3.x, JVM target 17 |
| UI | Jetpack Compose (BOM 2026.02), Material 3 |
| Navigation | **Navigation 3** (`androidx.navigation3` — not the legacy Navigation Compose) |
| DI | Hilt 2.60 + `androidx-hilt-work` |
| Local storage | Room 2.8.4, DataStore Preferences |
| Network | Retrofit 3.x + kotlinx-serialization JSON, OkHttp 5 |
| Background work | WorkManager 2.11 |
| Reminders | AlarmManager (`USE_EXACT_ALARM`), foreground services |
| Build | AGP 9.3 alpha, Gradle 9.5, KSP 2.3.9 |
| Code quality | ktlint 14, detekt 1.23 |

Min SDK 26 (Android 8.0), target SDK 37. Core library desugaring enabled so `java.time` works everywhere.

---

## Architecture

### Modules

```
DoneBot/
├── app/           # The application
└── uikit/         # Design-system module (TDText, TDPreview, themes, components)
```

`uikit` is consumed by `app`; the inverse is forbidden by the Gradle dependency graph.

### Layered structure inside `app/`

```
data/         # Implementations + integrations
  alarm/      #   AlarmManager scheduling, BroadcastReceiver fire-out
  engine/     #   PomodoroEngineImpl (in-process timer)
  mapper/     #   entity ↔ domain ↔ DTO conversions
  model/      #   Room entities, network DTOs (request/response/data)
  notification/  Notification channels + service
  overlay/    #   System-alert window service + channel
  repository/ #   *RepositoryImpl
  source/     #   local (Room DAO, AppDatabase) + remote (Retrofit, interceptor, authenticator)
  sync/       #   SyncWorker, FetchTasksWorker, +UseCase impls

domain/       # Pure Kotlin — no Android, no Room, no Retrofit
  engine/     #   PomodoroEngine interface
  model/      #   Task, User, AuthSession (the domain types)
  repository/ #   Interfaces only
  usecase/    #   Reusable orchestration: ScheduleTaskAlarmUseCase, FetchTasksUseCase, etc.

ui/           # MVI screens (one folder per feature)
  home/       #   3 MVI files + a components/ subfolder for the screen's helpers
  pomodoro/
    banner/   #   Top-bar banner (its own MVI)
    launch/   #   Pre-start configuration screen
    edit/     #   Settings editor
    components/ #  Shared composables (ring, dots, controls, mode theme)
    ...       #   The running-timer screen sits at this root
  …           #   addtask, details, login, register, onboarding, profile, settings, splash, theme

navigation/   # AppKey routes + NavigationEffect + per-NavHost subfolders
  auth/       #   AuthNavHost + AuthNavigator
  main/       #   MainNavHost + MainNavigator + BottomBar + BottomSheetSceneStrategy + NavigationState

common/       # handleRequest, handleLocal, DomainException, RingtoneHolder, PermissionPrompts
di/           # Hilt modules — DatabaseModule, LocalStorageModule, NetworkModule, DispatcherModule, RepositoryModule
```

Domain has zero Android imports. Data implements domain interfaces and is the only layer that knows about Room, Retrofit, AlarmManager, etc. UI talks to domain via use cases and repositories — never directly to data.

### MVI per feature

Every screen follows the same shape:

```kotlin
object FooContract {
    @Immutable data class UiState(/* … */)
    sealed interface UiAction { /* OnXTap, OnYChange, … */ }
    sealed interface UiEffect { /* ShowToast, ShowError, … */ }
}

@HiltViewModel
class FooViewModel @Inject constructor(...) : ViewModel() {
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _uiEffect = Channel<UiEffect>(Channel.BUFFERED)
    val uiEffect = _uiEffect.receiveAsFlow()

    private val _navEffect = Channel<NavigationEffect>(Channel.BUFFERED)
    val navEffect = _navEffect.receiveAsFlow()

    fun onAction(action: UiAction) { when (action) { /* … */ } }

    private fun emitEffect(effect: UiEffect) = viewModelScope.launch { _uiEffect.send(effect) }
    private fun emitNav(effect: NavigationEffect) = viewModelScope.launch { _navEffect.send(effect) }
}

@Composable
fun FooScreen(uiState: UiState, uiEffect: Flow<UiEffect>, onAction: (UiAction) -> Unit) { … }
```

Rules:

- One public `onAction(UiAction)` per ViewModel — never per-event public methods.
- `NavigationEffect` is the project-wide enum (`Navigate(key)`, `ReplaceCurrent(key)`, `GoBack`); wired through `NavigationEffectController` inside the NavHost entry.
- StateFlow writes always go through `.update { }`, never `.value =`.
- Channel sends always go through `emitNav` / `emitEffect` helpers, never `.trySend()` (which can silently fail).
- `delay()` always takes a typed `Duration` (`delay(1500.milliseconds)`), never a raw `Long`.

### DI map

| Module | Provides |
|---|---|
| `DatabaseModule` | `AppDatabase`, `TaskDao` |
| `LocalStorageModule` | `DataStore<Preferences>` |
| `NetworkModule` | `OkHttpClient`, two `Retrofit` instances (default + `@Named("token")` for auth), `DoneBotApi`, `DoneBotAuthApi`, a mutex used by the token-refresh authenticator |
| `DispatcherModule` | `@IoDispatcher`, `@MainDispatcher`, `@DefaultDispatcher` dispatchers, `@ApplicationScope CoroutineScope`, `@SyncMutex Mutex` |
| `RepositoryModule` | Binds every `*RepositoryImpl` to its interface, plus the sync use cases |

The two-Retrofit split exists to break a Hilt DI cycle: `TokenRefreshAuthenticator` depends on `AuthRepository`, which in turn would depend on `DoneBotApi` (which carries the authenticator)…  Routing auth endpoints through `DoneBotAuthApi` — built on a second `OkHttpClient` with no interceptor and no authenticator — breaks the cycle and also keeps wrong-password 401s from triggering a wasteful refresh attempt.

---

## Reminders, overlays, and notifications

The most distinctive bit. A task with a `taskTime` and a user-configured "remind me" lead in `ReminderSettingsRepository` becomes a scheduled `AlarmManager` wake-up that fires regardless of Doze or app state.

### Permissions

| Permission | Purpose |
|---|---|
| `USE_EXACT_ALARM` | Schedule `setExactAndAllowWhileIdle` reminders (install-time on API 33+ for reminder apps). |
| `SYSTEM_ALERT_WINDOW` | Show the overlay card on top of other apps. User-revocable; requested via `Settings.canDrawOverlays(context)` flow. |
| `POST_NOTIFICATIONS` | Heads-up notification on API 33+. User-revocable. |
| `FOREGROUND_SERVICE` + `FOREGROUND_SERVICE_SPECIAL_USE` | Required to run the overlay and notification services as foreground. |

The overlay is the preferred surface; if the user denies overlay permission at fire time, the alarm receiver re-routes to a notification instead.

### Scheduling

```
AddTaskScreen → AddTaskViewModel.onSave()
  → AddTaskUseCase (creates Task in domain)
    → TaskRepositoryImpl.addTask() (writes to Room, fires remote push)
    → ScheduleTaskAlarmUseCase
        ↓
        AlarmScheduler.scheduleForTask(AlarmItem)
        ↓
        AlarmManager.setExactAndAllowWhileIdle(RTC_WAKEUP, fireAt, pendingIntent)
```

The `PendingIntent` carries an `EXTRA_FIRE_TARGET` so the receiver can later distinguish "show overlay" vs "post notification" routing decisions.

### Fire-out

```
[AlarmManager fires at exact time]
  ↓
AlarmFireReceiver.onReceive()
  ↓
[checks Settings.canDrawOverlays() in case user revoked overlay perm]
  ↓
  if overlay allowed → startForegroundService(OverlayService)
  else                → startForegroundService(NotificationService)
```

The re-check at fire time matters because the user can revoke overlay permission between scheduling and firing. Routing here keeps the overlay service from being started without permission (which would crash inside `windowManager.addView`).

### OverlayService

A `Service` + `LifecycleOwner` + `SavedStateRegistryOwner` that:

1. Calls `startForeground(...)` within Android's 5-second contract with a silent placeholder notification.
2. Inflates a `ComposeView` with a `TDOverlayNotificationCard` using `TYPE_APPLICATION_OVERLAY` layout params.
3. Adds the view via `WindowManager.addView`.
4. Plays a ringtone via `RingtoneHolder` (respects the device ringer mode — silent mode suppresses the sound).
5. On dismiss/tap, animates out, removes the view, stops the foreground state, and `stopSelf()`.

It implements `LifecycleOwner` and `SavedStateRegistryOwner` itself so Compose can drive remember/saved-state inside the overlay (a Service doesn't get either by default).

### NotificationService

A simpler foreground service that posts a high-importance reminder notification on the `ReminderChannel` and stops itself. Used as the fallback path when overlay permission is missing.

### Reschedule on resume

`AlarmManager` doesn't persist alarms across reboots or app process death. To paper over this without a `BOOT_COMPLETED` receiver, `RescheduleAllAlarmsUseCase` runs from `Application.onStart` (via `ProcessLifecycleOwner`) every time the app comes to the foreground — it iterates Room's tasks and re-arms an alarm for every future, incomplete one. So a freshly-rebooted device gets its alarms back the moment the user opens the app. Adding a boot receiver would close the "user reboots and never opens the app" gap if that becomes a real complaint.

---

## Offline-first sync engine

### The model

Every `TaskEntity` carries a `SyncStatus` enum:

| Status | Meaning |
|---|---|
| `SYNCED` | Local copy matches server. |
| `PENDING_CREATE` | Local insert that hasn't been POSTed yet. |
| `PENDING_UPDATE` | Local edit that hasn't been PUT yet. |
| `PENDING_DELETE` | Local delete-marker not yet DELETEd remotely. |

`TaskDao.getAllTasks()` filters out `PENDING_DELETE` rows so the UI doesn't show them.

### The push

`SyncWorker` → `TaskRepository.syncLocalTasksToServer()`:

```
for entity in localDataSource.findPending():
    match entity.syncStatus:
        PENDING_CREATE → POST /tasks      → on success: update row to SYNCED with remoteId
        PENDING_UPDATE → PUT  /tasks      → on success: update row to SYNCED
        PENDING_DELETE → DELETE /tasks/id → on success: remove row from Room
    if Unauthorized → bail the whole batch (same token, same problem)
    if NotFound on update → demote to PENDING_CREATE (edit-beats-delete)
    if NotFound on delete → prune locally (server already removed it)
```

Everything runs inside a `syncMutex.withLock { }` so push and pull never race.

### The pull

`FetchTasksWorker` → `TaskRepository.syncRemoteTasksWithLocal()`:

```
list = GET /tasks
for remote in list:
    when local row for remote.id is:
      missing            → insert as SYNCED
      SYNCED             → overwrite (server wins)
      any PENDING_*      → skip (local wins until pushed)
delete every SYNCED local row whose remoteId isn't in list  (orphans, server-side deletions)
```

### Triggers

| When | What runs |
|---|---|
| Login / Register / Token refresh | `SyncPendingTasksUseCase` (push only) |
| App foreground (`ProcessLifecycleOwner.ON_START`) | `FetchTasksUseCase` (push.then(pull)) |
| Pull-to-refresh on Home | `FetchTasksUseCase(force = true)` |
| Add/update/delete task | Inline fire-and-forget remote on `@ApplicationScope` |

`FetchTasksUseCase` has a 60s cooldown to debounce rapid foreground/pull-to-refresh bursts unless `force = true`.

### Why `@ApplicationScope` for inline pushes

`TaskRepositoryImpl.firePushCreate` launches the remote POST on a `@SupervisorJob`-rooted scope tied to the Application lifetime — not the caller's `viewModelScope`. If the user navigates away mid-POST, the request still completes. The local row stays in `PENDING_CREATE` either way; the sync worker would retry, but using `@ApplicationScope` avoids that round-trip.

---

## Authentication

A guest can use the entire app. Sign-in is gradient-coloring on top: it lets the same data survive a device wipe and sync across devices.

### Token persistence

`AuthSessionRepository` over DataStore Preferences (`donebot_prefs`):

- `access_token` (Bearer for API calls)
- `refresh_token` (used to mint a new access token)
- `expires_at` — stored as an **absolute timestamp in ms** (`now + session.expiresIn * 1000`), not a relative duration

### Request flow

```
LoginScreen → LoginViewModel.tryLogin()
  → AuthRepository.login(email, password)            (DoneBotAuthApi — no interceptor)
  → AuthSessionRepository.saveSession(session)        (writes prefs)
  → FetchTasksUseCase(force = true)                   (pull this user's tasks)
  → emitNav(NavigationEffect.Navigate(Home))
```

### Interceptor + authenticator

- `AuthInterceptor` attaches `Authorization: Bearer <token>` to every request except `/auth/login`, `/auth/register`, `/auth/refresh`.
- `TokenRefreshAuthenticator` runs on 401: mutex-guarded refresh through `DoneBotAuthApi` (the no-interceptor, no-authenticator Retrofit) to avoid recursion. On `Unauthorized` or `NotFound` (the refresh endpoint itself is broken) it clears prefs — triggering `SplashViewModel`'s reactive observer to flip the top-level state to `NeedsAuth`, which mounts `AuthNavHost`.

### Splash → routing decision

`SplashViewModel.resolve()`:

| Condition | Result |
|---|---|
| No refresh token | `NeedsAuth()` (clear any stray access token) |
| Access token still valid (`expiresAt > now`) | `EnterApp` |
| Otherwise | Try `authRepository.refresh()`. Success → save + `EnterApp`. 401 → clear + `NeedsAuth()`. Other failure → `EnterApp` (lenient for offline) |

A reactive observer on the refresh token also flips state to `NeedsAuth` if anything elsewhere (the authenticator, an explicit logout) clears it mid-session.

---

## Navigation

Built on `androidx.navigation3` (not the legacy Compose Navigation). The cold-start sequence:

```
MainActivity.AppRoot()
  ├─ Splash → SplashViewModel.uiState
  │   ├─ Resolving        → SplashScreen
  │   ├─ EnterApp         → MainNavHost
  │   └─ NeedsAuth(startAt, cancelable)
  │                       → AuthNavHost(startAt, onCancel)
```

`AuthNavHost` runs Onboarding → Login → Register. `MainNavHost` runs Home + Profile tabs with per-tab back stacks, plus push entries for AddTask (bottom sheet), Details, Pomodoro, PomodoroLaunch, AddPomodoroTimer, Settings.

The Pomodoro banner is mounted as the Scaffold's `topBar` — visible across every main-app screen except the running Pomodoro screen itself, fading in/out based on whether the engine has an active session.

Profile's "Sign in" and "Sign up" bubble up to the activity as `onRequestAuth`, which re-mounts `AuthNavHost` with `cancelable = true`, so a guest can poke at auth and back out without losing their place.

---

## Pomodoro

A `PomodoroEngine` singleton (`@Singleton`-bound `PomodoroEngineImpl`) holds the timer state machine, driven by a command channel (`Channel.UNLIMITED`) for non-suspending public API. It exposes:

```kotlin
interface PomodoroEngine {
    val state: StateFlow<PomodoroEngineState>
    val events: SharedFlow<PomodoroEvent>

    fun setSessionQueue(sessions: List<Session>)
    fun prepare(); fun start(); fun pause(); fun skip(); fun finish(); fun reset()
}
```

Four UI surfaces consume the engine:

| Screen | Role |
|---|---|
| `pomodoro/launch/` | Pre-start configuration. Starting pops itself and navigates to the running screen. |
| `pomodoro/edit/` | Edit saved settings in DataStore. |
| `pomodoro/` (root) | The running timer — ring, mode, controls, session dots. |
| `pomodoro/banner/` | The top-bar mini-display, visible on every other screen while a session is active. |

`PomodoroSettingsRepository.getSettings()` returns a `Flow<Pomodoro?>` so the launch screen reflects edits made on the editor screen as soon as the user returns.

---

## Building

```bash
./gradlew :app:assembleDebug              # full build
./gradlew :app:compileDebugKotlin          # Kotlin + KSP only, faster
./gradlew :app:lintDebug                   # Android Lint
```

Notes:

- AGP 9 alpha is the deliberate version; `optimization { enable = false }` in the release build type is intentional. Don't bump AGP casually.
- Hilt 2.60 generated Java needs `compileOnly(libs.errorprone.annotations)` because it references `com.google.errorprone.annotations.CanIgnoreReturnValue`. Without it, `hiltJavaCompileDebug` fails.
- WorkManager + Hilt requires the manifest to strip `androidx.work.WorkManagerInitializer` from `androidx.startup` so the custom `HiltWorkerFactory` actually takes effect. The override is already in `AndroidManifest.xml`.

### Inspecting on device

```bash
# Local DB
adb shell run-as com.utakatalp.donebot sqlite3 /data/data/com.utakatalp.donebot/databases/donebot_debug.db

# WorkManager state
adb shell dumpsys jobscheduler | grep -i donebot
adb logcat -s WM-WorkerWrapper
```

---

## Backend

The API surface lives at `https://api.candroid.dev/todos/` and is a Spring/Kotlin service. It's mounted under `/todos/` (nginx) with Spring `context-path: /todos`, so Retrofit relative paths like `tasks` resolve to `https://api.candroid.dev/todos/tasks`.

| Endpoint | Method | Carries Bearer? |
|---|---|---|
| `/auth/login` | POST | no (auth client) |
| `/auth/register` | POST | no (auth client) |
| `/auth/refresh` | POST | no (auth client) |
| `/tasks` | GET | yes |
| `/tasks` | POST | yes |
| `/tasks` | PUT | yes |
| `/tasks/complete` | PATCH | yes |
| `/tasks/{id}` | DELETE | yes |

All endpoints return `BaseResponse<T?>` with `code: Int`, `message: String`, `data: T?`. The shared `handleRequest { … }` helper in `common/Extensions.kt` normalizes errors into `DomainException` (`NoInternet`, `Unauthorized`, `NotFound`, `Server`, `Unknown`) and unwraps `BaseResponse.data`.

---

## Conventions worth knowing

- **No emojis in code.** No multi-paragraph KDoc.
- **`TaskEntity.date`** is stored as `LocalDate.toEpochDay()` (days since epoch). `timeStart`/`timeEnd` are minutes-of-day. Backend takes these `Long`s opaquely.
- **`PENDING_DELETE` rows are hidden from the UI.** DAO queries filter them.
- **No client-side dedup for `PENDING_CREATE`.** A partially-succeeded push retried later can create a server-side duplicate. Accepted trade-off.
- **Stale-pending while online is by design.** Pending rows only drain on auth events (login / register / refresh) — not on every foreground resume. Foreground only triggers the pull.
- **MainActivity and DoneBotApplication live in the project root package**, not under `ui/`.

---

## Project layout (top level)

```
.
├── app/                                   # The application module
│   └── src/main/java/com/utakatalp/donebot/
│       ├── MainActivity.kt                # AppRoot composable
│       ├── Application.kt                 # DoneBotApplication (HiltAndroidApp)
│       ├── common/                        # handleRequest, RingtoneHolder, Permissions, etc.
│       ├── data/                          # impls — see Architecture above
│       ├── di/                            # Hilt modules
│       ├── domain/                        # interfaces, models, use cases
│       ├── navigation/                    # AppKey + auth/ + main/ + NavigationEffect
│       └── ui/                            # screens, one folder per feature
├── uikit/                                 # Design-system module (TDText, TDPreview, themes)
├── gradle/libs.versions.toml              # Single source of truth for versions
└── CLAUDE.md                              # Agent/maintainer-focused architecture notes
```
