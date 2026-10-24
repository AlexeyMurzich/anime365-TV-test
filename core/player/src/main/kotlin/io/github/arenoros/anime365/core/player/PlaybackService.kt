package io.github.arenoros.anime365.core.player

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import androidx.annotation.OptIn
import androidx.annotation.StringRes
import androidx.core.content.IntentCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.CommandButton
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

private const val ACTION_PREVIOUS_EPISODE = "io.github.arenoros.anime365.PREVIOUS_EPISODE"
private const val ACTION_NEXT_EPISODE = "io.github.arenoros.anime365.NEXT_EPISODE"
private val PREVIOUS_EPISODE = SessionCommand(ACTION_PREVIOUS_EPISODE, Bundle.EMPTY)
private val NEXT_EPISODE = SessionCommand(ACTION_NEXT_EPISODE, Bundle.EMPTY)

/**
 * Держит [MediaSession] над плеером: уведомление, гарнитура и звук, пока приложение свёрнуто.
 * ⏮ и ⏭ в уведомлении и на гарнитуре — соседние серии, а не начало файла: в плейлисте всегда один Эпизод.
 * Кнопки уведомления по слотам и обработка гарнитуры в Media3 пока нестабильны.
 */
@OptIn(UnstableApi::class)
@AndroidEntryPoint
class PlaybackService : MediaSessionService() {
    @Inject
    internal lateinit var animePlayer: AnimePlayer

    private var session: MediaSession? = null

    // У нового Эпизода свои соседи: кнопки уведомления перестраиваются.
    private val mediaListener =
        object : Player.Listener {
            override fun onMediaItemTransition(
                mediaItem: MediaItem?,
                reason: Int,
            ) {
                session?.setMediaButtonPreferences(episodeButtons())
            }
        }

    override fun onCreate() {
        super.onCreate()
        val builder =
            MediaSession
                .Builder(this, animePlayer.player)
                .setCallback(EpisodeCallback())
                .setMediaButtonPreferences(episodeButtons())
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
        animePlayer.player.addListener(mediaListener)
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = session

    /** Приложение смахнули из недавних — звук не должен играть дальше без экрана. */
    override fun onTaskRemoved(rootIntent: Intent?) {
        pauseAllPlayersAndStopSelf()
    }

    override fun onDestroy() {
        // Плеер общий с экраном и живёт дольше сервиса: освобождаем только сессию.
        animePlayer.player.removeListener(mediaListener)
        session?.release()
        session = null
        animePlayer.onServiceDestroyed()
        super.onDestroy()
    }

    /** Нет соседней серии — нет и кнопки. */
    private fun episodeButtons(): List<CommandButton> {
        val media = animePlayer.currentMedia
        return listOfNotNull(
            if (media?.hasPreviousEpisode == true) {
                button(CommandButton.ICON_PREVIOUS, R.string.core_player_previous_episode, PREVIOUS_EPISODE)
            } else {
                null
            },
            if (media?.hasNextEpisode == true) {
                button(CommandButton.ICON_NEXT, R.string.core_player_next_episode, NEXT_EPISODE)
            } else {
                null
            },
        )
    }

    private fun button(
        icon: Int,
        @StringRes name: Int,
        command: SessionCommand,
    ): CommandButton =
        CommandButton
            .Builder(icon)
            .setDisplayName(getString(name))
            .setSessionCommand(command)
            // Места «назад» и «вперёд» по бокам от паузы.
            .setSlots(if (command == PREVIOUS_EPISODE) CommandButton.SLOT_BACK else CommandButton.SLOT_FORWARD)
            .build()

    private inner class EpisodeCallback : MediaSession.Callback {
        // Перемотка в начало и по плейлисту убрана: её места в уведомлении занимают соседние серии.
        override fun onConnect(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
        ): MediaSession.ConnectionResult {
            val trusted = controller.isTrusted
            val sessionCommands =
                if (trusted) {
                    MediaSession.ConnectionResult.DEFAULT_SESSION_COMMANDS
                } else {
                    MediaSession.ConnectionResult.DEFAULT_UNTRUSTED_SESSION_COMMANDS
                }
            val playerCommands =
                if (trusted) {
                    MediaSession.ConnectionResult.DEFAULT_PLAYER_COMMANDS
                } else {
                    MediaSession.ConnectionResult.DEFAULT_UNTRUSTED_PLAYER_COMMANDS
                }
            return MediaSession.ConnectionResult
                .AcceptedResultBuilder(session, controller)
                .setAvailableSessionCommands(
                    sessionCommands
                        .buildUpon()
                        .add(PREVIOUS_EPISODE)
                        .add(NEXT_EPISODE)
                        .build(),
                ).setAvailablePlayerCommands(
                    playerCommands
                        .buildUpon()
                        .removeAll(
                            Player.COMMAND_SEEK_TO_PREVIOUS,
                            Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM,
                            Player.COMMAND_SEEK_TO_NEXT,
                            Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM,
                        ).build(),
                ).build()
        }

        override fun onCustomCommand(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            customCommand: SessionCommand,
            args: Bundle,
        ): ListenableFuture<SessionResult> {
            when (customCommand.customAction) {
                ACTION_PREVIOUS_EPISODE -> animePlayer.skip(EpisodeSkip.Previous)
                ACTION_NEXT_EPISODE -> animePlayer.skip(EpisodeSkip.Next)
            }
            return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
        }

        /** Кнопки гарнитуры «дальше» и «назад» — тоже соседние серии. */
        override fun onMediaButtonEvent(
            session: MediaSession,
            controllerInfo: MediaSession.ControllerInfo,
            intent: Intent,
        ): Boolean {
            val event = IntentCompat.getParcelableExtra(intent, Intent.EXTRA_KEY_EVENT, KeyEvent::class.java)
            val skip =
                when (event?.keyCode) {
                    KeyEvent.KEYCODE_MEDIA_PREVIOUS -> EpisodeSkip.Previous
                    KeyEvent.KEYCODE_MEDIA_NEXT -> EpisodeSkip.Next
                    else -> null
                }
            // Одно нажатие — одна серия: отпускание и автоповтор только поглощаются.
            val pressed = event?.action == KeyEvent.ACTION_DOWN && event.repeatCount == 0
            if (skip != null && pressed) animePlayer.skip(skip)
            return skip != null
        }
    }
}
