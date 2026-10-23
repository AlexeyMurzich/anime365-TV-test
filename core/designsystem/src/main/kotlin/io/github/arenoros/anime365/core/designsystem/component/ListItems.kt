package io.github.arenoros.anime365.core.designsystem.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Badge
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import io.github.arenoros.anime365.core.designsystem.R
import io.github.arenoros.anime365.core.designsystem.icon.KadrIcons
import io.github.arenoros.anime365.core.designsystem.theme.KadrTheme
import io.github.arenoros.anime365.core.designsystem.tokens.KadrLayout
import io.github.arenoros.anime365.core.designsystem.tokens.KadrSpacing
import io.github.arenoros.anime365.core.designsystem.tokens.KadrTypography

/** RankingListItem Kadr: место, миниатюра, названия, рейтинг. Студии в API нет. */
@Composable
fun RankingListItem(
    place: Int,
    imageUrl: String?,
    title: String,
    originalTitle: String?,
    score: Double?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val placeDescription = stringResource(R.string.core_designsystem_place_description, place)
    Row(
        modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = KadrSpacing.Space4, vertical = KadrSpacing.Space3),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(KadrSpacing.Space3),
    ) {
        Text(
            place.toString(),
            Modifier.widthIn(min = 24.dp).semantics { contentDescription = placeDescription },
            style = KadrTypography.Numeric.copy(fontSize = 16.sp),
            color = KadrTheme.palette.inkMuted,
        )
        Thumbnail(imageUrl, width = 56.dp)
        TitleColumn(title, originalTitle, Modifier.weight(1f))
        RatingCompact(score)
    }
}

/**
 * Строка Ленты «Новые серии»: подпись Эпизода цветом `primary` и название аниме,
 * справа — время выхода.
 */
@Composable
fun EpisodeFeedRow(
    imageUrl: String?,
    episodeLabel: String,
    title: String,
    time: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    markLabel: String? = null,
) {
    val primary = KadrTheme.palette.primary
    Row(
        modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = KadrSpacing.Space4, vertical = KadrSpacing.Space2),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(KadrSpacing.Space3),
    ) {
        Thumbnail(imageUrl, width = 40.dp)
        Text(
            buildAnnotatedString {
                withStyle(SpanStyle(color = primary)) { append(episodeLabel) }
                append(" · ")
                append(title)
            },
            Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        if (markLabel != null) {
            Icon(
                painterResource(KadrIcons.Bookmark),
                contentDescription = markLabel,
                Modifier.size(18.dp),
                tint = primary,
            )
        }
        Text(
            time,
            style = MaterialTheme.typography.bodySmall.copy(fontFeatureSettings = "tnum"),
            color = KadrTheme.palette.inkMuted,
        )
    }
}

/**
 * Строка Списка: миниатюра, название, подпись, бейдж и кнопка действия справа.
 * [badgeDescription] читает TalkBack вместо голого числа в [badge].
 */
@Composable
fun LibraryListItem(
    imageUrl: String?,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    @DrawableRes actionIcon: Int,
    actionLabel: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
    badge: String? = null,
    badgeDescription: String? = null,
) {
    Row(
        modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(
                start = KadrSpacing.Space4,
                end = KadrSpacing.Space2,
                top = KadrSpacing.Space2,
                bottom = KadrSpacing.Space2,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(KadrSpacing.Space3),
    ) {
        Thumbnail(imageUrl, width = 56.dp)
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = KadrTheme.palette.inkMuted)
        }
        if (badge != null) {
            Badge(
                Modifier.semantics { badgeDescription?.let { contentDescription = it } },
                containerColor = KadrTheme.palette.primary,
                contentColor = KadrTheme.palette.onPrimary,
            ) { Text(badge) }
        }
        IconButton(onClick = onAction) {
            Icon(painterResource(actionIcon), contentDescription = actionLabel)
        }
    }
}

@Composable
private fun Thumbnail(
    imageUrl: String?,
    width: Dp,
) {
    Box(
        Modifier
            .width(width)
            .aspectRatio(KadrLayout.PosterRatio)
            .clip(MaterialTheme.shapes.small)
            .background(KadrTheme.palette.surfaceVariant),
    ) {
        AsyncImage(imageUrl, contentDescription = null, Modifier.matchParentSize(), contentScale = ContentScale.Crop)
    }
}

@Composable
private fun TitleColumn(
    title: String,
    originalTitle: String?,
    modifier: Modifier,
) {
    Column(modifier) {
        Text(title, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
        if (originalTitle != null) {
            Text(
                originalTitle,
                style = MaterialTheme.typography.bodySmall,
                color = KadrTheme.palette.inkMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/** Скелетон строки списка с миниатюрой шириной [thumbnailWidth]. */
@Composable
fun ListItemPlaceholder(
    thumbnailWidth: Dp,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier.fillMaxWidth().padding(horizontal = KadrSpacing.Space4, vertical = KadrSpacing.Space2),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(KadrSpacing.Space3),
    ) {
        Box(Modifier.width(thumbnailWidth).aspectRatio(KadrLayout.PosterRatio).skeleton())
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(KadrSpacing.Space2)) {
            SkeletonLine(Modifier.fillMaxWidth(fraction = 0.8f))
            SkeletonLine(Modifier.fillMaxWidth(fraction = 0.5f))
        }
    }
}
