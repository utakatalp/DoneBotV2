package com.utakatalp.donebot.data.source.remote.interceptor

import android.util.Log
import com.utakatalp.donebot.domain.repository.AuthSessionRepository
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

private const val TAG = "AuthFlow"

class AuthInterceptor @Inject constructor(
    private val authSession: AuthSessionRepository,
) : Interceptor {

    private val noAuthPaths = listOf(
        "/auth/login",
        "/auth/register",
        "/auth/refresh",
    )

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        if (noAuthPaths.any { original.url.encodedPath.contains(it) }) {
            Log.d(TAG, "[AuthInterceptor] ${original.method} ${original.url.encodedPath} (auth-free path, no Bearer)")
            return chain.proceed(original)
        }
        val token = runBlocking { authSession.getAccessToken() }
        val request = if (token != null) {
            Log.d(
                TAG,
                "[AuthInterceptor] ${original.method} ${original.url.encodedPath} " +
                    "with Bearer ${token.takeLast(8)}...",
            )
            original.newBuilder().header("Authorization", "Bearer $token").build()
        } else {
            Log.d(
                TAG,
                "[AuthInterceptor] ${original.method} ${original.url.encodedPath} " +
                    "WITHOUT Bearer (no access token in DataStore)",
            )
            original
        }
        return chain.proceed(request)
    }
}
