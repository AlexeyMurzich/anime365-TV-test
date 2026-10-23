package io.github.arenoros.anime365.core.testing

import io.github.arenoros.anime365.core.data.repository.AccountRepository
import io.github.arenoros.anime365.core.data.repository.LibraryRepository
import io.github.arenoros.anime365.core.data.repository.Playback
import io.github.arenoros.anime365.core.data.repository.SeriesRepository
import io.github.arenoros.anime365.core.data.repository.TranslationRepository
import io.github.arenoros.anime365.core.model.Account
import io.github.arenoros.anime365.core.model.ContinueWatching
import io.github.arenoros.anime365.core.model.EpisodeProgress
import io.github.arenoros.anime365.core.model.LastWatched
import io.github.arenoros.anime365.core.model.Series
import io.github.arenoros.anime365.core.model.Translation
import io.github.arenoros.anime365.core.model.TranslationChoice
import io.github.arenoros.anime365.core.model.VideoStream
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeSeriesRepository : SeriesRepository {
    val series = MutableStateFlow<Series?>(null)
    val refreshCalls = mutableListOf<Pair<Int, Boolean>>()
    var result: Result<Unit> = Result.success(Unit)

    override fun series(id: Int): Flow<Series?> = series

    override suspend fun refresh(
        id: Int,
        force: Boolean,
    ): Result<Unit> {
        refreshCalls += id to force
        return result
    }
}

/** Переводы по `id` Эпизода; [stream] — ответ Видеопотока по `id` Перевода. */
class FakeTranslationRepository : TranslationRepository {
    val translations = MutableStateFlow<Map<Int, List<Translation>>>(emptyMap())
    var refreshResult: Result<Unit> = Result.success(Unit)
    var stream: (Int) -> Result<VideoStream> = { error("stream $it not stubbed") }
    val streamRequests = mutableListOf<Int>()

    override fun translations(episodeId: Int): Flow<List<Translation>> = translations.map { it[episodeId].orEmpty() }

    override suspend fun refresh(
        episodeId: Int,
        force: Boolean,
    ): Result<Unit> = refreshResult

    override suspend fun load(episodeId: Int): Result<List<Translation>> =
        refreshResult.map { translations.value[episodeId].orEmpty() }

    override suspend fun stream(translationId: Int): Result<VideoStream> {
        streamRequests += translationId
        return stream.invoke(translationId)
    }
}

class FakeLibraryRepository : LibraryRepository {
    val progress = MutableStateFlow<Map<Int, EpisodeProgress>>(emptyMap())
    val lastWatched = MutableStateFlow<LastWatched?>(null)
    val continueWatching = MutableStateFlow<List<ContinueWatching>>(emptyList())
    var choice: TranslationChoice? = null
    val saved = mutableListOf<Playback>()

    override fun progress(seriesId: Int): Flow<Map<Int, EpisodeProgress>> = progress

    override fun lastWatched(seriesId: Int): Flow<LastWatched?> = lastWatched

    override fun continueWatching(): Flow<List<ContinueWatching>> = continueWatching

    override suspend fun progressOf(episodeId: Int): EpisodeProgress? = progress.value[episodeId]

    override suspend fun choice(seriesId: Int): TranslationChoice? = choice

    override suspend fun save(playback: Playback) {
        saved += playback
    }
}

class FakeAccountRepository : AccountRepository {
    override val account = MutableStateFlow<Account>(Account.Guest)
    val logInCookies = mutableListOf<String>()
    var logInResult: Result<Unit> = Result.success(Unit)
    var refreshCalls = 0

    override suspend fun logIn(sessionCookie: String): Result<Unit> {
        logInCookies += sessionCookie
        return logInResult
    }

    override suspend fun refresh(): Result<Unit> {
        refreshCalls++
        return Result.success(Unit)
    }

    override suspend fun logOut() {
        account.value = Account.Guest
    }
}
