package io.github.arenoros.anime365.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
 * Новый Эпизод — номер цвета `primary` и точка в углу.
 * [description] — что прочитает TalkBack вместо голого номера; [onLongClickLabel] — что делает долгое нажатие.
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
    onLongClick: (() -> Unit)? = null,
    onLongClickLabel: String? = null,
) {
    val palette = KadrTheme.palette
    val current = state == EpisodeState.Current
    val color =
        when (state) {
            EpisodeState.Upcoming -> palette.inkDisabled
            EpisodeState.Released -> palette.ink
            EpisodeState.New -> palette.primary
            EpisodeState.Current -> palette.onPrimaryContainer
            EpisodeState.Watched -> palette.inkMuted
        }
    Box(
        modifier
            .heightIn(min = KadrLayout.TouchTarget)
            .tvFocusIndicator()
            .clip(MaterialTheme.shapes.small)
            .background(if (current) palette.primaryContainer else palette.surfaceVariant)
            .then(
                if (onClick != null) {
                    Modifier.combinedClickable(
                        role = Role.Button,
                        onLongClickLabel = onLongClickLabel,
                        onLongClick = onLongClick,
                        onClick = onClick,
                    )
                } else {
                    Modifier
                },
            ).clearAndSetSemantics { contentDescription = description },
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
        StateMark(state, progress)
    }
}

/** Точка Нового Эпизода в углу или полоска позиции внизу. */
@Composable
private fun BoxScope.StateMark(
    state: EpisodeState,
    progress: Float,
) {
    val primary = KadrTheme.palette.primary
    when (state) {
        EpisodeState.New -> {
            Box(
                Modifier
                    .align(Alignment.TopEnd)
                    .padding(KadrSpacing.Space1)
                    .size(6.dp)
                    .background(primary, CircleShape),
            )
        }

        EpisodeState.Current -> {
            Box(
                Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth(progress.coerceIn(0f, 1f))
                    .height(3.dp)
                    .background(primary),
            )
        }

        else -> {}
    }
}
