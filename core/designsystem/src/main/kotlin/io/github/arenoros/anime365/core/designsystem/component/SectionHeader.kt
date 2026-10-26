package io.github.arenoros.anime365.core.designsystem.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import io.github.arenoros.anime365.core.designsystem.R
import io.github.arenoros.anime365.core.designsystem.tokens.KadrLayout
import io.github.arenoros.anime365.core.designsystem.tokens.KadrSpacing

/** Заголовок блока; [onShowAll] добавляет «ВСЕ» справа. */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    onShowAll: (() -> Unit)? = null,
) {
    Row(
        modifier.heightIn(min = KadrLayout.TouchTarget).padding(start = KadrSpacing.Space4, end = KadrSpacing.Space2),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            title,
            Modifier.weight(1f).semantics { heading() },
            style = MaterialTheme.typography.titleMedium,
        )
        if (onShowAll != null) {
            TextButton(modifier = Modifier.tvFocusIndicator(), onClick = onShowAll) {
                Text(stringResource(R.string.core_designsystem_all).uppercase())
            }
        }
    }
}
