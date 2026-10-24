package io.github.arenoros.anime365.feature.account

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.arenoros.anime365.core.data.repository.AppUpdate
import io.github.arenoros.anime365.core.data.repository.UpdateRepository
import io.github.arenoros.anime365.core.designsystem.tokens.KadrSpacing
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Проверка при запуске: раз в сутки, ошибки сети молча (docs/screens.md, «Диалоги»). */
@Composable
fun DailyUpdateCheck() {
    val viewModel: DailyUpdateViewModel = hiltViewModel()
    val update by viewModel.update.collectAsStateWithLifecycle()
    update?.let { UpdateDialog(it, onDismiss = viewModel::dismiss) }
}

@HiltViewModel
internal class DailyUpdateViewModel
    @Inject
    constructor(
        updates: UpdateRepository,
    ) : ViewModel() {
        private val found = MutableStateFlow<AppUpdate?>(null)
        val update: StateFlow<AppUpdate?> = found.asStateFlow()

        init {
            viewModelScope.launch { found.value = updates.checkDaily() }
        }

        /** «ПОЗЖЕ»: через сутки проверка покажет диалог снова. */
        fun dismiss() {
            found.value = null
        }
    }

/** «Доступно обновление»: «ОБНОВИТЬ» открывает APK в браузере — он скачает файл, установка — системная. */
@Composable
internal fun UpdateDialog(
    update: AppUpdate,
    onDismiss: () -> Unit,
) {
    val uriHandler = LocalUriHandler.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.feature_account_update_title)) },
        text = {
            Column(
                Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(KadrSpacing.Space2),
            ) {
                Text(
                    stringResource(R.string.feature_account_update_version, update.version),
                    style = MaterialTheme.typography.titleSmall,
                )
                if (update.notes.isNotEmpty()) Text(update.notes)
            }
        },
        confirmButton = {
            TextButton(onClick = {
                uriHandler.openUri(update.url)
                onDismiss()
            }) { Text(stringResource(R.string.feature_account_update).uppercase()) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.feature_account_update_later).uppercase()) }
        },
    )
}
