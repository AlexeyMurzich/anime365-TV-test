package io.github.arenoros.anime365.core.player

import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.Buffer
import okio.GzipSink
import okio.buffer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test

class GzipInterceptorTest {
    private val server = MockWebServer().apply { start() }
    private val client = OkHttpClient.Builder().addNetworkInterceptor(GzipInterceptor).build()

    @After
    fun tearDown() {
        server.close()
    }

    @Test
    fun unpacksGzipEvenWhenIdentityWasRequested() {
        server.enqueue(gzipped("[Script Info]"))

        // Так запрашивает Media3, и так сервер всё равно отвечает gzip.
        val request =
            Request
                .Builder()
                .url(server.url("/a.ass"))
                .header("Accept-Encoding", "identity")
                .build()

        assertEquals(
            "[Script Info]",
            client
                .newCall(request)
                .execute()
                .body
                .string(),
        )
    }

    @Test
    fun unpacksGzipThatOkHttpRequestedItself() {
        server.enqueue(gzipped("[Script Info]"))

        val request = Request.Builder().url(server.url("/a.ass")).build()

        assertEquals(
            "[Script Info]",
            client
                .newCall(request)
                .execute()
                .body
                .string(),
        )
    }

    private fun gzipped(text: String): MockResponse {
        val body = Buffer()
        GzipSink(body).buffer().use { it.writeUtf8(text) }
        return MockResponse
            .Builder()
            .body(body)
            .addHeader("Content-Encoding", "gzip")
            .build()
    }
}
