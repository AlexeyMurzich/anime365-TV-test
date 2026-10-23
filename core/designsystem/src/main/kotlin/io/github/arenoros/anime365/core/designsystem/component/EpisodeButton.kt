package io.github.arenoros.anime365.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.arenoros.anime365.core.designsystem.icon.KadrIcons
import io.github.arenoros.anime365.core.designsystem.theme.KadrTheme
import io.github.arenoros.anime365.core.designsystem.tokens.KadrLayout
import io.github.arenoros.anime365.core.designsystem.tokens.KadrSpacing
import io.github.arenoros.anime365.core.designsystem.tokens.KadrTypography

/**
 * Кнопка EpisodeGrid. [wide] — трейлер, спешл, фильм: подпись с иконкой вместо номера.
 * [progress] — доля просмотренного, рисуется полоской в состоянии [EpisodeState.Current].
 * [description] — что прочитает TalkBack вместо голого номера.
 */
@Composable
fun EpisodeButton(
    label: String,
    description: String,
    state: EpisodeState,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    wide: Boolean = false,
    progress: Float = 0f,
) {
    val palette = KadrTheme.palette
    val current = state == EpisodeState.Current
    val color =
        when (state) {
            EpisodeState.Upcoming -> palette.inkDisabled
            EpisodeState.Released -> palette.ink
            EpisodeState.Current -> palette.onPrimaryContainer
            EpisodeState.Watched -> palette.inkMuted
        }
    Box(
        modifier
            .heightIn(min = KadrLayout.TouchTarget)
            .clip(MaterialTheme.shapes.small)
            .background(if (current) palette.primaryContainer else palette.surfaceVariant)
            .then(if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier)
            .clearAndSetSemantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        Row(
            Modifier.padding(horizontal = KadrSpacing.Space2),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(KadrSpacing.Space1, Alignment.CenterHorizontally),
        ) {
            val icon =
                when {
                    state == EpisodeState.Watched -> KadrIcons.Check
                    wide -> KadrIcons.PlayCircleOutline
                    else -> null
                }
            if (icon != null) {
                Icon(painterResource(icon), contentDescription = null, Modifier.size(18.dp), tint = color)
            }
            Text(
                label,
                color = color,
                style = if (wide) MaterialTheme.typography.labelLarge else KadrTypography.Numeric,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (current) {
            Box(
                Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth(progress.coerceIn(0f, 1f))
                    .height(3.dp)
                    .background(palette.primary),
            )
        }
    }
}
