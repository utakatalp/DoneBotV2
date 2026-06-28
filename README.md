# DoneBot

An offline-first Android task manager with a built-in pomodoro timer and on-time, draw-over-other-apps reminder overlays. Works fully without an account; signs in to a Spring Kotlin backend to sync across devices.

---

## At a glance

- **Offline-first.** Every write goes to Room first; remote calls are best-effort. Guest pending rows drain on sign-in.
- **Background-resilient reminders.** `AlarmManager` wakes the device at the exact time and either shows a system-alert overlay or posts a heads-up notification.
- **Pomodoro with a persistent banner.** A countdown engine lives at the application scope; a top-bar banner mirrors its state on every other screen.
- **Reactive UI throughout.** Compose + StateFlow, MVI per feature, one `onAction(UiAction)` per ViewModel.
- **Hilt + WorkManager sync.** `SyncWorker` pushes pending mutations, `FetchTasksWorker` pulls; both serialized through a shared repository mutex.
- **Two-NavHost architecture.** Cold-start swaps between `AuthNavHost` and `MainNavHost` based on Splash-resolved auth state — no auth screens leak into the main app.

---

## UI tests (Maestro)

Flows live in `.maestro/`:

- `smoke_guest.yaml` — onboarding → guest → Home
- `add_task.yaml` — create a task and assert it lands on Home
- `register_and_login.yaml` — register a fresh account, then log back in

Run all flows against a running emulator/device with the debug APK installed:

```bash
maestro test .maestro/
```

Recordings: [`smoke_guest.mp4`](.maestro/recordings/smoke_guest.mp4) · [`add_task.mp4`](.maestro/recordings/add_task.mp4) · [`register_and_login.mp4`](.maestro/recordings/register_and_login.mp4)

https://github.com/user-attachments/assets/be946e59-8eb3-4671-853d-a93642146974

https://github.com/user-attachments/assets/14ffe6bd-fef8-4d87-aa57-9d58a6e3f653

https://github.com/user-attachments/assets/0704e131-b571-4de6-8708-a1bbf84dc084

---

## Tech stack

| Layer | Library |
|---|---|
| Language | Kotlin 2.3.x, JVM target 17 |
| UI | Jetpack Compose (BOM 2026.02), Material 3 |
| Navigation | **Navigation 3** (`androidx.navigation3` — not legacy Navigation Compose) |
| DI | Hilt 2.60 + `androidx-hilt-work` |
| Local storage | Room 2.8.4, DataStore Preferences |
| Network | Retrofit 3.x + kotlinx-serialization, OkHttp 5 |
| Background work | WorkManager 2.11 |
| Reminders | AlarmManager (`USE_EXACT_ALARM`), foreground services |
| Build | AGP 9.3 alpha, Gradle 9.5, KSP 2.3.9 |
| Code quality | ktlint 14, detekt 1.23 |

Min SDK 26, target SDK 37. Core library desugaring enabled.

---

## Architecture

Two Gradle modules:

```
DoneBot/
├── app/      # The application
└── uikit/    # Design-system module (TDText, TDPreview, themes)
```

Inside `app/`: a `data` / `domain` / `ui` split with `navigation`, `di`, and `common` alongside. Domain has zero Android imports — it's pure Kotlin interfaces and models. Data implements those interfaces and is the only layer that knows about Room, Retrofit, or AlarmManager. UI talks to domain via use cases and repositories, never directly to data.

**MVI per feature.** Every screen has a `FooContract` (UiState + sealed UiAction + sealed UiEffect), a `FooViewModel` with a single public `onAction(UiAction)`, and a `FooScreen` composable. `NavigationEffect` is a project-wide enum routed through `NavigationEffectController`. StateFlow writes go through `.update { }`; channel sends go through `emitNav` / `emitEffect` helpers; `delay()` always takes a typed `Duration`.

Full architecture walkthrough — module tree, MVI rules, DI map, the two-Retrofit split — lives in [`CLAUDE.md`](CLAUDE.md).

---

## Reminders, overlays, and notifications

A task with a `taskTime` plus a "remind me" lead becomes a `setExactAndAllowWhileIdle` alarm that fires regardless of Doze. The receiver re-checks `Settings.canDrawOverlays()` at fire time and routes to either `OverlayService` (system-alert window with a Compose `TDOverlayNotificationCard` + ringtone via `RingtoneHolder`) or `NotificationService` (heads-up fallback when overlay permission was revoked).

`AlarmManager` doesn't persist across reboots, so `RescheduleAllAlarmsUseCase` runs from `ProcessLifecycleOwner.ON_START` every foreground — it walks Room and re-arms every future, incomplete task.

Permissions used: `USE_EXACT_ALARM`, `SYSTEM_ALERT_WINDOW`, `POST_NOTIFICATIONS`, `FOREGROUND_SERVICE` + `FOREGROUND_SERVICE_SPECIAL_USE`.

---

## Offline-first sync engine

Every `TaskEntity` carries a `SyncStatus`:

| Status | Meaning |
|---|---|
| `SYNCED` | Local matches server. |
| `PENDING_CREATE` | Local insert not yet POSTed. |
| `PENDING_UPDATE` | Local edit not yet PUT. |
| `PENDING_DELETE` | Delete-marker not yet DELETEd (hidden from the UI). |

`SyncWorker` drains pending rows; `FetchTasksWorker` pulls (server wins for `SYNCED`, local wins for any `PENDING_*`). Both run inside `syncMutex.withLock { }`. Push triggers: login / register / token refresh. Pull triggers: app foreground + pull-to-refresh, with a 60s cooldown unless forced. Inline writes also fire a best-effort remote on `@ApplicationScope` so navigating away mid-POST doesn't kill the request.

---

## Authentication

A guest can use the entire app. Sign-in lets the same data survive a wipe and sync across devices.

`AuthSessionRepository` persists `access_token`, `refresh_token`, and `expires_at` (absolute ms) in DataStore. `AuthInterceptor` attaches the bearer; `TokenRefreshAuthenticator` runs on 401, mutex-guarded, refreshing via a **second Retrofit** (`DoneBotAuthApi`, no interceptor / no authenticator) to break the dependency cycle. On refresh failure it clears prefs, which a reactive observer in `SplashViewModel` picks up and flips to `NeedsAuth`.

Splash resolves: no refresh token → `NeedsAuth`; valid access → `EnterApp`; else refresh (success → enter; 401 → `NeedsAuth`; offline → enter leniently).

---

## Navigation

Built on `androidx.navigation3` — not legacy Compose Navigation. `MainActivity.AppRoot()` swaps between `SplashScreen`, `MainNavHost`, and `AuthNavHost` based on `SplashViewModel.uiState`. `AuthNavHost` runs Onboarding → Login → Register. `MainNavHost` has Home + Profile tabs with per-tab back stacks, plus push entries for AddTask (bottom sheet), Details, Pomodoro, Settings.

Profile's "Sign in" / "Sign up" bubble up to the activity as `onRequestAuth`, which re-mounts `AuthNavHost` with `cancelable = true` — guests can poke at auth and back out.

---

## Pomodoro

A `@Singleton PomodoroEngineImpl` holds the timer state machine, driven by a `Channel.UNLIMITED` command queue for non-suspending public API. Four UI surfaces consume it: `pomodoro/launch/` (pre-start config), `pomodoro/edit/` (DataStore settings editor), `pomodoro/` root (the running ring + controls), and `pomodoro/banner/` (the top-bar mini-display mounted as the Scaffold's `topBar` on every other screen).

---

## Building

```bash
./gradlew :app:assembleDebug        # full build
./gradlew :app:compileDebugKotlin   # Kotlin + KSP only, faster
./gradlew :app:lintDebug            # Android Lint
```

Gotchas: AGP 9 alpha is deliberate (don't bump casually); `optimization { enable = false }` in release is intentional. Hilt 2.60 needs `compileOnly(libs.errorprone.annotations)`. The manifest strips `androidx.work.WorkManagerInitializer` from `androidx.startup` so the `HiltWorkerFactory` actually wins.

### Inspecting on device

```bash
adb shell run-as com.utakatalp.donebot sqlite3 /data/data/com.utakatalp.donebot/databases/donebot_debug.db
adb shell dumpsys jobscheduler | grep -i donebot
adb logcat -s WM-WorkerWrapper
```

---

## Backend

API at `https://api.candroid.dev/todos/` (Spring Kotlin, mounted under `/todos/` via nginx + `context-path`).

| Endpoint | Method | Bearer? |
|---|---|---|
| `/auth/login`, `/auth/register`, `/auth/refresh` | POST | no (auth client) |
| `/tasks` | GET / POST / PUT | yes |
| `/tasks/complete` | PATCH | yes |
| `/tasks/{id}` | DELETE | yes |

Responses are `BaseResponse<T?>` (`code`, `message`, `data`). `handleRequest { … }` normalizes errors into `DomainException` (`NoInternet`, `Unauthorized`, `NotFound`, `Server`, `Unknown`) and unwraps `data`.

---

## Conventions worth knowing

- **No emojis in code.** No multi-paragraph KDoc.
- **`TaskEntity.date`** is `LocalDate.toEpochDay()` (days since epoch). `timeStart`/`timeEnd` are minutes-of-day. Backend takes these `Long`s opaquely.
- **`PENDING_DELETE` rows are hidden from the UI.** DAO queries filter them.
- **No client-side dedup for `PENDING_CREATE`.** Partial-push retries can create server-side duplicates — accepted trade-off.
- **Stale-pending while online is by design.** Pending rows only drain on auth events, not every foreground resume.
- **MainActivity and DoneBotApplication live in the project root package**, not under `ui/`.

---

## Project layout (top level)

```
.
├── app/                                   # The application module
│   └── src/main/java/com/utakatalp/donebot/
│       ├── MainActivity.kt
│       ├── Application.kt                 # DoneBotApplication (HiltAndroidApp)
│       ├── common/                        # handleRequest, RingtoneHolder, Permissions, etc.
│       ├── data/                          # impls (Room, Retrofit, alarm, overlay, sync)
│       ├── di/                            # Hilt modules
│       ├── domain/                        # interfaces, models, use cases
│       ├── navigation/                    # AppKey + auth/ + main/ + NavigationEffect
│       └── ui/                            # screens, one folder per feature
├── uikit/                                 # Design-system module
├── .maestro/                              # UI test flows + recordings
└── gradle/libs.versions.toml              # Single source of truth for versions
```
