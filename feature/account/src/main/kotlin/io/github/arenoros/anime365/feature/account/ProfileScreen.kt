package io.github.arenoros.anime365.feature.account

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.arenoros.anime365.core.data.repository.SiteLinks
import io.github.arenoros.anime365.core.designsystem.component.KadrTopAppBar
import io.github.arenoros.anime365.core.designsystem.icon.KadrIcons
import io.github.arenoros.anime365.core.designsystem.theme.KadrTheme
import io.github.arenoros.anime365.core.designsystem.tokens.KadrSpacing
import io.github.arenoros.anime365.core.model.Account
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
internal fun ProfileRoute(
    onLogin: () -> Unit,
    viewModel: AccountViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ProfileScreen(state.account, viewModel.now(), onLogin = onLogin, onLogOut = viewModel::logOut)
}

/** Пункты меню из docs/screens.md появляются вместе со своими фичами: Настройки, Экспорт, Публикации. */
@Composable
internal fun ProfileScreen(
    account: Account?,
    now: Instant,
    onLogin: () -> Unit,
    onLogOut: () -> Unit,
    modifier: Modifier = Modifier,
    zone: ZoneId = ZoneId.systemDefault(),
) {
    Column(modifier.fillMaxSize()) {
        KadrTopAppBar(stringResource(R.string.feature_account_profile_title))
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(KadrSpacing.Space4),
            verticalArrangement = Arrangement.spacedBy(KadrSpacing.Space4),
        ) {
            when (account) {
                null -> Unit
                Account.Guest -> Guest(onLogin)
                is Account.User -> User(account, now, zone, onLogOut)
            }
            HorizontalDivider()
            Text(
                stringResource(R.string.feature_account_local_library_hint),
                style = MaterialTheme.typography.bodySmall,
                color = KadrTheme.palette.inkMuted,
            )
        }
    }
}

@Composable
private fun Guest(onLogin: () -> Unit) {
    Text(stringResource(R.string.feature_account_guest_hint), style = MaterialTheme.typography.bodyLarge)
    Button(onClick = onLogin) { Text(stringResource(R.string.feature_account_log_in).uppercase()) }
}

@Composable
private fun User(
    user: Account.User,
    now: Instant,
    zone: ZoneId,
    onLogOut: () -> Unit,
) {
    Text(user.name, style = MaterialTheme.typography.headlineSmall)
    SubscriptionStatus(user, now, zone)
    TextButton(onClick = onLogOut) {
        Icon(painterResource(KadrIcons.Logout), contentDescription = null, Modifier.padding(end = 8.dp))
        Text(stringResource(R.string.feature_account_log_out).uppercase())
    }
}

/** «Активна до 15.03.2027» или «Нет подписки» и ссылка на сайт: оплаты в приложении нет. */
@Composable
internal fun SubscriptionStatus(
    user: Account.User,
    now: Instant,
    zone: ZoneId,
) {
    val until = user.subscriptionUntil
    if (until != null && user.hasSubscription(now)) {
        val date = DateTimeFormatter.ofPattern("dd.MM.yyyy").format(until.atZone(zone))
        Text(
            stringResource(R.string.feature_account_subscription_until, date),
            style = MaterialTheme.typography.bodyLarge,
        )
    } else {
        val uriHandler = LocalUriHandler.current
        Text(stringResource(R.string.feature_account_no_subscription), style = MaterialTheme.typography.bodyLarge)
        OutlinedButton(onClick = { uriHandler.openUri(SiteLinks.SITE) }) {
            Text(stringResource(R.string.feature_account_subscribe_on_site).uppercase())
        }
    }
}
