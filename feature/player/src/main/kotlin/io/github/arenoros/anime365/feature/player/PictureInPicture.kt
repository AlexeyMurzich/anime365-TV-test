package io.github.arenoros.anime365.feature.player

import android.app.PictureInPictureParams
import android.content.pm.ActivityInfo
import android.os.Build
import android.util.Rational
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.app.PictureInPictureModeChangedInfo
import androidx.core.util.Consumer
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

private val VIDEO_RATIO = Rational(16, 9)

/** Свернуть в «картинку в картинке», пока видео играет: по кнопке «Домой» или жесту. */
@Composable
internal fun AutoPictureInPicture(playing: Boolean) {
    val activity = LocalActivity.current as? ComponentActivity ?: return
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        DisposableEffect(activity, playing) {
            val builder = PictureInPictureParams.Builder().setAspectRatio(VIDEO_RATIO)
            activity.setPictureInPictureParams(builder.setAutoEnterEnabled(playing).build())
            onDispose { activity.setPictureInPictureParams(builder.setAutoEnterEnabled(false).build()) }
        }
    } else {
        DisposableEffect(activity, playing) {
            val params = PictureInPictureParams.Builder().setAspectRatio(VIDEO_RATIO).build()
            val listener = Runnable { if (playing) activity.enterPictureInPictureMode(params) }
            activity.addOnUserLeaveHintListener(listener)
            onDispose { activity.removeOnUserLeaveHintListener(listener) }
        }
    }
}

@Composable
internal fun rememberIsInPictureInPicture(): Boolean {
    val activity = LocalActivity.current as? ComponentActivity
    var inPip by remember(activity) { mutableStateOf(activity?.isInPictureInPictureMode == true) }
    DisposableEffect(activity) {
        val listener = Consumer<PictureInPictureModeChangedInfo> { inPip = it.isInPictureInPictureMode }
        activity?.addOnPictureInPictureModeChangedListener(listener)
        onDispose { activity?.removeOnPictureInPictureModeChangedListener(listener) }
    }
    return inPip
}

/** В ландшафте Плеер — во весь экран: системные панели появляются только по свайпу. */
@Composable
internal fun HideSystemBars() {
    val activity = LocalActivity.current ?: return
    DisposableEffect(activity) {
        val controller = WindowCompat.getInsetsController(activity.window, activity.window.decorView)
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        controller.hide(WindowInsetsCompat.Type.systemBars())
        onDispose { controller.show(WindowInsetsCompat.Type.systemBars()) }
    }
}

/**
 * Кнопка полноэкранного режима поворачивает экран; с уходом с Плеера ориентация снова свободна.
 * ponytail: после кнопки поворот закреплён до выхода с Плеера; автоповорот по датчику — если попросят.
 */
@Composable
internal fun rememberOrientationToggle(landscape: Boolean): () -> Unit {
    val activity = LocalActivity.current
    DisposableEffect(activity) {
        onDispose { activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED }
    }
    return {
        activity?.requestedOrientation =
            if (landscape) {
                ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT
            } else {
                ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            }
    }
}
