package io.github.arenoros.anime365.core.data

import kotlinx.coroutines.CancellationException

/**
 * Сеть, ответ с ошибкой, неожиданный JSON — для экрана это одно: показать «ПОВТОРИТЬ», а не упасть.
 * Отмену корутины пропускаем дальше, иначе она превратилась бы в ошибку.
 */
@Suppress("TooGenericExceptionCaught")
internal suspend fun <T> catchingFailures(block: suspend () -> T): Result<T> =
    try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }
