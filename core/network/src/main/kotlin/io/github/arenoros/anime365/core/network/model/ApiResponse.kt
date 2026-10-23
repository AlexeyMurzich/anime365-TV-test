package io.github.arenoros.anime365.core.network.model

import kotlinx.serialization.Serializable

@Serializable
data class ApiResponse<T>(
    val data: T? = null,
    val error: ApiErrorDto? = null,
) {
    fun unwrap(): T = data ?: throw ApiException(error?.code ?: 0, error?.message.orEmpty())
}

@Serializable
data class ApiErrorDto(
    val code: Int = 0,
    val message: String = "",
)

/** Сервер ответил ошибкой: `{"error":{"code":404,"message":"Series not found."}}`. */
class ApiException(
    val code: Int,
    message: String,
) : RuntimeException("API error $code: $message")
