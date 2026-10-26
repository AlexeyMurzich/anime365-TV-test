package io.github.arenoros.anime365.feature.player

import androidx.annotation.OptIn
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.keepScreenOn
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.round
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.compose.state.PlayPauseButtonState
import androidx.media3.ui.compose.state.rememberPlayPauseButtonState
import io.github.arenoros.anime365.core.designsystem.component.tvFocusIndicator
import io.github.arenoros.anime365.core.designsystem.icon.KadrIcons
import io.github.arenoros.anime365.core.designsystem.tokens.KadrRadius
import io.github.arenoros.anime365.core.designsystem.tokens.KadrSpacing
import io.github.arenoros.anime365.core.player.VideoPlayer
import io.github.arenoros.anime365.core.player.VideoSurface
import kotlinx.coroutines.launch

private const val WIDTH_FRACTION = 0.45f
private val MaxWidth = 320.dp
private val WindowElevation = 8.dp
private val ButtonBackground = Color.Black.copy(alpha = 0.5f)

/**
 * Свёрнутый Плеер: видео в окне поверх приложения, пока ходишь по экранам. Касание — развернуть,
 * ✕ — закрыть; окно перетаскивается и прилипает к ближайшему углу. В системной «картинке в
 * картинке» — только видео. [player] — `null` в скриншот-тестах.
 */
@OptIn(UnstableApi::class)
@Composable
fun MiniPlayer(
    player: VideoPlayer?,
    onExpand: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (rememberIsInPictureInPicture()) {
        Box(modifier.fillMaxSize().background(Color.Black)) {
            if (player != null) VideoSurface(player, Modifier.fillMaxSize())
        }
        return
    }
    val playPause = rememberPlayPauseButtonState(player?.player)
    BoxWithConstraints(modifier.fillMaxSize().statusBarsPadding().padding(KadrSpacing.Space3)) {
        val width = windowWidth(maxWidth)
        val height = width / VIDEO_ASPECT_RATIO
        val bounds = with(LocalDensity.current) { Offset((maxWidth - width).toPx(), (maxHeight - height).toPx()) }
        var atEnd by rememberSaveable { mutableStateOf(true) }
        var atBottom by rememberSaveable { mutableStateOf(true) }
        val corner = Offset(if (atEnd) bounds.x else 0f, if (atBottom) bounds.y else 0f)
        val offset = remember { Animatable(corner, Offset.VectorConverter) }
        // Повернули экран или отпустили окно в другой половине — к своему углу.
        LaunchedEffect(corner) { offset.animateTo(corner) }
        val scope = rememberCoroutineScope()
        // Где окно под пальцем: считается сразу, а не после того, как сдвиг доедет до offset.
        var position by remember { mutableStateOf(corner) }
        val settle = {
            atEnd = position.x > bounds.x / 2
            atBottom = position.y > bounds.y / 2
            // Тот же угол — LaunchedEffect не перезапустится: возвращаем окно сами.
            scope.launch { offset.animateTo(Offset(if (atEnd) bounds.x else 0f, if (atBottom) bounds.y else 0f)) }
        }
        val description = stringResource(R.string.feature_player_mini)
        Box(
            Modifier
                .offset { offset.value.round() }
                .size(width, height)
                .shadow(WindowElevation, RoundedCornerShape(KadrRadius.Md))
                .clip(RoundedCornerShape(KadrRadius.Md))
                .background(Color.Black)
                .then(if (playPause.showPlay) Modifier else Modifier.keepScreenOn()),
        ) {
            if (player != null) VideoSurface(player, Modifier.fillMaxSize())
            Box(
                Modifier
                    .matchParentSize()
                    .semantics { contentDescription = description }
                    .pointerInput(bounds) {
                        detectDragGestures(
                            onDragStart = { position = offset.value },
                            onDragEnd = { settle() },
                            onDragCancel = { settle() },
                        ) { change, drag ->
                            change.consume()
                            val moved = position + drag
                            position = Offset(moved.x.coerceIn(0f, bounds.x), moved.y.coerceIn(0f, bounds.y))
                            val target = position
                            scope.launch { offset.snapTo(target) }
                        }
                    }.clickable(onClickLabel = stringResource(R.string.feature_player_expand), onClick = onExpand),
            )
            WindowButtons(playPause, onClose)
        }
    }
}

/** ✕ — в верхнем углу, пауза — в нижнем: середина окна остаётся под касание «развернуть». */
@OptIn(UnstableApi::class)
@Composable
private fun BoxScope.WindowButtons(
    playPause: PlayPauseButtonState,
    onClose: () -> Unit,
) {
    MiniButton(
        KadrIcons.Close,
        stringResource(R.string.feature_player_close),
        onClose,
        Modifier.align(Alignment.TopEnd),
    )
    MiniButton(
        if (playPause.showPlay) KadrIcons.PlayArrow else KadrIcons.Pause,
        stringResource(if (playPause.showPlay) R.string.feature_player_play else R.string.feature_player_pause),
        playPause::onClick,
        Modifier.align(Alignment.BottomStart),
    )
}

/**
 * Где окно появляется после сворачивания — внизу справа в [area] (координаты корня): туда
 * сворачивается развёрнутый Плеер.
 */
fun miniPlayerBounds(
    area: Rect,
    density: Density,
): Rect =
    with(density) {
        val margin = KadrSpacing.Space3.toPx()
        val width = windowWidth((area.width - 2 * margin).toDp()).toPx()
        val height = width / VIDEO_ASPECT_RATIO
        Rect(area.right - margin - width, area.bottom - margin - height, area.right - margin, area.bottom - margin)
    }

private fun windowWidth(available: Dp): Dp = min(available * WIDTH_FRACTION, MaxWidth)

@Composable
private fun MiniButton(
    icon: Int,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier,
) {
    IconButton(onClick = onClick, modifier = modifier.tvFocusIndicator()) {
        Icon(
            painterResource(icon),
            contentDescription = description,
            Modifier.background(ButtonBackground, CircleShape).padding(KadrSpacing.Space1),
            tint = Color.White,
        )
    }
}
