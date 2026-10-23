package io.github.arenoros.anime365.feature.account.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.arenoros.anime365.core.data.repository.AccountRepository
import io.github.arenoros.anime365.core.data.repository.SiteLinks
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

internal sealed interface LoginState {
    /** Пользователь на страницах сайта: вводит пароль, входит через ВКонтакте. */
    data object Browsing : LoginState

    /** Сессия сайта меняется на Токен. */
    data object Exchanging : LoginState

    data object Done : LoginState

    data class Failed(
        val error: Throwable,
    ) : LoginState
}

/**
 * Куда сайт ведёт после входа, не документировано. Поэтому после каждой загруженной страницы,
 * кроме самой формы входа, пробуем обменять сессию на Токен (ADR 0005): без входа сервер ответит
 * ошибкой, и пользователь просто продолжит.
 */
@HiltViewModel
internal class LoginViewModel
    @Inject
    constructor(
        private val repository: AccountRepository,
    ) : ViewModel() {
        private val _state = MutableStateFlow<LoginState>(LoginState.Browsing)
        val state: StateFlow<LoginState> = _state.asStateFlow()

        fun onPageFinished(
            url: String,
            sessionCookie: String?,
        ) {
            val onSitePage = url.startsWith(SiteLinks.SITE) && !url.startsWith(SiteLinks.LOGIN)
            if (_state.value != LoginState.Browsing || !onSitePage || sessionCookie.isNullOrEmpty()) return
            _state.value = LoginState.Exchanging
            viewModelScope.launch {
                val error = repository.logIn(sessionCookie).exceptionOrNull()
                _state.value =
                    when (error) {
                        null -> LoginState.Done
                        is IOException -> LoginState.Failed(error)
                        else -> LoginState.Browsing
                    }
            }
        }

        fun onPageFailed() {
            _state.value = LoginState.Failed(IOException("Login page failed to load"))
        }

        fun retry() {
            _state.value = LoginState.Browsing
        }
    }
