package io.github.arenoros.anime365.feature.player

import android.view.KeyEvent

internal enum class TvRemoteAction {
    None,
    ShowControls,
    HideControls,
    SeekBack,
    SeekForward,
    TogglePlayback,
    Play,
    Pause,
}

/** При видимых кнопках стрелки и OK двигают и нажимают фокус как обычно. Медиакнопки работают всегда. */
internal fun tvRemoteAction(
    keyCode: Int,
    controlsVisible: Boolean,
): TvRemoteAction =
    when (keyCode) {
        KeyEvent.KEYCODE_BACK -> if (controlsVisible) TvRemoteAction.HideControls else TvRemoteAction.None
        KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> TvRemoteAction.TogglePlayback
        KeyEvent.KEYCODE_MEDIA_PLAY -> TvRemoteAction.Play
        KeyEvent.KEYCODE_MEDIA_PAUSE -> TvRemoteAction.Pause
        KeyEvent.KEYCODE_MEDIA_REWIND -> TvRemoteAction.SeekBack
        KeyEvent.KEYCODE_MEDIA_FAST_FORWARD -> TvRemoteAction.SeekForward
        else -> if (controlsVisible) TvRemoteAction.None else hiddenControlsAction(keyCode)
    }

private fun hiddenControlsAction(keyCode: Int): TvRemoteAction =
    when (keyCode) {
        KeyEvent.KEYCODE_DPAD_LEFT -> TvRemoteAction.SeekBack

        KeyEvent.KEYCODE_DPAD_RIGHT -> TvRemoteAction.SeekForward

        KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER,
        KeyEvent.KEYCODE_DPAD_UP, KeyEvent.KEYCODE_DPAD_DOWN,
        -> TvRemoteAction.ShowControls

        else -> TvRemoteAction.None
    }

/** Отпускание кнопки достаётся тому же, кто принял нажатие, даже если это нажатие показало кнопки. */
internal class TvRemoteInput {
    private var pressedKey: Int? = null
    private var pressedAction = TvRemoteAction.None

    fun handle(
        keyCode: Int,
        isDown: Boolean,
        repeatCount: Int,
        controlsVisible: Boolean,
        onAction: (TvRemoteAction) -> Unit,
    ): Boolean {
        if (!isDown) return release(keyCode)
        val action =
            if (repeatCount > 0 &&
                pressedKey == keyCode
            ) {
                pressedAction
            } else {
                tvRemoteAction(keyCode, controlsVisible)
            }
        val handled = action != TvRemoteAction.None
        if (handled) {
            pressedKey = keyCode
            pressedAction = action
            val repeatable = action == TvRemoteAction.SeekBack || action == TvRemoteAction.SeekForward
            if (repeatCount == 0 || repeatable) onAction(action)
        }
        return handled
    }

    private fun release(keyCode: Int): Boolean {
        val handled = pressedKey == keyCode
        if (handled) pressedKey = null
        return handled
    }
}
