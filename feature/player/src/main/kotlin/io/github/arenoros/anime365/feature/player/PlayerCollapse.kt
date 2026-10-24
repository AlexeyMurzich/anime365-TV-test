package io.github.arenoros.anime365.feature.player

import androidx.compose.animation.core.animate
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.lerp
import androidx.compose.ui.graphics.GraphicsLayerScope
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

/** Отпустили дальше этой доли пути — Плеер доезжает до окна сам. */
private const val COLLAPSE_FRACTION = 0.3f

/** Быстрый взмах вниз сворачивает, вверх — возвращает, сколько бы ни протащили. */
private val FlingVelocity = 400.dp

/**
 * Сворачивание Плеера в окно поверх приложения: видео уменьшается и едет туда, где появится окно
 * ([target], координаты корня), остальное гаснет и открывает приложение под Плеером.
 * [fraction]: 0 — развёрнут, 1 — видео уже на месте окна, дальше [onCollapsed].
 */
@Stable
internal class PlayerCollapse(
    private val target: () -> Rect?,
    private val onCollapsed: () -> Unit,
) {
    var fraction by mutableFloatStateOf(0f)
        private set

    /** Видео без сдвига; `null` — сворачивать анимацией неоткуда (ландшафт, ещё не измерено). */
    var videoBounds: Rect? = null

    private val path: Pair<Rect, Rect>?
        get() {
            val from = videoBounds ?: return null
            val to = target() ?: return null
            return from to to
        }

    /** Где сейчас видео. */
    fun videoAt(fraction: Float): Rect? = path?.let { (from, to) -> lerp(from, to, fraction) }

    /** Во сколько раз видео сейчас меньше: жест по нему приходит в уменьшенных координатах. */
    val scale: Float get() = videoBounds?.let { from -> videoAt(fraction)?.let { it.width / from.width } } ?: 1f

    /** Палец сдвинулся на [dy] пикселей экрана. */
    fun dragBy(dy: Float) {
        val (from, to) = path ?: return
        fraction = (fraction + dy / (to.top - from.top)).coerceIn(0f, 1f)
    }

    suspend fun collapse() {
        if (path != null) animate(fraction, 1f) { value, _ -> fraction = value }
        onCollapsed()
    }

    /** Палец отпущен со скоростью [velocity] пикселей в секунду вниз. */
    suspend fun settle(
        velocity: Float,
        flingVelocity: Float,
    ) {
        if (shouldCollapse(fraction, velocity, flingVelocity)) {
            collapse()
        } else {
            animate(fraction, 0f) { value, _ -> fraction = value }
        }
    }

    /** Масштаб и сдвиг слоя Плеера: видео из [videoBounds] переезжает в окно. */
    fun transform(layer: GraphicsLayerScope) {
        val from = videoBounds ?: return
        val video = videoAt(fraction) ?: return
        val scale = video.width / from.width
        layer.transformOrigin = TransformOrigin(0f, 0f)
        layer.scaleX = scale
        layer.scaleY = scale
        layer.translationX = video.left - from.left * scale
        layer.translationY = video.top - from.top * scale
    }
}

internal fun shouldCollapse(
    fraction: Float,
    velocity: Float,
    flingVelocity: Float,
): Boolean =
    when {
        velocity > flingVelocity -> true
        velocity < -flingVelocity -> false
        else -> fraction > COLLAPSE_FRACTION
    }

/**
 * Видео Плеера: запоминает, где оно без сдвига, и сворачивается свайпом вниз, если [enabled].
 * Касания и двойные касания управления не мешают: свайп начинается только после сдвига по вертикали.
 */
internal fun Modifier.collapsibleVideo(
    collapse: PlayerCollapse,
    enabled: Boolean,
): Modifier =
    onGloballyPositioned { if (collapse.fraction == 0f) collapse.videoBounds = it.boundsInRoot() }
        .then(if (enabled) Modifier.swipeDownToCollapse(collapse) else Modifier)

private fun Modifier.swipeDownToCollapse(collapse: PlayerCollapse): Modifier =
    pointerInput(collapse) {
        val flingVelocity = FlingVelocity.toPx()
        val tracker = VelocityTracker()
        var settling: Job? = null
        // Позиция пальца на экране: видео под ним само уменьшается и едет.
        var y = 0f
        coroutineScope {
            detectVerticalDragGestures(
                onDragStart = {
                    settling?.cancel()
                    tracker.resetTracking()
                    y = 0f
                },
                onDragEnd = {
                    val velocity = tracker.calculateVelocity().y
                    settling = launch { collapse.settle(velocity, flingVelocity) }
                },
                onDragCancel = { settling = launch { collapse.settle(0f, flingVelocity) } },
            ) { change, dragAmount ->
                change.consume()
                val dy = dragAmount * collapse.scale
                y += dy
                tracker.addPosition(change.uptimeMillis, Offset(0f, y))
                collapse.dragBy(dy)
            }
        }
    }
