package io.github.arenoros.anime365.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import io.github.arenoros.anime365.core.designsystem.R
import io.github.arenoros.anime365.core.designsystem.theme.KadrTheme
import io.github.arenoros.anime365.core.designsystem.tokens.KadrSpacing
import java.io.IOException

/** Текст ошибки для человека: отличаем только «нет сети» от всего остального. */
@Composable
fun errorMessage(error: Throwable): String =
    stringResource(
        if (error is IOException) {
            R.string.core_designsystem_error_offline
        } else {
            R.string.core_designsystem_error_generic
        },
    )

/** Плашка над данными из кэша, которые не удалось обновить из-за [error]. */
@Composable
fun StaleDataBanner(
    error: Throwable,
    modifier: Modifier = Modifier,
) {
    StaleDataBanner(
        stringResource(
            if (error is IOException) {
                R.string.core_designsystem_stale_offline
            } else {
                R.string.core_designsystem_stale_generic
            },
        ),
        modifier,
    )
}

/** Ошибка без данных: текст по-человечески и «ПОВТОРИТЬ». Тостов нет (docs/screens.md). */
@Composable
fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    MessageState(message, modifier) {
        Button(modifier = Modifier.tvFocusIndicator(), onClick = onRetry) {
            Text(stringResource(R.string.core_designsystem_retry).uppercase())
        }
    }
}

/** Пусто: пояснение и действие, которое из этого выводит. */
@Composable
fun EmptyState(
    message: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: () -> Unit = {},
) {
    MessageState(message, modifier) {
        actionLabel?.let { label ->
            TextButton(modifier = Modifier.tvFocusIndicator(), onClick = onAction) { Text(label.uppercase()) }
        }
    }
}

@Composable
private fun MessageState(
    message: String,
    modifier: Modifier,
    action: @Composable () -> Unit,
) {
    Column(
        modifier.fillMaxWidth().padding(KadrSpacing.Space6),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(KadrSpacing.Space4),
    ) {
        Text(
            message,
            style = MaterialTheme.typography.bodyLarge,
            color = KadrTheme.palette.inkMuted,
            textAlign = TextAlign.Center,
        )
        action()
    }
}

/** Тонкая плашка поверх данных из кэша, когда обновить их не вышло. */
@Composable
fun StaleDataBanner(
    message: String,
    modifier: Modifier = Modifier,
) {
    Surface(modifier.fillMaxWidth(), color = KadrTheme.palette.surfaceVariant, contentColor = KadrTheme.palette.ink) {
        Text(
            message,
            Modifier.padding(horizontal = KadrSpacing.Space4, vertical = KadrSpacing.Space2),
            style = MaterialTheme.typography.bodySmall,
        )
    }
}
