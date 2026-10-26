package io.github.arenoros.anime365.feature.series

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import io.github.arenoros.anime365.core.designsystem.component.tvAwareSheetDragHandle
import io.github.arenoros.anime365.core.designsystem.component.tvFocusIndicator
import io.github.arenoros.anime365.core.designsystem.icon.KadrIcons
import io.github.arenoros.anime365.core.designsystem.tokens.KadrLayout
import io.github.arenoros.anime365.core.designsystem.tokens.KadrSpacing
import io.github.arenoros.anime365.core.model.Episode

/**
 * Долгое нажатие на серию в сетке (docs/screens.md): Скачать, Скачать эту и следующие — если после неё
 * есть серии того же типа, — и отметка «просмотрено».
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun EpisodeActionsSheet(
    episode: Episode,
    watched: Boolean,
    hasFollowing: Boolean,
    onDownload: (following: Boolean) -> Unit,
    onSetWatched: (Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(dragHandle = tvAwareSheetDragHandle(), onDismissRequest = onDismiss) {
        EpisodeActions(episode, watched, hasFollowing, onDownload, onSetWatched, Modifier.navigationBarsPadding())
    }
}

@Composable
internal fun EpisodeActions(
    episode: Episode,
    watched: Boolean,
    hasFollowing: Boolean,
    onDownload: (following: Boolean) -> Unit,
    onSetWatched: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxWidth().padding(bottom = KadrSpacing.Space4)) {
        Text(
            episode.label,
            Modifier.padding(horizontal = KadrSpacing.Space4).semantics { heading() },
            style = MaterialTheme.typography.titleLarge,
        )
        ActionRow(KadrIcons.Download, stringResource(R.string.feature_series_download)) { onDownload(false) }
        if (hasFollowing) {
            ActionRow(KadrIcons.Download, stringResource(R.string.feature_series_download_following)) {
                onDownload(true)
            }
        }
        ActionRow(
            if (watched) KadrIcons.Close else KadrIcons.Check,
            stringResource(
                if (watched) R.string.feature_series_unmark_watched else R.string.feature_series_mark_watched,
            ),
        ) { onSetWatched(!watched) }
    }
}

@Composable
private fun ActionRow(
    @DrawableRes icon: Int,
    label: String,
    onClick: () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = KadrLayout.TouchTarget)
            .tvFocusIndicator()
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = KadrSpacing.Space4),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(KadrSpacing.Space4),
    ) {
        Icon(painterResource(icon), contentDescription = null)
        Text(label, style = MaterialTheme.typography.bodyLarge)
    }
}
