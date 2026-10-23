package io.github.arenoros.anime365.core.designsystem.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.arenoros.anime365.core.designsystem.tokens.KadrLayout

private val MEDIUM_WIDTH = 600.dp
private val EXPANDED_WIDTH = 840.dp

/** Сетка постеров Kadr: 3 колонки на телефоне, 5 от 600dp, 6 от 840dp. */
@Suppress("MagicNumber")
fun posterColumns(width: Dp): Int =
    when {
        width >= EXPANDED_WIDTH -> 6
        width >= MEDIUM_WIDTH -> 5
        else -> KadrLayout.GridColumnsPhone
    }

/** EpisodeGrid: 4 колонки на телефоне, 8 на планшете. */
@Suppress("MagicNumber")
fun episodeColumns(width: Dp): Int = if (width >= MEDIUM_WIDTH) 8 else 4
