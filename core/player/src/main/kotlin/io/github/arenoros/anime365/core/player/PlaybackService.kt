package io.github.arenoros.anime365.core.player

import android.app.PendingIntent
import android.content.Intent
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/** Держит [MediaSession] над плеером: уведомление, гарнитура и звук, пока приложение свёрнуто. */
@AndroidEntryPoint
class PlaybackService : MediaSessionService() {
    @Inject
    internal lateinit var animePlayer: AnimePlayer

    private var session: MediaSession? = null

    override fun onCreate() {
        super.onCreate()
        val builder = MediaSession.Builder(this, animePlayer.player)
        // Касание уведомления возвращает в приложение — на экран Плеера, где оно и было.
        packageManager.getLaunchIntentForPackage(packageName)?.let { launch ->
            builder.setSessionActivity(
                PendingIntent.getActivity(
                    this,
                    0,
                    launch,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
                ),
            )
        }
        // Сервис запускается startService, а не через MediaController: сессию добавляем сами.
        session = builder.build().also(::addSession)
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = session

    /** Приложение смахнули из недавних — звук не должен играть дальше без экрана. */
    @OptIn(UnstableApi::class)
    override fun onTaskRemoved(rootIntent: Intent?) {
        pauseAllPlayersAndStopSelf()
    }

    override fun onDestroy() {
        // Плеер общий с экраном и живёт дольше сервиса: освобождаем только сессию.
        session?.release()
        session = null
        animePlayer.onServiceDestroyed()
        super.onDestroy()
    }
}
