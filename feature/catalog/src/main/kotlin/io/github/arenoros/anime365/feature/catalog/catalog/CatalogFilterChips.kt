package io.github.arenoros.anime365.feature.catalog.catalog

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import io.github.arenoros.anime365.core.designsystem.icon.KadrIcons
import io.github.arenoros.anime365.core.designsystem.theme.KadrTheme
import io.github.arenoros.anime365.core.designsystem.tokens.KadrLayout
import io.github.arenoros.anime365.core.designsystem.tokens.KadrSpacing
import io.github.arenoros.anime365.core.model.CatalogFilter
import io.github.arenoros.anime365.core.model.GenreMatch
import io.github.arenoros.anime365.core.model.Season
import io.github.arenoros.anime365.core.model.SeriesType
import io.github.arenoros.anime365.feature.catalog.R
import java.text.Collator
import java.time.LocalDate
import java.util.Locale

private enum class FilterSheet { Types, Year, Season, Genres }

// Самый ранний год в каталоге сайта.
private const val FIRST_YEAR = 1958

/** Чипы фильтров: выбранные видны всегда, «Сбросить» — одним касанием (docs/screens.md, «Каталог»). */
@Composable
internal fun CatalogFilterChips(
    filter: CatalogFilter,
    onFilterChange: (CatalogFilter) -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var sheet by rememberSaveable { mutableStateOf<FilterSheet?>(null) }
    Row(
        modifier.horizontalScroll(rememberScrollState()).padding(horizontal = KadrSpacing.Space4),
        horizontalArrangement = Arrangement.spacedBy(KadrSpacing.Space2),
    ) {
        DropdownChip(
            label =
                if (filter.types.isEmpty()) {
                    stringResource(R.string.feature_catalog_filter_type)
                } else {
                    filter.types
                        .sorted()
                        .map { stringResource(it.label) }
                        .joinToString(", ")
                },
            selected = filter.types.isNotEmpty(),
            onClick = { sheet = FilterSheet.Types },
        )
        DropdownChip(
            label = filter.year?.toString() ?: stringResource(R.string.feature_catalog_filter_year),
            selected = filter.year != null,
            onClick = { sheet = FilterSheet.Year },
        )
        DropdownChip(
            label = stringResource(filter.season?.label ?: R.string.feature_catalog_filter_season),
            selected = filter.season != null,
            enabled = filter.year != null,
            onClick = { sheet = FilterSheet.Season },
        )
        DropdownChip(
            label = genresLabel(filter.genreIds),
            selected = filter.genreIds.isNotEmpty(),
            onClick = { sheet = FilterSheet.Genres },
        )
        FilterChip(
            selected = filter.ongoingOnly,
            onClick = { onFilterChange(filter.copy(ongoingOnly = !filter.ongoingOnly)) },
            label = { Text(stringResource(R.string.feature_catalog_filter_ongoing_only)) },
            leadingIcon =
                if (filter.ongoingOnly) {
                    { Icon(painterResource(KadrIcons.Check), contentDescription = null) }
                } else {
                    null
                },
        )
        if (!filter.isEmpty) {
            TextButton(onClick = onReset) { Text(stringResource(R.string.feature_catalog_reset).uppercase()) }
        }
    }
    when (sheet) {
        FilterSheet.Types -> TypesSheet(filter, onFilterChange, onDismiss = { sheet = null })
        FilterSheet.Year -> YearSheet(filter, onFilterChange, onDismiss = { sheet = null })
        FilterSheet.Season -> SeasonSheet(filter, onFilterChange, onDismiss = { sheet = null })
        FilterSheet.Genres -> GenresSheet(filter, onFilterChange, onDismiss = { sheet = null })
        null -> Unit
    }
}

@Composable
private fun genresLabel(genreIds: Set<Int>): String =
    when (genreIds.size) {
        0 -> {
            stringResource(R.string.feature_catalog_filter_genres)
        }

        1 -> {
            CatalogGenres.firstOrNull { it.first == genreIds.first() }?.let { stringResource(it.second) }
                ?: stringResource(R.string.feature_catalog_filter_genres_selected, 1)
        }

        else -> {
            stringResource(R.string.feature_catalog_filter_genres_selected, genreIds.size)
        }
    }

@Composable
private fun DropdownChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    enabled: Boolean = true,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        enabled = enabled,
        trailingIcon = { Icon(painterResource(KadrIcons.ExpandMore), contentDescription = null) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterSheet(
    title: String,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = KadrTheme.palette.surface) {
        Text(
            title,
            Modifier.padding(horizontal = KadrSpacing.Space4, vertical = KadrSpacing.Space2),
            style = MaterialTheme.typography.titleMedium,
        )
        content()
    }
}

@Composable
private fun TypesSheet(
    filter: CatalogFilter,
    onFilterChange: (CatalogFilter) -> Unit,
    onDismiss: () -> Unit,
) {
    FilterSheet(stringResource(R.string.feature_catalog_filter_type), onDismiss) {
        FlowRow(
            Modifier.padding(KadrSpacing.Space4),
            horizontalArrangement = Arrangement.spacedBy(KadrSpacing.Space2),
        ) {
            SeriesType.entries.forEach { type ->
                FilterChip(
                    selected = type in filter.types,
                    onClick = { onFilterChange(filter.copy(types = filter.types.toggle(type))) },
                    label = { Text(stringResource(type.label)) },
                )
            }
        }
    }
}

@Composable
private fun YearSheet(
    filter: CatalogFilter,
    onFilterChange: (CatalogFilter) -> Unit,
    onDismiss: () -> Unit,
) {
    // Анонсы бывают на год вперёд.
    val years = remember { (LocalDate.now().year + 1 downTo FIRST_YEAR).toList() }
    FilterSheet(stringResource(R.string.feature_catalog_filter_year), onDismiss) {
        LazyColumn {
            item {
                OptionRow(stringResource(R.string.feature_catalog_filter_any), filter.year == null) {
                    onFilterChange(filter.copy(year = null))
                    onDismiss()
                }
            }
            items(years) { year ->
                OptionRow(year.toString(), filter.year == year) {
                    onFilterChange(filter.copy(year = year))
                    onDismiss()
                }
            }
        }
    }
}

@Composable
private fun SeasonSheet(
    filter: CatalogFilter,
    onFilterChange: (CatalogFilter) -> Unit,
    onDismiss: () -> Unit,
) {
    FilterSheet(stringResource(R.string.feature_catalog_filter_season), onDismiss) {
        Column {
            OptionRow(stringResource(R.string.feature_catalog_filter_any), filter.season == null) {
                onFilterChange(filter.copy(season = null))
                onDismiss()
            }
            Season.entries.forEach { season ->
                OptionRow(stringResource(season.label), filter.season == season) {
                    onFilterChange(filter.copy(season = season))
                    onDismiss()
                }
            }
        }
    }
}

@Composable
private fun GenresSheet(
    filter: CatalogFilter,
    onFilterChange: (CatalogFilter) -> Unit,
    onDismiss: () -> Unit,
) {
    val resources = LocalResources.current
    val genres =
        remember(resources) {
            val collator = Collator.getInstance(Locale.forLanguageTag("ru"))
            CatalogGenres
                .map { (id, title) ->
                    id to resources.getString(title)
                }.sortedWith(compareBy(collator) { it.second })
        }
    FilterSheet(stringResource(R.string.feature_catalog_filter_genres), onDismiss) {
        GenreMatchSelector(filter.genreMatch, onSelect = { onFilterChange(filter.copy(genreMatch = it)) })
        LazyColumn {
            item {
                FlowRow(
                    Modifier.padding(KadrSpacing.Space4),
                    horizontalArrangement = Arrangement.spacedBy(KadrSpacing.Space2),
                ) {
                    genres.forEach { (id, title) ->
                        FilterChip(
                            selected = id in filter.genreIds,
                            onClick = { onFilterChange(filter.copy(genreIds = filter.genreIds.toggle(id))) },
                            label = { Text(title) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GenreMatchSelector(
    selected: GenreMatch,
    onSelect: (GenreMatch) -> Unit,
) {
    SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth().padding(horizontal = KadrSpacing.Space4)) {
        GenreMatch.entries.forEachIndexed { index, match ->
            val label =
                if (match ==
                    GenreMatch.All
                ) {
                    R.string.feature_catalog_genres_all
                } else {
                    R.string.feature_catalog_genres_any
                }
            SegmentedButton(
                selected = selected == match,
                onClick = { onSelect(match) },
                shape = SegmentedButtonDefaults.itemShape(index, GenreMatch.entries.size),
            ) {
                Text(stringResource(label))
            }
        }
    }
}

private fun <T> Set<T>.toggle(item: T): Set<T> = if (item in this) this - item else this + item

@Composable
private fun OptionRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = KadrLayout.TouchTarget)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = KadrSpacing.Space4),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
        if (selected) {
            Icon(
                painterResource(KadrIcons.Check),
                contentDescription = null,
                tint = KadrTheme.palette.primary,
            )
        }
    }
}

@get:StringRes
private val SeriesType.label: Int
    get() =
        when (this) {
            SeriesType.Tv -> R.string.feature_catalog_type_tv
            SeriesType.Movie -> R.string.feature_catalog_type_movie
            SeriesType.Ova -> R.string.feature_catalog_type_ova
            SeriesType.Ona -> R.string.feature_catalog_type_ona
            SeriesType.Special -> R.string.feature_catalog_type_special
            SeriesType.Music -> R.string.feature_catalog_type_music
        }

@get:StringRes
private val Season.label: Int
    get() =
        when (this) {
            Season.Winter -> R.string.feature_catalog_season_winter
            Season.Spring -> R.string.feature_catalog_season_spring
            Season.Summer -> R.string.feature_catalog_season_summer
            Season.Autumn -> R.string.feature_catalog_season_autumn
        }
