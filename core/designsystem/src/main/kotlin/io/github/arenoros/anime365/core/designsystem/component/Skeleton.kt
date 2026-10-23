package io.github.arenoros.anime365.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.arenoros.anime365.core.designsystem.theme.KadrTheme

/** Скелетон загрузки: `surface-variant` той же формы, что и будущий контент. Без спиннеров. */
@Composable
fun Modifier.skeleton(shape: Shape = MaterialTheme.shapes.small): Modifier =
    background(KadrTheme.palette.surfaceVariant, shape)

@Composable
fun SkeletonLine(
    modifier: Modifier = Modifier,
    height: Dp = 12.dp,
) {
    Box(modifier.height(height).skeleton(MaterialTheme.shapes.extraSmall))
}
