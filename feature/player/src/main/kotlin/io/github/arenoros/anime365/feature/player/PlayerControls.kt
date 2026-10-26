package io.github.arenoros.anime365.feature.player

import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.compose.state.PlayPauseButtonState
import androidx.media3.ui.compose.state.rememberPlayPauseButtonState
import androidx.media3.ui.compose.state.rememberProgressStateWithTickInterval
import io.github.arenoros.anime365.core.designsystem.component.isTelevision
import io.github.arenoros.anime365.core.designsystem.component.tvAwareSheetDragHandle
import io.github.arenoros.anime365.core.designsystem.component.tvFocusIndicator
import io.github.arenoros.anime365.core.designsystem.icon.KadrIcons
import io.github.arenoros.anime365.core.designsystem.tokens.KadrRadius
import io.github.arenoros.anime365.core.designsystem.tokens.KadrSpacing
import kotlinx.coroutines.delay
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.milliseconds
import io.github.arenoros.anime365.core.designsystem.R as DesignR

private const val HIDE_CONTROLS_MS = 3_000L
private const val TICK_MS = 1_000L

// Меток опенинга в API нет: «+85 с» показываем в первые 5 минут Эпизода (docs/screens.md).
private val SKIP_OPENING_WINDOW_MS = TimeUnit.MINUTES.toMillis(5)
private const val SKIP_OPENING_MS = 85_000L
private val SPEEDS = listOf(0.5f, 0.75f, 1f, 1.25f, 1.5f, 2f)
private val Scrim = Color.Black.copy(alpha = 0.45f)
private val DisabledControl = Color.White.copy(alpha = 0.38f)
private const val EDGE_FRACTION = 1f / 3

/**
 * Управление поверх видео: скрывается через 3 с, касание показывает и прячет, двойное касание у
 * краёв — перемотка на 10 с. По краям центра — соседние серии. [player] — `null` в скриншот-тестах.
 */
@OptIn(UnstableApi::class)
@Composable
internal fun PlayerControls(
    state: PlayerUiState,
    player: Player?,
    fullscreen: Boolean,
    onBack: () -> Unit,
    onChooseTranslation: () -> Unit,
    onOpenSettings: () -> Unit,
    onToggleFullscreen: () -> Unit,
    onPlayPrevious: () -> Unit,
    onPlayNext: () -> Unit,
    modifier: Modifier = Modifier,
    initiallyVisible: Boolean = true,
    active: Boolean = true,
) {
    val playPause = rememberPlayPauseButtonState(player)
    var visible by remember { mutableStateOf(initiallyVisible) }
    val playing = !playPause.showPlay
    val television = isTelevision()
    val surfaceFocus = remember { FocusRequester() }
    val playFocus = remember { FocusRequester() }
    var interaction by remember { mutableStateOf(0) }
    var controlsHaveFocus by remember { mutableStateOf(false) }
    val inputReady = !television || controlsHaveFocus
    BackHandler(enabled = television && visible) { visible = false }
    RequestPlayerControlsFocus(television && active, if (visible) playFocus else surfaceFocus, controlsHaveFocus)
    LaunchedEffect(visible, playing, interaction, inputReady) {
        if (visible && playing && inputReady) {
            delay(HIDE_CONTROLS_MS)
            visible = false
        }
    }
    Box(
        modifier
            .fillMaxSize()
            .then(
                if (television) {
                    Modifier.tvPlayerInput(
                        player,
                        playPause,
                        visible,
                        surfaceFocus,
                        onHasFocus = { controlsHaveFocus = it },
                        onInteraction = { interaction++ },
                        onShowControls = { visible = true },
                        onHideControls = { visible = false },
                    )
                } else {
                    Modifier
                },
            ).playerTouchInput(player) { visible = !visible },
    ) {
        PlayerControlsVisibility(visible, television) {
            Box(Modifier.fillMaxSize().background(Scrim)) {
                TopBar(state, onBack, onChooseTranslation, onOpenSettings)
                CenterControls(
                    state,
                    player,
                    playPause,
                    onPlayPrevious,
                    onPlayNext,
                    if (television) playFocus else null,
                )
                BottomBar(state, player, fullscreen, onToggleFullscreen)
            }
        }
    }
}

@Composable
private fun RequestPlayerControlsFocus(
    television: Boolean,
    requester: FocusRequester,
    hasFocus: Boolean,
) {
    val windowFocused = LocalWindowInfo.current.isWindowFocused
    LaunchedEffect(television, requester, windowFocused) {
        if (television && windowFocused) {
            withFrameNanos { }
            requester.requestFocus()
        }
    }
    // Закрывшийся диалог может ещё раз сбросить фокус: пока слой активен, фокус возвращается.
    LaunchedEffect(television, windowFocused, hasFocus) {
        if (television && windowFocused && !hasFocus) {
            withFrameNanos { }
            requester.requestFocus()
        }
    }
}

/** На телевизоре скрытые кнопки исчезают сразу: во время анимации исчезновения они ещё ловили бы фокус. */
@Composable
private fun PlayerControlsVisibility(
    visible: Boolean,
    television: Boolean,
    content: @Composable () -> Unit,
) {
    if (television) {
        if (visible) content()
    } else {
        AnimatedVisibility(visible, enter = fadeIn(), exit = fadeOut()) { content() }
    }
}

@OptIn(UnstableApi::class)
private fun Modifier.playerTouchInput(
    player: Player?,
    onToggle: () -> Unit,
): Modifier =
    pointerInput(player) {
        detectTapGestures(
            onTap = { onToggle() },
            onDoubleTap = { offset ->
                when {
                    offset.x < size.width * EDGE_FRACTION -> player?.seekBack()
                    offset.x > size.width * (1 - EDGE_FRACTION) -> player?.seekForward()
                }
            },
        )
    }

/** Соседние серии, перемотка и пауза; серии нет — кнопка неактивна. */
@OptIn(UnstableApi::class)
@Composable
private fun BoxScope.CenterControls(
    state: PlayerUiState,
    player: Player?,
    playPause: PlayPauseButtonState,
    onPlayPrevious: () -> Unit,
    onPlayNext: () -> Unit,
    playFocus: FocusRequester?,
) {
    Row(
        Modifier.align(Alignment.Center),
        horizontalArrangement = Arrangement.spacedBy(KadrSpacing.Space4),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ControlButton(
            KadrIcons.SkipPrevious,
            stringResource(R.string.feature_player_previous_episode),
            enabled = state.previousEpisode != null,
            onClick = onPlayPrevious,
        )
        ControlButton(
            KadrIcons.Replay10,
            stringResource(R.string.feature_player_rewind),
        ) { player?.seekBack() }
        ControlButton(
            icon = if (playPause.showPlay) KadrIcons.PlayArrow else KadrIcons.Pause,
            description =
                stringResource(
                    if (playPause.showPlay) R.string.feature_player_play else R.string.feature_player_pause,
                ),
            iconSize = 56.dp,
            modifier = if (playFocus != null) Modifier.focusRequester(playFocus) else Modifier,
            onClick = playPause::onClick,
        )
        ControlButton(
            KadrIcons.Forward10,
            stringResource(R.string.feature_player_forward),
        ) { player?.seekForward() }
        ControlButton(
            KadrIcons.SkipNext,
            stringResource(R.string.feature_player_next_episode),
            enabled = state.nextEpisode != null,
            onClick = onPlayNext,
        )
    }
}

@Composable
private fun BoxScope.TopBar(
    state: PlayerUiState,
    onBack: () -> Unit,
    onChooseTranslation: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    Row(Modifier.align(Alignment.TopCenter).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        ControlButton(KadrIcons.ArrowBack, stringResource(DesignR.string.core_designsystem_back), onClick = onBack)
        Text(
            listOfNotNull(
                state.episode?.label,
                state.translation?.team?.takeIf { it.isNotEmpty() },
            ).joinToString(" · "),
            Modifier.weight(1f),
            color = Color.White,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        ControlButton(
            KadrIcons.Translate,
            stringResource(R.string.feature_player_translation),
            onClick = onChooseTranslation,
        )
        ControlButton(KadrIcons.Settings, stringResource(R.string.feature_player_settings), onClick = onOpenSettings)
    }
}

@OptIn(UnstableApi::class)
@Composable
private fun BoxScope.BottomBar(
    state: PlayerUiState,
    player: Player?,
    fullscreen: Boolean,
    onToggleFullscreen: () -> Unit,
) {
    val progress = rememberProgressStateWithTickInterval(player, tickIntervalMs = TICK_MS)
    val duration = progress.durationMs.takeIf { it != C.TIME_UNSET && it > 0 } ?: 0L
    Column(Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(horizontal = KadrSpacing.Space2)) {
        SeekBar(progress.currentPositionMs, progress.bufferedPositionMs, duration, onSeek = { player?.seekTo(it) })
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "${formatTime(progress.currentPositionMs)} / ${formatTime(duration)}",
                Modifier.padding(start = KadrSpacing.Space2),
                color = Color.White,
                style = MaterialTheme.typography.labelLarge,
            )
            Spacer(Modifier.weight(1f))
            if (progress.currentPositionMs < SKIP_OPENING_WINDOW_MS) {
                val description = stringResource(R.string.feature_player_skip_opening_description)
                TextButton(
                    onClick = { player?.let { it.seekTo(it.currentPosition + SKIP_OPENING_MS) } },
                    modifier = Modifier.tvFocusIndicator().semantics { contentDescription = description },
                ) { Text(stringResource(R.string.feature_player_skip_opening), color = Color.White) }
            }
            state.quality?.let { height ->
                Text(
                    stringResource(R.string.feature_player_height, height),
                    Modifier.border().padding(horizontal = KadrSpacing.Space1, vertical = 2.dp),
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
            ControlButton(
                icon = if (fullscreen) KadrIcons.FullscreenExit else KadrIcons.Fullscreen,
                description =
                    stringResource(
                        if (fullscreen) R.string.feature_player_exit_fullscreen else R.string.feature_player_fullscreen,
                    ),
                onClick = onToggleFullscreen,
            )
        }
    }
}

private fun Modifier.border(): Modifier = background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(KadrRadius.Xs))

/** Полоса прогресса с буфером: буфер — подложкой под ползунком. */
@Composable
private fun SeekBar(
    positionMs: Long,
    bufferedMs: Long,
    durationMs: Long,
    onSeek: (Long) -> Unit,
) {
    var dragging by remember { mutableStateOf<Float?>(null) }
    val fraction = if (durationMs > 0) positionMs.toFloat() / durationMs else 0f
    val positionDescription = stringResource(R.string.feature_player_position)
    Box(contentAlignment = Alignment.Center) {
        LinearProgressIndicator(
            progress = { if (durationMs > 0) bufferedMs.toFloat() / durationMs else 0f },
            modifier = Modifier.fillMaxWidth().padding(horizontal = KadrSpacing.Space2),
            color = Color.White.copy(alpha = 0.5f),
            trackColor = Color.White.copy(alpha = 0.2f),
            drawStopIndicator = {},
        )
        Slider(
            value = dragging ?: fraction,
            onValueChange = { dragging = it },
            onValueChangeFinished = {
                dragging?.let { onSeek((it * durationMs).toLong()) }
                dragging = null
            },
            enabled = durationMs > 0,
            modifier = Modifier.tvFocusIndicator().semantics { contentDescription = positionDescription },
            colors =
                SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = Color.White,
                    inactiveTrackColor = Color.Transparent,
                    disabledInactiveTrackColor = Color.Transparent,
                ),
        )
    }
}

@Composable
private fun ControlButton(
    icon: Int,
    description: String,
    modifier: Modifier = Modifier,
    iconSize: Dp = 28.dp,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier.size(iconSize + KadrSpacing.Space4).tvFocusIndicator(),
        enabled = enabled,
    ) {
        Icon(
            painterResource(icon),
            contentDescription = description,
            Modifier.size(iconSize),
            tint = if (enabled) Color.White else DisabledControl,
        )
    }
}

/** Конец Эпизода: «Следующая серия через 5 с» и «ОТМЕНА» (docs/screens.md). */
@Composable
internal fun NextEpisodeCard(
    seconds: Int,
    onPlayNow: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier,
        shape = MaterialTheme.shapes.medium,
        color = Color.Black.copy(alpha = 0.8f),
        contentColor = Color.White,
    ) {
        Row(Modifier.padding(start = KadrSpacing.Space4), verticalAlignment = Alignment.CenterVertically) {
            TextButton(
                modifier = Modifier.tvFocusIndicator(),
                onClick = onPlayNow,
            ) { Text(stringResource(R.string.feature_player_next_in, seconds), color = Color.White) }
            TextButton(
                modifier = Modifier.tvFocusIndicator(),
                onClick = onCancel,
            ) { Text(stringResource(R.string.feature_player_cancel).uppercase(), color = Color.White) }
        }
    }
}

/** Разрешение (смена не сбрасывает позицию), скорость и Отдельные субтитры. */
@kotlin.OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PlayerSettingsSheet(
    state: PlayerUiState,
    onSelectQuality: (Int) -> Unit,
    onSelectSpeed: (Float) -> Unit,
    onSubtitlesChange: (Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(dragHandle = tvAwareSheetDragHandle(), onDismissRequest = onDismiss) {
        Column(
            Modifier.padding(horizontal = KadrSpacing.Space4).navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(KadrSpacing.Space2),
        ) {
            SettingTitle(stringResource(R.string.feature_player_quality))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(KadrSpacing.Space2)) {
                state.qualities.forEach { height ->
                    FilterChip(
                        modifier = Modifier.tvFocusIndicator(),
                        selected = height == state.quality,
                        onClick = { onSelectQuality(height) },
                        label = { Text(stringResource(R.string.feature_player_height, height)) },
                    )
                }
            }
            SettingTitle(stringResource(R.string.feature_player_speed))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(KadrSpacing.Space2)) {
                SPEEDS.forEach { speed ->
                    FilterChip(
                        modifier = Modifier.tvFocusIndicator(),
                        selected = speed == state.speed,
                        onClick = { onSelectSpeed(speed) },
                        label = { Text(stringResource(R.string.feature_player_speed_value, formatSpeed(speed))) },
                    )
                }
            }
            if (state.hasSubtitles) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    SettingTitle(stringResource(R.string.feature_player_subtitles), Modifier.weight(1f))
                    Switch(
                        modifier = Modifier.tvFocusIndicator(),
                        checked = state.subtitlesEnabled,
                        onCheckedChange = onSubtitlesChange,
                    )
                }
            }
            Spacer(Modifier.padding(bottom = KadrSpacing.Space4))
        }
    }
}

@Composable
private fun SettingTitle(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(text, modifier.padding(top = KadrSpacing.Space2), style = MaterialTheme.typography.titleSmall)
}

/** «1:05:09», «24:00». */
internal fun formatTime(ms: Long): String =
    ms.coerceAtLeast(0).milliseconds.toComponents { hours, minutes, seconds, _ ->
        if (hours > 0) {
            String.format(Locale.ROOT, "%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format(Locale.ROOT, "%d:%02d", minutes, seconds)
        }
    }

/** «1», «0.75», «1.5». */
private fun formatSpeed(speed: Float): String = if (speed % 1f == 0f) speed.toInt().toString() else speed.toString()
