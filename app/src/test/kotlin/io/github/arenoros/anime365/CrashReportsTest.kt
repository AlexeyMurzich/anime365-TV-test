package io.github.arenoros.anime365

import org.junit.Assert.assertEquals
import org.junit.Test

class CrashReportsTest {
    @Test
    fun tokenIsRemovedFromUrlsInMessages() {
        assertEquals(
            "Failed: https://smotret-anime.app/api/translations/embed/1?access_token=[Filtered]&x=1",
            withoutToken("Failed: https://smotret-anime.app/api/translations/embed/1?access_token=ab12-CD_34&x=1"),
        )
        assertEquals("\"…?access_token=[Filtered]\" timed out", withoutToken("\"…?access_token=secret\" timed out"))
    }

    @Test
    fun textWithoutTokenStaysAsIs() {
        val text = "HTTP 500 at https://smotret-anime.app/api/series?fields=id"
        assertEquals(text, withoutToken(text))
    }
}
