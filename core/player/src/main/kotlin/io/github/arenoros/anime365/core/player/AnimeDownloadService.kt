package io.github.arenoros.anime365.core.player

import android.app.Notification
import android.app.PendingIntent
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.offline.DownloadNotificationHelper
import androidx.media3.exoplayer.offline.DownloadService
import androidx.media3.exoplayer.scheduler.PlatformScheduler
import androidx.media3.exoplayer.scheduler.Scheduler
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

private const val NOTIFICATION_ID = 365
private const val CHANNEL_ID = "downloads"
private const val JOB_ID = 365

/**
 * Скачивание в фоне с уведомлением о прогрессе. Нет сети или Wi-Fi — останавливается, а когда они
 * появятся, JobScheduler запустит его снова.
 */
@OptIn(UnstableApi::class)
@AndroidEntryPoint
class AnimeDownloadService :
    DownloadService(
        NOTIFICATION_ID,
        DEFAULT_FOREGROUND_NOTIFICATION_UPDATE_INTERVAL,
        CHANNEL_ID,
        R.string.core_player_downloads_channel,
        0,
    ) {
    @Inject
    internal lateinit var downloads: MediaDownloads

    private val notifications by lazy { DownloadNotificationHelper(this, CHANNEL_ID) }

    override fun getDownloadManager(): DownloadManager = downloads.manager

    override fun getScheduler(): Scheduler = PlatformScheduler(this, JOB_ID)

    override fun getForegroundNotification(
        downloads: MutableList<Download>,
        notMetRequirements: Int,
    ): Notification =
        notifications.buildProgressNotification(
            this,
            R.drawable.core_player_ic_download,
            // Касание уведомления открывает приложение там, где его оставили.
            packageManager.getLaunchIntentForPackage(packageName)?.let { launch ->
                PendingIntent.getActivity(this, 0, launch, PendingIntent.FLAG_IMMUTABLE)
            },
            null,
            downloads,
            notMetRequirements,
        )
}
