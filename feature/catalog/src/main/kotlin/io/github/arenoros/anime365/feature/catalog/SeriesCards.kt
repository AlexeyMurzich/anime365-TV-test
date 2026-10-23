package io.github.arenoros.anime365.feature.catalog

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import io.github.arenoros.anime365.core.designsystem.component.PosterCard
import io.github.arenoros.anime365.core.designsystem.component.PosterStatus
import io.github.arenoros.anime365.core.model.ReleaseStatus
import io.github.arenoros.anime365.core.model.SeriesSummary

/** PosterCard для аниме: мета — «ТВ сериал · 28 эп. · 2023». */
@Composable
internal fun SeriesPosterCard(
    series: SeriesSummary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val episodes = series.episodeCount?.let { stringResource(R.string.feature_catalog_episodes_count, it) }
    PosterCard(
        imageUrl = series.posterUrl,
        title = series.title,
        meta = listOfNotNull(series.kind, episodes, series.year?.toString()).joinToString(" · "),
        score = series.score,
        status = series.status.toPosterStatus(),
        onClick = onClick,
        modifier = modifier,
    )
}

internal fun ReleaseStatus.toPosterStatus(): PosterStatus =
    when (this) {
        ReleaseStatus.Ongoing -> PosterStatus.Ongoing
        ReleaseStatus.Released -> PosterStatus.Released
        ReleaseStatus.Announced -> PosterStatus.Announced
    }
