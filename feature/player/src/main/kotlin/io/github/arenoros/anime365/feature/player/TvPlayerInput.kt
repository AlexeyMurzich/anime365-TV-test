package io.github.arenoros.anime365.feature.player

import androidx.annotation.OptIn
import androidx.compose.foundation.focusable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.compose.state.PlayPauseButtonState

@OptIn(UnstableApi::class)
@Composable
internal fun Modifier.tvPlayerInput(
    player: Player?,
    playPause: PlayPauseButtonState,
    visible: Boolean,
    requester: FocusRequester,
    onHasFocus: (Boolean) -> Unit,
    onInteraction: () -> Unit,
    onShowControls: () -> Unit,
    onHideControls: () -> Unit,
): Modifier {
    val input = remember { TvRemoteInput() }
    return this
        .onFocusChanged { onHasFocus(it.hasFocus) }
        .focusRequester(requester)
        .onPreviewKeyEvent { event ->
            val down = event.type == KeyEventType.KeyDown
            if (down) onInteraction()
            input.handle(event.nativeKeyEvent.keyCode, down, event.nativeKeyEvent.repeatCount, visible) { action ->
                if (action == TvRemoteAction.HideControls) {
                    onHideControls()
                } else {
                    performRemoteAction(action, player, playPause)
                    onShowControls()
                }
            }
        }.focusable()
}

@OptIn(UnstableApi::class)
private fun performRemoteAction(
    action: TvRemoteAction,
    player: Player?,
    playPause: PlayPauseButtonState,
) {
    when (action) {
        TvRemoteAction.SeekBack -> {
            player?.seekBack()
        }

        TvRemoteAction.SeekForward -> {
            player?.seekForward()
        }

        TvRemoteAction.TogglePlayback -> {
            playPause.onClick()
        }

        TvRemoteAction.Play -> {
            player?.play()
        }

        TvRemoteAction.Pause -> {
            player?.pause()
        }

        else -> {}
    }
}
