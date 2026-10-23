package io.github.arenoros.anime365.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TranslationChoiceTest {
    private val tv = Translation(2, 102, TranslationKind.VoiceRu, "AniLibria", "tv", 1080)
    private val bd = tv.copy(id = 5, source = "bd")
    private val anilibria = TranslationChoice(TranslationKind.VoiceRu, "AniLibria")

    @Test
    fun sameTeamPrefersSameSource() {
        assertEquals(tv, listOf(bd, tv).ofTeam(anilibria, source = "tv"))
        assertEquals(bd, listOf(bd).ofTeam(anilibria, source = "tv"))
        assertEquals(bd, listOf(bd, tv).ofTeam(anilibria))
    }

    @Test
    fun teamAndKindMustMatch() {
        assertNull(listOf(tv.copy(team = "")).ofTeam(anilibria.copy(team = "")))
        assertNull(listOf(tv.copy(kind = TranslationKind.SubRu)).ofTeam(anilibria))
        assertNull(emptyList<Translation>().ofTeam(anilibria))
    }
}
