package io.github.arenoros.anime365.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PickHeightTest {
    @Test
    fun qualityPrefersHighestNotAboveTarget() {
        assertEquals(1080, pickHeight(listOf(2160, 1080, 720), target = 1080))
        assertEquals(720, pickHeight(listOf(1080, 720, 360), target = 720))
        assertEquals(480, pickHeight(listOf(480, 360), target = 1080))
        // Все выше целевого — самое маленькое из них.
        assertEquals(1080, pickHeight(listOf(2160, 1080), target = 720))
        assertNull(pickHeight(emptyList(), target = 1080))
    }
}
