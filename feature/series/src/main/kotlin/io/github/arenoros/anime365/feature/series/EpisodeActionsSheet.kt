package io.github.arenoros.anime365.feature.series

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
import io.github.arenoros.anime365.core.designsystem.icon.KadrIcons
import io.github.arenoros.anime365.core.designsystem.tokens.KadrLayout
import io.github.arenoros.anime365.core.designsystem.tokens.KadrSpacing
import io.github.arenoros.anime365.core.model.Episode

/** Долгое нажатие на серию в сетке; «Скачать» добавится вместе со Скачиванием (docs/roadmap.md, M4). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun EpisodeActionsSheet(
    episode: Episode,
    watched: Boolean,
    onSetWatched: (Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        EpisodeActions(episode, watched, onSetWatched, Modifier.navigationBarsPadding())
    }
}

@Composable
internal fun EpisodeActions(
    episode: Episode,
    watched: Boolean,
    onSetWatched: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxWidth().padding(bottom = KadrSpacing.Space4)) {
        Text(
            episode.label,
            Modifier.padding(horizontal = KadrSpacing.Space4).semantics { heading() },
            style = MaterialTheme.typography.titleLarge,
        )
        Row(
            Modifier
                .fillMaxWidth()
                .heightIn(min = KadrLayout.TouchTarget)
                .clickable(role = Role.Button, onClick = { onSetWatched(!watched) })
                .padding(horizontal = KadrSpacing.Space4),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(KadrSpacing.Space4),
        ) {
            Icon(painterResource(if (watched) KadrIcons.Close else KadrIcons.Check), contentDescription = null)
            Text(
                stringResource(
                    if (watched) R.string.feature_series_unmark_watched else R.string.feature_series_mark_watched,
                ),
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}
