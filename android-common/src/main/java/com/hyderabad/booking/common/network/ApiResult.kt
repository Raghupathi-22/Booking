package com.hyderabad.booking.common.network

import org.json.JSONObject
import retrofit2.HttpException
import java.io.IOException

sealed interface ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>
    data class Error(val message: String, val code: Int? = null) : ApiResult<Nothing>
}

/** Runs an API call and turns every failure into a readable [ApiResult.Error]. */
suspend fun <T> safeApiCall(block: suspend () -> T): ApiResult<T> =
    try {
        ApiResult.Success(block())
    } catch (e: HttpException) {
        ApiResult.Error(parseErrorMessage(e), e.code())
    } catch (e: IOException) {
        ApiResult.Error("Can't reach the server. Check your internet connection and try again.")
    } catch (e: kotlinx.coroutines.CancellationException) {
        throw e
    } catch (e: Exception) {
        ApiResult.Error(e.message ?: "Something went wrong")
    }

/** Backend errors look like {"status":400,"error":"Bad Request","message":"..."} (message may be a map). */
private fun parseErrorMessage(e: HttpException): String {
    val fallback = when (e.code()) {
        401 -> "Please log in again."
        403 -> "You are not allowed to do this."
        404 -> "Not found."
        429 -> "Too many requests. Please wait a moment."
        else -> "Request failed (${e.code()})."
    }
    return try {
        val body = e.response()?.errorBody()?.string().orEmpty()
        val json = JSONObject(body)
        when (val message = json.opt("message")) {
            is String -> message.ifBlank { fallback }
            is JSONObject -> message.keys().asSequence().joinToString("\n") { message.optString(it) }
            else -> fallback
        }
    } catch (ex: Exception) {
        fallback
    }
}

inline fun <T, R> ApiResult<T>.map(transform: (T) -> R): ApiResult<R> = when (this) {
    is ApiResult.Success -> ApiResult.Success(transform(data))
    is ApiResult.Error -> this
}
