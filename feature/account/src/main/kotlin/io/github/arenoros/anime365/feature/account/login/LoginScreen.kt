package io.github.arenoros.anime365.feature.account.login

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.webkit.CookieManager
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.arenoros.anime365.core.data.repository.SiteLinks
import io.github.arenoros.anime365.core.designsystem.component.ErrorState
import io.github.arenoros.anime365.core.designsystem.component.KadrTopAppBar
import io.github.arenoros.anime365.feature.account.R

@Composable
internal fun LoginRoute(
    onDone: () -> Unit,
    onClose: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(state) {
        if (state == LoginState.Done) {
            // Токен получен, сессия сайта больше не нужна: не держим её на диске.
            CookieManager.getInstance().removeAllCookies(null)
            onDone()
        }
    }
    LoginScreen(
        state = state,
        onPageFinished = viewModel::onPageFinished,
        onPageFailed = viewModel::onPageFailed,
        onRetry = viewModel::retry,
        onClose = onClose,
    )
}

@Composable
internal fun LoginScreen(
    state: LoginState,
    onPageFinished: (url: String, sessionCookie: String?) -> Unit,
    onPageFailed: () -> Unit,
    onRetry: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var loading by remember { mutableStateOf(true) }
    Column(modifier.fillMaxSize()) {
        KadrTopAppBar(stringResource(R.string.feature_account_login_title), onBack = onClose, closeIcon = true)
        if (loading || state == LoginState.Exchanging) LinearProgressIndicator(Modifier.fillMaxWidth())
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (state is LoginState.Failed) {
                ErrorState(stringResource(R.string.feature_account_login_failed), onRetry = onRetry)
            } else {
                SiteLoginPage(
                    onLoading = { loading = it },
                    onPageFinished = onPageFinished,
                    onPageFailed = onPageFailed,
                )
            }
        }
    }
}

/** Страница входа сайта: там же вход через ВКонтакте. Своей формы пароля в приложении нет (ADR 0005). */
@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun SiteLoginPage(
    onLoading: (Boolean) -> Unit,
    onPageFinished: (url: String, sessionCookie: String?) -> Unit,
    onPageFailed: () -> Unit,
) {
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                // Формы сайта и вход через ВКонтакте работают на JavaScript.
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                webViewClient =
                    object : WebViewClient() {
                        override fun onPageStarted(
                            view: WebView,
                            url: String,
                            favicon: Bitmap?,
                        ) = onLoading(true)

                        override fun onPageFinished(
                            view: WebView,
                            url: String,
                        ) {
                            onLoading(false)
                            onPageFinished(url, CookieManager.getInstance().getCookie(SiteLinks.SITE))
                        }

                        override fun onReceivedError(
                            view: WebView,
                            request: WebResourceRequest,
                            error: WebResourceError,
                        ) {
                            if (request.isForMainFrame) onPageFailed()
                        }
                    }
                // Сессия прошлого входа могла остаться: начинаем с чистой формы.
                CookieManager.getInstance().removeAllCookies { loadUrl(SiteLinks.LOGIN) }
            }
        },
        modifier = Modifier.fillMaxSize(),
        onRelease = WebView::destroy,
    )
}
