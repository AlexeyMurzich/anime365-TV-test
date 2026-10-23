package io.github.arenoros.anime365.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import io.github.arenoros.anime365.core.designsystem.theme.KadrTheme
import io.github.arenoros.anime365.core.designsystem.tokens.KadrLayout
import io.github.arenoros.anime365.core.designsystem.tokens.KadrRadius
import io.github.arenoros.anime365.core.designsystem.tokens.KadrSpacing

/**
 * PosterCard Kadr: постер 2:3, рейтинг слева сверху, статус справа снизу, название в две строки.
 * Название всегда занимает две строки, чтобы карточки одного ряда сетки были одной высоты.
 */
@Composable
fun PosterCard(
    imageUrl: String?,
    title: String,
    meta: String?,
    score: Double?,
    status: PosterStatus?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier
            .clip(MaterialTheme.shapes.small)
            .clickable(onClick = onClick),
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .aspectRatio(KadrLayout.PosterRatio)
                .clip(MaterialTheme.shapes.small)
                .background(KadrTheme.palette.surfaceVariant),
        ) {
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop,
            )
            if (score != null) {
                RatingCompact(
                    score = score,
                    color = KadrTheme.palette.onScrim,
                    modifier =
                        Modifier
                            .align(Alignment.TopStart)
                            .padding(KadrSpacing.Space1)
                            .background(KadrTheme.palette.scrim, RoundedCornerShape(KadrRadius.Xs))
                            .padding(horizontal = KadrSpacing.Space1, vertical = 2.dp),
                )
            }
            if (status != null) {
                StatusBadge(
                    status = status,
                    overImage = true,
                    modifier = Modifier.align(Alignment.BottomEnd).padding(KadrSpacing.Space1),
                )
            }
        }
        Text(
            text = title,
            modifier = Modifier.padding(top = KadrSpacing.Space1),
            style = MaterialTheme.typography.titleSmall,
            minLines = 2,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = meta.orEmpty(),
            style = MaterialTheme.typography.bodySmall,
            color = KadrTheme.palette.inkMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** Скелетон той же геометрии, что [PosterCard]. */
@Composable
fun PosterCardPlaceholder(modifier: Modifier = Modifier) {
    Column(modifier) {
        Box(
            Modifier
                .fillMaxWidth()
                .aspectRatio(KadrLayout.PosterRatio)
                .skeleton(),
        )
        Spacer(Modifier.height(KadrSpacing.Space2))
        SkeletonLine(Modifier.fillMaxWidth())
        Spacer(Modifier.height(KadrSpacing.Space1))
        SkeletonLine(Modifier.fillMaxWidth(fraction = 0.6f))
    }
}
