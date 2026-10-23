package io.github.arenoros.anime365.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.arenoros.anime365.core.designsystem.R
import io.github.arenoros.anime365.core.designsystem.icon.KadrIcons
import io.github.arenoros.anime365.core.designsystem.theme.KadrTheme
import io.github.arenoros.anime365.core.designsystem.tokens.KadrSpacing
import io.github.arenoros.anime365.core.designsystem.tokens.KadrTypography
import java.util.Locale

/** Рейтинг Kadr: одна цифра после точки — `9.2`. */
fun formatScore(score: Double): String = String.format(Locale.ROOT, "%.1f", score)

@Composable
fun scoreDescription(score: Double?): String =
    if (score == null) {
        stringResource(R.string.core_designsystem_no_score)
    } else {
        stringResource(R.string.core_designsystem_score_description, formatScore(score))
    }

/** Компактный рейтинг для списков. [color] — `star`, поверх постера — `on-scrim`. */
@Composable
fun RatingCompact(
    score: Double?,
    modifier: Modifier = Modifier,
    color: Color = KadrTheme.palette.star,
) {
    val description = scoreDescription(score)
    Row(
        modifier.clearAndSetSemantics { contentDescription = description },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Icon(
            painterResource(if (score == null) KadrIcons.StarBorder else KadrIcons.Star),
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = if (score == null) KadrTheme.palette.inkMuted else KadrTheme.palette.star,
        )
        Text(
            text = score?.let(::formatScore) ?: "—",
            style = KadrTypography.Numeric,
            color = if (score == null) KadrTheme.palette.inkMuted else color,
        )
    }
}

/** Крупный рейтинг экрана Аниме: число и пять звёзд, каждая — два балла. Числа оценок в API нет. */
@Composable
fun RatingLarge(
    score: Double,
    modifier: Modifier = Modifier,
) {
    val description = scoreDescription(score)
    Row(
        modifier.clearAndSetSemantics { contentDescription = description },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(KadrSpacing.Space2),
    ) {
        Text(formatScore(score), fontSize = 32.sp, lineHeight = 36.sp, color = KadrTheme.palette.star)
        Row {
            @Suppress("MagicNumber")
            val filled = Math.round(score / 2).toInt()
            repeat(STARS) { index ->
                Icon(
                    painterResource(if (index < filled) KadrIcons.Star else KadrIcons.StarBorder),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = KadrTheme.palette.star,
                )
            }
        }
        Text(
            stringResource(R.string.core_designsystem_score_out_of_ten),
            style = MaterialTheme.typography.bodySmall,
            color = KadrTheme.palette.inkMuted,
        )
    }
}

private const val STARS = 5
