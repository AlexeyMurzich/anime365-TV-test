package io.github.arenoros.anime365.feature.account

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.arenoros.anime365.core.data.repository.AccountRepository
import io.github.arenoros.anime365.core.data.repository.ImportMode
import io.github.arenoros.anime365.core.data.repository.LibraryFileSummary
import io.github.arenoros.anime365.core.data.repository.LibraryTransfer
import io.github.arenoros.anime365.core.data.repository.UnsupportedLibraryFileException
import io.github.arenoros.anime365.core.model.Account
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject

/** [account] — `null`, пока не прочитан с диска. */
internal data class AccountUiState(
    val account: Account? = null,
    val isRefreshing: Boolean = false,
    val error: Throwable? = null,
)

/** Итог Экспорта или Импорта Списка — текст снекбара. */
internal sealed interface TransferResult {
    data class Exported(
        val summary: LibraryFileSummary,
    ) : TransferResult

    data class Imported(
        val summary: LibraryFileSummary,
    ) : TransferResult

    data object ExportFailed : TransferResult

    data object ImportFailed : TransferResult

    /** Файл из более новой версии приложения. */
    data object NewerFile : TransferResult
}

/** Профиль и Нужна Подписка: оба показывают аккаунт и перечитывают его с сайта. */
@HiltViewModel
internal class AccountViewModel
    @Inject
    constructor(
        private val repository: AccountRepository,
        private val transfer: LibraryTransfer,
        private val clock: Clock,
    ) : ViewModel() {
        private val refreshing = MutableStateFlow(false)
        private val error = MutableStateFlow<Throwable?>(null)
        private val transferChannel = Channel<TransferResult>(Channel.BUFFERED)
        val transferResults: Flow<TransferResult> = transferChannel.receiveAsFlow()

        val uiState: StateFlow<AccountUiState> =
            combine(repository.account, refreshing, error, ::AccountUiState)
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), AccountUiState())

        init {
            // Подписку оформляют и продлевают на сайте: при каждом открытии сверяемся с ним.
            refresh()
        }

        fun now(): Instant = clock.instant()

        fun refresh() {
            viewModelScope.launch {
                refreshing.value = true
                error.value = repository.refresh().exceptionOrNull()
                refreshing.value = false
            }
        }

        fun logOut() {
            viewModelScope.launch { repository.logOut() }
        }

        /** «anime365-library-2026-10-24.json» — дата по часам устройства. */
        fun exportFileName(zone: ZoneId = ZoneId.systemDefault()): String =
            "anime365-library-${clock.instant().atZone(zone).toLocalDate()}.json"

        fun export(uri: Uri) {
            viewModelScope.launch {
                transferChannel.send(
                    transfer.export(uri).fold({ TransferResult.Exported(it) }, { TransferResult.ExportFailed }),
                )
            }
        }

        fun import(
            uri: Uri,
            mode: ImportMode,
        ) {
            viewModelScope.launch {
                val result = transfer.import(uri, mode)
                transferChannel.send(
                    when (val error = result.exceptionOrNull()) {
                        null -> TransferResult.Imported(result.getOrThrow())
                        is UnsupportedLibraryFileException -> TransferResult.NewerFile
                        else -> TransferResult.ImportFailed
                    },
                )
            }
        }

        private companion object {
            const val STOP_TIMEOUT_MILLIS = 5_000L
        }
    }
