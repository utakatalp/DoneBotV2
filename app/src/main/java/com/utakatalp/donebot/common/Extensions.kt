package com.utakatalp.donebot.common

import android.database.sqlite.SQLiteException
import com.utakatalp.donebot.data.model.network.response.BaseResponse
import com.utakatalp.donebot.data.model.network.response.ErrorResponse
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import retrofit2.Response
import java.net.SocketTimeoutException
import java.net.UnknownHostException

inline fun <T> handleLocal(block: () -> T): Result<T> = try {
    Result.success(block())
} catch (ce: CancellationException) {
    throw ce
} catch (t: Throwable) {
    Result.failure(DomainException.fromThrowable(t))
}

suspend fun <T> handleRequest(request: suspend () -> Response<BaseResponse<T?>>): Result<T> = try {
    val response = request()

    if (!response.isSuccessful) {
        val errorBody = response.errorBody()?.string()
        val message = errorBody
            ?.let { runCatching { Json.decodeFromString<ErrorResponse>(it).message }.getOrNull() }
            ?: response.message()
            ?: "Something went wrong"
        when (response.code()) {
            401, 403 -> Result.failure(DomainException.Unauthorized())
            404 -> Result.failure(DomainException.NotFound(message))
            else -> Result.failure(DomainException.Server(message))
        }
    } else {
        val body = response.body()
            ?: return Result.failure(DomainException.Server("Empty response"))
        val data = body.data
            ?: return Result.failure(DomainException.Server(body.message))
        Result.success(data)
    }
} catch (t: Throwable) {
    if (t is CancellationException) throw t
    Result.failure(DomainException.fromThrowable(t))
}

sealed class DomainException(message: String) : Exception(message) {
    class NoInternet : DomainException("No internet connection")
    class Unauthorized : DomainException("Unauthorized")
    class NotFound(message: String) : DomainException(message)
    class Server(message: String) : DomainException(message)
    class Database(message: String) : DomainException(message)
    class Unknown(cause: Throwable) : DomainException(cause.message ?: "Unknown error")

    companion object {
        private const val HTTP_STATUS_UNAUTHORIZED = 401
        private const val HTTP_STATUS_NOT_FOUND = 404

        fun fromThrowable(t: Throwable): DomainException = when (t) {
            is UnknownHostException, is SocketTimeoutException -> NoInternet()
            is HttpException -> when (t.code()) {
                HTTP_STATUS_UNAUTHORIZED -> Unauthorized()
                HTTP_STATUS_NOT_FOUND -> NotFound(t.message())
                else -> Server("Server error")
            }
            is SQLiteException -> Database(t.message ?: "Database error")
            else -> Unknown(t)
        }
    }
}
