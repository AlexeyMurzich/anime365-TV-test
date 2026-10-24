package io.github.arenoros.anime365.core.model

/** Вид перевода — `type` в API. Порядок — как в Выборе Перевода (docs/screens.md). */
enum class TranslationKind(
    val apiType: String,
) {
    VoiceRu("voiceRu"),
    SubRu("subRu"),
    VoiceUk("voiceUk"),
    VoiceEn("voiceEn"),
    SubEn("subEn"),
    SubJa("subJa"),
    VoiceOther("voiceOther"),
    Raw("raw"),

    /** Вид, которого не было на сайте, когда писалось приложение. */
    Other(""),
    ;

    companion object {
        fun of(apiType: String): TranslationKind = entries.firstOrNull { it.apiType == apiType } ?: Other
    }
}

/**
 * Перевод одного Эпизода.
 * [team] — Команда, бывает пустой. [source] — Источник: `tv`, `bd`, `dvd`. [height] — Разрешение.
 */
data class Translation(
    val id: Int,
    val episodeId: Int,
    val kind: TranslationKind,
    val team: String,
    val source: String?,
    val height: Int,
)

/**
 * Видеопоток: ссылки CDN живут сутки, поэтому запрашивается при каждом запуске (docs/api-notes.md).
 * [qualities] — от большего Разрешения к меньшему. [subtitlesUrl] — Отдельные субтитры;
 * `null` — их нет или они Вшитые. [downloads] — ссылки для Скачивания: не истекают, но открываются
 * только с Токеном.
 */
data class VideoStream(
    val qualities: List<StreamQuality>,
    val subtitlesUrl: String?,
    val downloads: List<StreamQuality> = emptyList(),
)

data class StreamQuality(
    val height: Int,
    val url: String,
)
