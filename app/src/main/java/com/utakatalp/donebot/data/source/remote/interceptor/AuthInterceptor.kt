package com.utakatalp.donebot.data.source.remote.interceptor

import com.utakatalp.donebot.domain.repository.AuthSessionRepository
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

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
            return chain.proceed(original)
        }
        val token = runBlocking { authSession.getAccessToken() }
        val request = if (token != null) {
            original.newBuilder().header("Authorization", "Bearer $token").build()
        } else {
            original
        }
        return chain.proceed(request)
    }
}
