package io.github.arenoros.anime365.core.player

import android.content.res.Configuration
import android.graphics.Color
import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerView

/**
 * Видео с субтитрами libass, без своих кнопок: управление рисует экран Плеера.
 * PlayerView, а не PlayerSurface из Compose: libass встраивается в его SubtitleView.
 */
@OptIn(UnstableApi::class)
@Composable
fun VideoSurface(
    player: VideoPlayer,
    modifier: Modifier = Modifier,
) {
    AndroidView(
        factory = { context ->
            PlayerView(context).apply {
                useController = false
                setShutterBackgroundColor(Color.BLACK)
                setKeepContentOnPlayerReset(true)
                this.player = player.player
                // TV input belongs to the Compose controls, not PlayerView's hidden controller.
                if (context.resources.configuration.uiMode and Configuration.UI_MODE_TYPE_MASK ==
                    Configuration.UI_MODE_TYPE_TELEVISION
                ) {
                    isFocusable = false
                    isFocusableInTouchMode = false
                    descendantFocusability = ViewGroup.FOCUS_BLOCK_DESCENDANTS
                }
                // Субтитры libass есть только у настоящего плеера, не у тестового.
                (player as? AnimePlayer)?.let { animePlayer -> subtitleView?.let(animePlayer::attach) }
            }
        },
        modifier = modifier,
        onRelease = { it.player = null },
    )
}
