package io.github.arenoros.anime365.core.model

/** Статус аниме в Библиотеке. [key] — имя в базе и в файле экспорта: его не меняют. */
enum class LibraryStatus(
    val key: String,
) {
    Watching("watching"),
    Planned("planned"),
    Completed("completed"),
    Dropped("dropped"),
    ;

    companion object {
        fun of(key: String?): LibraryStatus? = entries.firstOrNull { it.key == key }
    }
}

/** Строка Списка: «12 из 28 эп.» и бейдж Новых Эпизодов. */
data class LibraryEntry(
    val seriesId: Int,
    val title: String,
    val posterUrl: String?,
    val status: LibraryStatus,
    val watchedEpisodes: Int,
    val episodeCount: Int?,
    val newEpisodes: Int,
)

/** Главная кнопка аниме: недосмотренное — сразу в Плеер, иначе — следующий или первый Эпизод. */
sealed interface WatchAction {
    /** «ПРОДОЛЖИТЬ · 7 серия» — Плеер с тем же Переводом. */
    data class Resume(
        val lastWatched: LastWatched,
    ) : WatchAction

    /** «ПРОДОЛЖИТЬ · 8 серия» — прошлый досмотрен, Перевод для следующего ещё не выбран. */
    data class Next(
        val episode: Episode,
    ) : WatchAction

    /** «НАЧАТЬ ПРОСМОТР» — первый Эпизод. */
    data class Start(
        val episode: Episode,
    ) : WatchAction
}

/** Для [WatchAction.Resume] страница аниме не нужна: [details] может быть `null`. */
fun watchAction(
    details: SeriesDetails?,
    lastWatched: LastWatched?,
): WatchAction? =
    when {
        lastWatched != null && !lastWatched.progress.watched -> WatchAction.Resume(lastWatched)
        lastWatched != null -> details?.episodeAfter(lastWatched.episodeId)?.let(WatchAction::Next)
        else -> details?.firstEpisode?.let(WatchAction::Start)
    }
