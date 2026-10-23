package io.github.arenoros.anime365.core.player

import androidx.media3.common.Player
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * Плеер глазами экрана Плеера. Интерфейс, а не сам [AnimePlayer]: ViewModel проверяется
 * unit-тестом без ExoPlayer.
 */
interface VideoPlayer {
    /** Для видео, кнопок и полосы прогресса. */
    val player: Player

    val positionMs: Long

    /** Играет ли видео прямо сейчас: пока играет, Прогресс пишется раз в 10 секунд. */
    val playing: StateFlow<Boolean>

    /** `0`, пока длительность неизвестна. */
    val durationMs: Long

    /** Эпизод доигран до конца. */
    val ended: Flow<Unit>

    /** На мобильной сети по умолчанию 720p, иначе 1080p (docs/roadmap.md, M2). */
    val isMeteredNetwork: Boolean

    var subtitlesEnabled: Boolean

    var speed: Float

    /**
     * Возвращает номер сеанса для [stop]: экран, который уже сменился другим, не должен
     * останавливать чужое воспроизведение.
     */
    fun play(
        media: PlayerMedia,
        startPositionMs: Long,
    ): Int

    fun switchVideo(media: PlayerMedia)

    fun stop(session: Int)
}
