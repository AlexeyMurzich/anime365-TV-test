package io.github.arenoros.anime365.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.arenoros.anime365.core.designsystem.R
import io.github.arenoros.anime365.core.designsystem.theme.KadrTheme
import io.github.arenoros.anime365.core.designsystem.tokens.KadrRadius

/** Статусы Kadr: `ongoing`, `done`, `announce`. */
enum class PosterStatus { Ongoing, Released, Announced }

/** [overImage] — поверх постера: всё, кроме «Онгоинга», на `scrim`. */
@Composable
fun StatusBadge(
    status: PosterStatus,
    modifier: Modifier = Modifier,
    overImage: Boolean = false,
) {
    val palette = KadrTheme.palette
    val (background, content) =
        when {
            status == PosterStatus.Ongoing -> palette.ongoing to palette.onOngoing
            overImage -> palette.scrim to palette.onScrim
            status == PosterStatus.Released -> palette.primaryContainer to palette.onPrimaryContainer
            else -> palette.surfaceVariant to palette.inkMuted
        }
    val label =
        when (status) {
            PosterStatus.Ongoing -> R.string.core_designsystem_status_ongoing
            PosterStatus.Released -> R.string.core_designsystem_status_released
            PosterStatus.Announced -> R.string.core_designsystem_status_announced
        }
    val style = MaterialTheme.typography.labelMedium
    // Поверх постера места столько, сколько у постера: бейдж растёт со шрифтом не больше чем в полтора раза.
    val fontScale = LocalDensity.current.fontScale
    val fontSize =
        if (overImage &&
            fontScale > MAX_FONT_SCALE_OVER_IMAGE
        ) {
            style.fontSize * (MAX_FONT_SCALE_OVER_IMAGE / fontScale)
        } else {
            style.fontSize
        }
    Text(
        text = stringResource(label),
        modifier =
            modifier
                .background(background, RoundedCornerShape(KadrRadius.Xs))
                .padding(horizontal = 6.dp, vertical = 2.dp),
        color = content,
        fontSize = fontSize,
        style = style,
        // Бейдж — одно слово: перенос «Онгоин / г» хуже любого другого исхода.
        maxLines = 1,
        softWrap = false,
    )
}

private const val MAX_FONT_SCALE_OVER_IMAGE = 1.5f
