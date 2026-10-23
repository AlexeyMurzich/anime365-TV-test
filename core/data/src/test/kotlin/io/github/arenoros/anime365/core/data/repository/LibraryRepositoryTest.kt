package io.github.arenoros.anime365.core.data.repository

import io.github.arenoros.anime365.core.data.MutableClock
import io.github.arenoros.anime365.core.data.inMemoryLibraryDatabase
import io.github.arenoros.anime365.core.database.library.LibraryDatabase
import io.github.arenoros.anime365.core.model.Episode
import io.github.arenoros.anime365.core.model.ReleaseStatus
import io.github.arenoros.anime365.core.model.SeriesSummary
import io.github.arenoros.anime365.core.model.Translation
import io.github.arenoros.anime365.core.model.TranslationChoice
import io.github.arenoros.anime365.core.model.TranslationKind
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class LibraryRepositoryTest {
    private val database: LibraryDatabase = inMemoryLibraryDatabase()
    private val clock = MutableClock()
    private val repository = LocalLibraryRepository(database.libraryDao(), clock)

    private val series =
        SeriesSummary(30414, "Фрирен", null, "poster", null, 28, 2023, null, 9.2, ReleaseStatus.Ongoing)
    private val first = Episode(101, 1.0, "1 серия", "tv")
    private val second = Episode(102, 2.0, "2 серия", "tv")
    private val anilibria = Translation(1, 101, TranslationKind.VoiceRu, "AniLibria", "tv", 1080)

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun episodeIsWatchedFromNinetyPercentAndStaysWatched() =
        runTest {
            repository.save(Playback(series, first, anilibria, positionMs = 899, durationMs = 1000))
            assertFalse(repository.progressOf(101)!!.watched)

            repository.save(Playback(series, first, anilibria, positionMs = 900, durationMs = 1000))
            repository.save(Playback(series, first, anilibria, positionMs = 10, durationMs = 1000))

            assertTrue(repository.progressOf(101)!!.watched)
        }

    @Test
    fun continueWatchingShowsLastUnfinishedEpisodePerSeries() =
        runTest {
            repository.save(Playback(series, first, anilibria, positionMs = 1000, durationMs = 1000))
            assertEquals(emptyList<Any>(), repository.continueWatching().first())

            clock.advanceMinutes(1)
            repository.save(Playback(series, second, anilibria.copy(id = 2, episodeId = 102), 300, 1000))

            val cards = repository.continueWatching().first()
            assertEquals(1, cards.size)
            assertEquals("Фрирен", cards[0].seriesTitle)
            assertEquals(102, cards[0].lastWatched.episodeId)
            assertEquals("2 серия", cards[0].lastWatched.episodeLabel)
            assertEquals(2, cards[0].lastWatched.translationId)
            assertEquals(0.3f, cards[0].lastWatched.progress.fraction, 0.001f)
            assertEquals(setOf(101, 102), repository.progress(30414).first().keys)
        }

    @Test
    fun rememberedChoiceIsKindAndTeam() =
        runTest {
            repository.save(Playback(series, first, anilibria, 0, 1000))

            assertEquals(TranslationChoice(TranslationKind.VoiceRu, "AniLibria"), repository.choice(30414))
        }
}
