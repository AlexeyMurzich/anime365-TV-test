package io.github.arenoros.anime365.feature.account

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.arenoros.anime365.core.data.repository.SiteLinks
import io.github.arenoros.anime365.core.designsystem.component.KadrTopAppBar
import io.github.arenoros.anime365.core.designsystem.component.errorMessage
import io.github.arenoros.anime365.core.designsystem.theme.KadrTheme
import io.github.arenoros.anime365.core.designsystem.tokens.KadrSpacing
import io.github.arenoros.anime365.core.model.Account

@Composable
internal fun SubscriptionRoute(
    onDone: () -> Unit,
    onBack: () -> Unit,
    viewModel: AccountViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val now = viewModel.now()
    val hasSubscription = (state.account as? Account.User)?.hasSubscription(now) == true
    // Подписка появилась — пояснение больше не нужно: назад, к тому, что хотели смотреть.
    LaunchedEffect(hasSubscription) { if (hasSubscription) onDone() }
    // Подсказка «пока не видна» — только после нажатия, а не после проверки при открытии.
    var asked by rememberSaveable { mutableStateOf(false) }
    SubscriptionScreen(
        state = state,
        stillMissing = asked && !state.isRefreshing && state.error == null,
        onRefresh = {
            asked = true
            viewModel.refresh()
        },
        onBack = onBack,
    )
}

@Composable
internal fun SubscriptionScreen(
    state: AccountUiState,
    stillMissing: Boolean,
    onRefresh: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uriHandler = LocalUriHandler.current
    Column(modifier.fillMaxSize()) {
        KadrTopAppBar(stringResource(R.string.feature_account_subscription_title), onBack = onBack)
        if (state.isRefreshing) LinearProgressIndicator(Modifier.fillMaxWidth())
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(KadrSpacing.Space6),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(KadrSpacing.Space4),
        ) {
            Text(
                stringResource(R.string.feature_account_subscription_text),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
            )
            Button(onClick = { uriHandler.openUri(SiteLinks.SITE) }) {
                Text(stringResource(R.string.feature_account_subscribe_on_site).uppercase())
            }
            TextButton(onClick = onRefresh, enabled = !state.isRefreshing) {
                Text(stringResource(R.string.feature_account_subscription_refresh).uppercase())
            }
            val error = state.error
            val hint =
                when {
                    error != null -> errorMessage(error)
                    stillMissing -> stringResource(R.string.feature_account_subscription_still_missing)
                    else -> null
                }
            if (hint != null) {
                Text(
                    hint,
                    style = MaterialTheme.typography.bodySmall,
                    color = KadrTheme.palette.inkMuted,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
