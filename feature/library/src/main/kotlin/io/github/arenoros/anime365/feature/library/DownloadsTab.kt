package io.github.arenoros.anime365.feature.library

import android.text.format.Formatter
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import io.github.arenoros.anime365.core.designsystem.component.EmptyState
import io.github.arenoros.anime365.core.designsystem.icon.KadrIcons
import io.github.arenoros.anime365.core.designsystem.theme.KadrTheme
import io.github.arenoros.anime365.core.designsystem.tokens.KadrLayout
import io.github.arenoros.anime365.core.designsystem.tokens.KadrSpacing
import io.github.arenoros.anime365.core.model.DownloadState
import io.github.arenoros.anime365.core.model.EpisodeDownload
import kotlinx.coroutines.launch

private const val PERCENT = 100

/**
 * Вкладка «Скачанное» (docs/screens.md): сверху — сколько места занято, ниже — группы по аниме.
 * Готовое касанием играет, ошибка касанием повторяется. Удаление — свайпом или выбором нескольких
 * (долгое нажатие), всегда с подтверждением.
 */
@Composable
internal fun DownloadsTab(
    downloads: List<EpisodeDownload>,
    onPlay: (EpisodeDownload) -> Unit,
    onRetry: (EpisodeDownload) -> Unit,
    onRemove: (List<EpisodeDownload>) -> Unit,
    modifier: Modifier = Modifier,
) {
    var selected by remember { mutableStateOf(emptySet<Int>()) }
    var removing by remember { mutableStateOf<List<EpisodeDownload>?>(null) }
    removing?.let { pending ->
        RemoveDialog(
            pending,
            onConfirm = {
                onRemove(pending)
                selected = emptySet()
                removing = null
            },
            onDismiss = { removing = null },
        )
    }
    if (downloads.isEmpty()) {
        EmptyState(stringResource(R.string.feature_library_downloads_empty), modifier)
        return
    }
    Column(modifier.fillMaxSize()) {
        if (selected.isEmpty()) {
            Text(
                stringResource(R.string.feature_library_downloads_used, fileSize(downloads.sumOf { it.size })),
                Modifier.padding(horizontal = KadrSpacing.Space4, vertical = KadrSpacing.Space3),
                style = MaterialTheme.typography.bodyMedium,
                color = KadrTheme.palette.inkMuted,
            )
        } else {
            SelectionBar(
                count = selected.size,
                onRemove = { removing = downloads.filter { it.translation.id in selected } },
                onCancel = { selected = emptySet() },
            )
        }
        LazyColumn(Modifier.fillMaxSize()) {
            downloads.groupBy { it.seriesId }.forEach { (seriesId, group) ->
                item(key = "series-$seriesId") { SeriesHeader(group.first().seriesTitle) }
                items(group, key = { it.translation.id }) { download ->
                    val id = download.translation.id
                    DownloadRow(
                        download,
                        selection = if (selected.isEmpty()) null else id in selected,
                        onClick = {
                            when {
                                selected.isNotEmpty() -> selected = selected.toggle(id)
                                download.state == DownloadState.Completed -> onPlay(download)
                                download.state == DownloadState.Failed -> onRetry(download)
                            }
                        },
                        onLongClick = { selected = selected.toggle(id) },
                        onSwipe = { removing = listOf(download) },
                    )
                }
            }
        }
    }
}

private fun Set<Int>.toggle(id: Int): Set<Int> = if (id in this) this - id else this + id

@Composable
private fun fileSize(bytes: Long): String = Formatter.formatShortFileSize(LocalContext.current, bytes)

@Composable
private fun SeriesHeader(title: String) {
    Text(
        title,
        Modifier
            .fillMaxWidth()
            .padding(start = KadrSpacing.Space4, end = KadrSpacing.Space4, top = KadrSpacing.Space3)
            .semantics { heading() },
        style = MaterialTheme.typography.titleSmall,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
private fun SelectionBar(
    count: Int,
    onRemove: () -> Unit,
    onCancel: () -> Unit,
) {
    Row(
        Modifier.fillMaxWidth().background(KadrTheme.palette.surfaceVariant).padding(start = KadrSpacing.Space4),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            pluralStringResource(R.plurals.feature_library_downloads_selected, count, count),
            Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge,
        )
        TextButton(onClick = onCancel) { Text(stringResource(R.string.feature_library_cancel).uppercase()) }
        TextButton(onClick = onRemove) { Text(stringResource(R.string.feature_library_downloads_remove).uppercase()) }
    }
}

/** [selection] — `null`, пока ничего не выбрано: тогда флажков нет. */
@Composable
private fun DownloadRow(
    download: EpisodeDownload,
    selection: Boolean?,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onSwipe: () -> Unit,
) {
    val swipe = rememberSwipeToDismissBoxState()
    val scope = rememberCoroutineScope()
    SwipeToDismissBox(
        swipe,
        enableDismissFromStartToEnd = false,
        // Строка возвращается на место: уйдёт, только когда удаление подтвердят в диалоге.
        onDismiss = {
            onSwipe()
            scope.launch { swipe.snapTo(SwipeToDismissBoxValue.Settled) }
        },
        backgroundContent = {
            Box(
                Modifier.fillMaxSize().background(KadrTheme.palette.error).padding(horizontal = KadrSpacing.Space4),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Icon(
                    painterResource(KadrIcons.Delete),
                    contentDescription = null,
                    tint = KadrTheme.palette.onError,
                )
            }
        },
    ) {
        DownloadRowContent(download, selection, onClick, onLongClick)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DownloadRowContent(
    download: EpisodeDownload,
    selection: Boolean?,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(KadrTheme.palette.background)
            .heightIn(min = KadrLayout.TouchTarget + KadrSpacing.Space2)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
                onLongClickLabel = stringResource(R.string.feature_library_downloads_select),
            ).padding(horizontal = KadrSpacing.Space4, vertical = KadrSpacing.Space2),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(KadrSpacing.Space3),
    ) {
        if (selection != null) Checkbox(checked = selection, onCheckedChange = null)
        Column(Modifier.weight(1f)) {
            Text(
                listOf(download.episode.label, download.translation.team)
                    .filter { it.isNotEmpty() }
                    .joinToString(" · "),
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                status(download),
                style = MaterialTheme.typography.bodySmall,
                color =
                    if (download.state ==
                        DownloadState.Failed
                    ) {
                        KadrTheme.palette.error
                    } else {
                        KadrTheme.palette.inkMuted
                    },
            )
            if (download.state == DownloadState.Downloading) {
                val progress = download.progress
                if (progress != null) {
                    LinearProgressIndicator(
                        { progress },
                        Modifier.fillMaxWidth().padding(top = KadrSpacing.Space1),
                    )
                } else {
                    LinearProgressIndicator(Modifier.fillMaxWidth().padding(top = KadrSpacing.Space1))
                }
            }
        }
    }
}

/** «720p · 529 МБ», «720p · 43 % из 529 МБ», «Ждёт Wi-Fi»… */
@Composable
private fun status(download: EpisodeDownload): String {
    val height = stringResource(R.string.feature_library_downloads_height, download.height)
    return when (download.state) {
        DownloadState.Completed -> {
            "$height · ${fileSize(download.size)}"
        }

        DownloadState.Downloading -> {
            val progress = download.progress
            val total = download.contentLength
            if (progress != null && total != null) {
                stringResource(
                    R.string.feature_library_downloads_progress,
                    height,
                    (progress * PERCENT).toInt(),
                    fileSize(total),
                )
            } else {
                "$height · ${fileSize(download.bytesDownloaded)}"
            }
        }

        DownloadState.Queued -> {
            stringResource(R.string.feature_library_downloads_queued)
        }

        DownloadState.WaitingForNetwork -> {
            stringResource(R.string.feature_library_downloads_waiting_network)
        }

        DownloadState.WaitingForWifi -> {
            stringResource(R.string.feature_library_downloads_waiting_wifi)
        }

        DownloadState.Failed -> {
            stringResource(R.string.feature_library_downloads_failed)
        }
    }
}

/** «Удалить скачанное?» — что именно и сколько места освободится (docs/screens.md, «Диалоги»). */
@Composable
private fun RemoveDialog(
    downloads: List<EpisodeDownload>,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val size = fileSize(downloads.sumOf { it.size })
    val single = downloads.singleOrNull()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.feature_library_downloads_remove_title)) },
        text = {
            Text(
                if (single != null) {
                    stringResource(
                        R.string.feature_library_downloads_remove_one,
                        single.episode.label,
                        single.seriesTitle,
                        size,
                    )
                } else {
                    pluralStringResource(
                        R.plurals.feature_library_downloads_remove_many,
                        downloads.size,
                        downloads.size,
                        size,
                    )
                },
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.feature_library_downloads_remove).uppercase())
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.feature_library_cancel).uppercase()) }
        },
    )
}
