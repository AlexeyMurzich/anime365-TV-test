package io.github.arenoros.anime365.core.data.repository

import io.github.arenoros.anime365.core.model.Translation
import io.github.arenoros.anime365.core.model.ofTeam
import javax.inject.Inject

/**
 * Перевод запомненной для аниме Команды у этого Эпизода: с ним касание Эпизода ведёт сразу в Плеер.
 * `null` — Команду не запоминали или у Эпизода её нет: нужен Выбор Перевода.
 */
class RememberedTranslation
    @Inject
    constructor(
        private val library: LibraryRepository,
        private val translations: TranslationRepository,
    ) {
        suspend operator fun invoke(
            seriesId: Int,
            episodeId: Int,
        ): Translation? {
            val choice = library.choice(seriesId) ?: return null
            return translations.load(episodeId).getOrNull()?.ofTeam(choice)
        }
    }
