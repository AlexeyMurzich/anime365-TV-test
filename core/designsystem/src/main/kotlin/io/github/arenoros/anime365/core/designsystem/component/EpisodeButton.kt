package io.github.arenoros.anime365.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
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
 * [released] `false` — Эпизод ещё не вышел. Остальные состояния Kadr (просмотрен, текущий, новый)
 * появятся вместе с Прогрессом. [description] — что прочитает TalkBack вместо голого номера.
 */
@Composable
fun EpisodeButton(
    label: String,
    description: String,
    released: Boolean,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    wide: Boolean = false,
) {
    val palette = KadrTheme.palette
    Row(
        modifier
            .heightIn(min = KadrLayout.TouchTarget)
            .clip(MaterialTheme.shapes.small)
            .background(palette.surfaceVariant)
            .then(if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier)
            .clearAndSetSemantics { contentDescription = description }
            .padding(horizontal = KadrSpacing.Space2),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(KadrSpacing.Space2, Alignment.CenterHorizontally),
    ) {
        val color = if (released) palette.ink else palette.inkDisabled
        if (wide) {
            Icon(
                painterResource(KadrIcons.PlayCircleOutline),
                contentDescription = null,
                Modifier.size(20.dp),
                tint = color,
            )
        }
        Text(
            label,
            color = color,
            style = if (wide) MaterialTheme.typography.labelLarge else KadrTypography.Numeric,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
