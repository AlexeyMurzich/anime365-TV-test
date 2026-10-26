package io.github.arenoros.anime365.feature.series

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import io.github.arenoros.anime365.core.designsystem.component.tvAwareSheetDragHandle
import io.github.arenoros.anime365.core.designsystem.component.tvFocusIndicator
import io.github.arenoros.anime365.core.designsystem.icon.KadrIcons
import io.github.arenoros.anime365.core.designsystem.tokens.KadrLayout
import io.github.arenoros.anime365.core.designsystem.tokens.KadrSpacing
import io.github.arenoros.anime365.core.model.LibraryStatus
import io.github.arenoros.anime365.core.ui.label

/** «В СПИСОК», а если аниме уже в Списке — его Статус. */
@Composable
internal fun LibraryButton(
    status: LibraryStatus?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedButton(onClick = onClick, modifier = modifier.tvFocusIndicator()) {
        Icon(
            painterResource(if (status == null) KadrIcons.BookmarkBorder else KadrIcons.Bookmark),
            contentDescription = null,
        )
        Text(
            (status?.label() ?: stringResource(R.string.feature_series_add_to_list)).uppercase(),
            Modifier.padding(start = KadrSpacing.Space2),
            maxLines = 1,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun StatusSheet(
    status: LibraryStatus?,
    onSelect: (LibraryStatus?) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(dragHandle = tvAwareSheetDragHandle(), onDismissRequest = onDismiss) {
        StatusOptions(status, onSelect, Modifier.navigationBarsPadding())
    }
}

/** Статусы и «Убрать из списка»: Прогресс и запомненный Перевод при этом остаются. */
@Composable
internal fun StatusOptions(
    status: LibraryStatus?,
    onSelect: (LibraryStatus?) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxWidth().padding(bottom = KadrSpacing.Space4)) {
        Text(
            stringResource(R.string.feature_series_status_title),
            Modifier.padding(horizontal = KadrSpacing.Space4).semantics { heading() },
            style = MaterialTheme.typography.titleLarge,
        )
        Column(Modifier.selectableGroup()) {
            LibraryStatus.entries.forEach { option ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .heightIn(min = KadrLayout.TouchTarget)
                        .tvFocusIndicator()
                        .selectable(
                            selected = option == status,
                            role = Role.RadioButton,
                            onClick = { onSelect(option) },
                        ).padding(horizontal = KadrSpacing.Space4),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(selected = option == status, onClick = null)
                    Text(
                        option.label(),
                        Modifier.padding(start = KadrSpacing.Space4),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        }
        if (status != null) {
            TextButton(
                onClick = { onSelect(null) },
                modifier = Modifier.padding(horizontal = KadrSpacing.Space2).tvFocusIndicator(),
            ) {
                Text(stringResource(R.string.feature_series_remove_from_list).uppercase())
            }
        }
    }
}
