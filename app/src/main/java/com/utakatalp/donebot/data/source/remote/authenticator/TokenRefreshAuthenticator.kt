package com.utakatalp.donebot.data.source.remote.authenticator

import com.utakatalp.donebot.common.DomainException
import com.utakatalp.donebot.data.model.network.request.RefreshTokenRequest
import com.utakatalp.donebot.domain.repository.AuthRepository
import com.utakatalp.donebot.domain.repository.SessionPreferences
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
    private val sessionPreferences: SessionPreferences,
    private val mutex: Mutex,
    private val authRepository: AuthRepository,
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        if (responseCount(response) >= MAX_RETRY_COUNT) return null

        return runBlocking {
            mutex.withLock {
                val storedAccessToken = sessionPreferences.getAccessToken()
                val requestAccessToken = response.request.header("Authorization")?.removePrefix("Bearer ")

                if (!storedAccessToken.isNullOrBlank() && storedAccessToken != requestAccessToken) {
                    return@runBlocking response.request.newBuilder()
                        .header("Authorization", "Bearer $storedAccessToken")
                        .build()
                }

                val storedRefreshToken = sessionPreferences.getRefreshToken() ?: return@runBlocking null
                if (storedRefreshToken.isBlank()) return@runBlocking null

                val refreshed = authRepository.refresh(RefreshTokenRequest(storedRefreshToken))
                if (refreshed.isSuccess) {
                    val tokens = refreshed.getOrThrow()
                    sessionPreferences.setAccessToken(tokens.accessToken)
                    sessionPreferences.setRefreshToken(tokens.refreshToken)
                    sessionPreferences.setExpiresAt(tokens.expiresIn)
                    response.request.newBuilder()
                        .header("Authorization", "Bearer ${tokens.accessToken}")
                        .build()
                } else {
                    if (refreshed.exceptionOrNull() is DomainException.Unauthorized) {
                        sessionPreferences.clear()
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
