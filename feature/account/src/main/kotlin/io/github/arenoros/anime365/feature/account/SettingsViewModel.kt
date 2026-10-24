package io.github.arenoros.anime365.feature.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.arenoros.anime365.core.datastore.AppSettings
import io.github.arenoros.anime365.core.datastore.SettingsStore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class SettingsViewModel
    @Inject
    constructor(
        private val store: SettingsStore,
    ) : ViewModel() {
        /** `null`, пока не прочитаны с диска. */
        val settings: StateFlow<AppSettings?> =
            store.settings.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), null)

        fun update(transform: (AppSettings) -> AppSettings) {
            viewModelScope.launch { store.updateSettings(transform) }
        }

        private companion object {
            const val STOP_TIMEOUT_MILLIS = 5_000L
        }
    }
