package io.github.arenoros.anime365.feature.player

import android.view.KeyEvent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TvRemoteActionTest {
    @Test
    fun revealingControlsConsumesTheMatchingReleaseAndHeldOk() {
        val input = TvRemoteInput()
        val actions = mutableListOf<TvRemoteAction>()
        assertTrue(input.handle(KeyEvent.KEYCODE_DPAD_CENTER, true, 0, false, actions::add))
        assertTrue(input.handle(KeyEvent.KEYCODE_DPAD_CENTER, true, 1, true, actions::add))
        assertTrue(input.handle(KeyEvent.KEYCODE_DPAD_CENTER, false, 0, true, actions::add))
        assertEquals(listOf(TvRemoteAction.ShowControls), actions)
        assertFalse(input.handle(KeyEvent.KEYCODE_DPAD_CENTER, true, 0, true, actions::add))
    }

    @Test
    fun holdingSeekContinuesAfterOverlayAppearsWithoutRepeatingPause() {
        val input = TvRemoteInput()
        val actions = mutableListOf<TvRemoteAction>()
        input.handle(KeyEvent.KEYCODE_DPAD_RIGHT, true, 0, false, actions::add)
        input.handle(KeyEvent.KEYCODE_DPAD_RIGHT, true, 1, true, actions::add)
        input.handle(KeyEvent.KEYCODE_DPAD_RIGHT, false, 0, true, actions::add)
        input.handle(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, true, 0, true, actions::add)
        input.handle(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, true, 1, true, actions::add)
        assertEquals(
            listOf(TvRemoteAction.SeekForward, TvRemoteAction.SeekForward, TvRemoteAction.TogglePlayback),
            actions,
        )
    }

    @Test
    fun backHidesControlsAndConsumesReleaseBeforeAllowingExit() {
        val input = TvRemoteInput()
        val actions = mutableListOf<TvRemoteAction>()
        assertTrue(input.handle(KeyEvent.KEYCODE_BACK, true, 0, true, actions::add))
        assertTrue(input.handle(KeyEvent.KEYCODE_BACK, true, 1, false, actions::add))
        assertTrue(input.handle(KeyEvent.KEYCODE_BACK, false, 0, false, actions::add))
        assertEquals(listOf(TvRemoteAction.HideControls), actions)
        assertFalse(input.handle(KeyEvent.KEYCODE_BACK, true, 0, false, actions::add))
        assertFalse(input.handle(KeyEvent.KEYCODE_BACK, false, 0, false, actions::add))
    }

    @Test
    fun hiddenDirectionsSeekAndOkOnlyRevealsControls() {
        assertEquals(TvRemoteAction.SeekBack, tvRemoteAction(KeyEvent.KEYCODE_DPAD_LEFT, false))
        assertEquals(TvRemoteAction.SeekForward, tvRemoteAction(KeyEvent.KEYCODE_DPAD_RIGHT, false))
        assertEquals(TvRemoteAction.ShowControls, tvRemoteAction(KeyEvent.KEYCODE_DPAD_CENTER, false))
        assertEquals(TvRemoteAction.ShowControls, tvRemoteAction(KeyEvent.KEYCODE_DPAD_UP, false))
    }

    @Test
    fun visibleControlsKeepNormalFocusNavigationAndButtonActivation() {
        listOf(
            KeyEvent.KEYCODE_DPAD_LEFT,
            KeyEvent.KEYCODE_DPAD_RIGHT,
            KeyEvent.KEYCODE_DPAD_UP,
            KeyEvent.KEYCODE_DPAD_DOWN,
            KeyEvent.KEYCODE_DPAD_CENTER,
            KeyEvent.KEYCODE_ENTER,
        ).forEach {
            assertEquals(TvRemoteAction.None, tvRemoteAction(it, true))
        }
    }

    @Test
    fun mediaKeysWorkWithAndWithoutOverlay() {
        listOf(false, true).forEach { visible ->
            assertEquals(TvRemoteAction.TogglePlayback, tvRemoteAction(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, visible))
            assertEquals(TvRemoteAction.Play, tvRemoteAction(KeyEvent.KEYCODE_MEDIA_PLAY, visible))
            assertEquals(TvRemoteAction.Pause, tvRemoteAction(KeyEvent.KEYCODE_MEDIA_PAUSE, visible))
            assertEquals(TvRemoteAction.SeekBack, tvRemoteAction(KeyEvent.KEYCODE_MEDIA_REWIND, visible))
            assertEquals(TvRemoteAction.SeekForward, tvRemoteAction(KeyEvent.KEYCODE_MEDIA_FAST_FORWARD, visible))
        }
    }

    @Test
    fun volumeAndUnrelatedKeysAreNotIntercepted() {
        listOf(false, true).forEach { visible ->
            listOf(
                KeyEvent.KEYCODE_VOLUME_UP,
                KeyEvent.KEYCODE_VOLUME_DOWN,
                KeyEvent.KEYCODE_HOME,
                KeyEvent.KEYCODE_A,
            ).forEach {
                assertEquals(TvRemoteAction.None, tvRemoteAction(it, visible))
            }
        }
    }
}
