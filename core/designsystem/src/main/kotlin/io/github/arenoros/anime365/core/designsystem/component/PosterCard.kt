package io.github.arenoros.anime365.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
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
 * [progress] — полоска позиции внизу постера: карточка «Продолжить просмотр».
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
    progress: Float? = null,
) {
    Column(
        modifier
            .clip(MaterialTheme.shapes.small)
            .clickable(onClick = onClick),
    ) {
        PosterArt(imageUrl, score, status, progress)
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

/** Постер 2:3 с рейтингом, статусом и полоской позиции. */
@Composable
private fun PosterArt(
    imageUrl: String?,
    score: Double?,
    status: PosterStatus?,
    progress: Float?,
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
        if (progress != null) {
            val track = Modifier.align(Alignment.BottomStart).fillMaxWidth().height(4.dp)
            Box(track.background(KadrTheme.palette.scrim)) {
                Box(
                    Modifier
                        .fillMaxWidth(
                            progress.coerceIn(0f, 1f),
                        ).fillMaxHeight()
                        .background(KadrTheme.palette.primary),
                )
            }
        }
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
