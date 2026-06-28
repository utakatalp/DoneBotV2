package com.utakatalp.donebot.data.source.remote.authenticator

import com.utakatalp.donebot.common.DomainException
import com.utakatalp.donebot.domain.repository.AuthRepository
import com.utakatalp.donebot.domain.repository.AuthSessionRepository
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject

private const val MAX_RETRY_COUNT = 2

class TokenRefreshAuthenticator @Inject constructor(
    private val authSession: AuthSessionRepository,
    private val mutex: Mutex,
    private val authRepository: AuthRepository,
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        val path = response.request.url.encodedPath
        val count = responseCount(response)
        if (count >= MAX_RETRY_COUNT) {
            return null
        }

        return runBlocking {
            mutex.withLock {
                val storedAccessToken = authSession.getAccessToken()
                val requestAccessToken = response.request.header("Authorization")?.removePrefix("Bearer ")

                if (!storedAccessToken.isNullOrBlank() && storedAccessToken != requestAccessToken) {
                    return@runBlocking response.request.newBuilder()
                        .header("Authorization", "Bearer $storedAccessToken")
                        .build()
                }

                val storedRefreshToken = authSession.getRefreshToken()
                if (storedRefreshToken.isNullOrBlank()) {
                    return@runBlocking null
                }

                val refreshed = authRepository.refresh(storedRefreshToken)
                if (refreshed.isSuccess) {
                    val session = refreshed.getOrThrow()
                    authSession.saveSession(session)
                    response.request.newBuilder()
                        .header("Authorization", "Bearer ${session.accessToken}")
                        .build()
                } else {
                    val error = refreshed.exceptionOrNull()
                    when (error) {
                        is DomainException.Unauthorized,
                        is DomainException.NotFound,
                        -> {
                            // Terminal: refresh token rejected (401/403) OR the refresh endpoint
                            // itself is unreachable (404 — deploy bug, route typo, backend out of
                            // sync). Either way, retrying won't help — force re-auth. Clearing
                            // prefs triggers SplashViewModel's reactive observer to flip to
                            // NeedsAuth → AppRoot mounts AuthNavHost. Local Room data is preserved.
                            authSession.clear()
                        }
                        else -> {
                            // Transient (NoInternet / Server / Unknown): keep prefs, let the next
                            // request retry. User might recover when they're back online or the
                            // backend recovers.
                        }
                    }
                    null
                }
            }
        }
    }

    private fun responseCount(response: Response): Int {
        var r: Response? = response
        var count = 1
        while (r?.priorResponse != null) {
            count++
            r = r.priorResponse
        }
        return count
    }
}
