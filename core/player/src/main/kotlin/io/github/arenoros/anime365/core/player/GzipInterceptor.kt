package io.github.arenoros.anime365.core.player

import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody.Companion.asResponseBody
import okio.GzipSource
import okio.buffer

/**
 * Файлы `/episodeTranslations/{id}.ass` сервер отдаёт в gzip даже на `Accept-Encoding: identity`,
 * а Media3 просит именно identity — и OkHttp такой ответ не распаковывает (docs/api-notes.md).
 * Сетевой перехватчик распаковывает сам; если gzip попросил OkHttp, распакованное он пропустит как есть.
 */
internal object GzipInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        if (!response.header("Content-Encoding").equals("gzip", ignoreCase = true)) return response
        val body = response.body
        return response
            .newBuilder()
            .removeHeader("Content-Encoding")
            .removeHeader("Content-Length")
            .body(GzipSource(body.source()).buffer().asResponseBody(body.contentType(), -1))
            .build()
    }
}
