package io.github.arenoros.anime365.feature.account

import android.content.res.Resources
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.arenoros.anime365.core.data.repository.AppUpdate
import io.github.arenoros.anime365.core.data.repository.ImportMode
import io.github.arenoros.anime365.core.data.repository.LibraryFileSummary
import io.github.arenoros.anime365.core.data.repository.SiteLinks
import io.github.arenoros.anime365.core.designsystem.component.KadrTopAppBar
import io.github.arenoros.anime365.core.designsystem.component.tvFocusIndicator
import io.github.arenoros.anime365.core.designsystem.icon.KadrIcons
import io.github.arenoros.anime365.core.designsystem.theme.KadrTheme
import io.github.arenoros.anime365.core.designsystem.tokens.KadrLayout
import io.github.arenoros.anime365.core.designsystem.tokens.KadrSpacing
import io.github.arenoros.anime365.core.model.Account
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
internal fun ProfileRoute(
    onLogin: () -> Unit,
    onOpenSettings: () -> Unit,
    checksUpdates: Boolean,
    viewModel: AccountViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val resources = LocalResources.current
    LaunchedEffect(viewModel) {
        viewModel.transferResults.collect { result ->
            snackbar.showSnackbar(resources.message(result))
        }
    }
    var update by remember { mutableStateOf<AppUpdate?>(null) }
    LaunchedEffect(viewModel) {
        viewModel.updateChecks.collect { check ->
            when (check) {
                is UpdateCheck.Available -> update = check.update
                UpdateCheck.Latest -> snackbar.showSnackbar(resources.getString(R.string.feature_account_update_latest))
                UpdateCheck.Failed -> snackbar.showSnackbar(resources.getString(R.string.feature_account_update_failed))
            }
        }
    }
    update?.let { UpdateDialog(it, onDismiss = { update = null }) }
    val exportFile =
        rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument(LIBRARY_FILE_TYPE)) { uri ->
            uri?.let(viewModel::export)
        }
    var importFile by rememberSaveable { mutableStateOf<Uri?>(null) }
    val openFile = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { importFile = it }
    importFile?.let { uri ->
        ImportModeDialog(
            onSelect = { mode ->
                viewModel.import(uri, mode)
                importFile = null
            },
            onDismiss = { importFile = null },
        )
    }
    ProfileScreen(
        state.account,
        viewModel.now(),
        onLogin = onLogin,
        onLogOut = viewModel::logOut,
        onOpenSettings = onOpenSettings,
        onExport = { exportFile.launch(viewModel.exportFileName()) },
        // Не все файловые менеджеры знают тип .json.
        onImport = { openFile.launch(arrayOf(LIBRARY_FILE_TYPE, "application/octet-stream")) },
        onCheckUpdates = viewModel::checkUpdates.takeIf { checksUpdates },
        snackbar = snackbar,
    )
}

private const val LIBRARY_FILE_TYPE = "application/json"

private fun Resources.message(result: TransferResult): String =
    when (result) {
        is TransferResult.Exported -> {
            summary(result.summary, R.string.feature_account_exported, R.string.feature_account_exported_with_progress)
        }

        is TransferResult.Imported -> {
            summary(result.summary, R.string.feature_account_imported, R.string.feature_account_imported_with_progress)
        }

        TransferResult.ExportFailed -> {
            getString(R.string.feature_account_export_failed)
        }

        TransferResult.ImportFailed -> {
            getString(R.string.feature_account_import_failed)
        }

        TransferResult.NewerFile -> {
            getString(R.string.feature_account_import_newer)
        }
    }

/** «5 аниме» — если у всех есть Статус; иначе отдельно те, что в Списке, и те, где только Прогресс. */
private fun Resources.summary(
    summary: LibraryFileSummary,
    @StringRes inListOnly: Int,
    @StringRes withProgress: Int,
): String {
    val progressOnly = summary.total - summary.inList
    return if (progressOnly ==
        0
    ) {
        getString(inListOnly, summary.inList)
    } else {
        getString(withProgress, summary.inList, progressOnly)
    }
}

/**
 * Пункты меню из docs/screens.md появляются вместе со своими фичами: Мои публикации — с Публикацией.
 * [onCheckUpdates] — `null` в сборках, которые не обновляются с GitHub (dev).
 */
@Composable
internal fun ProfileScreen(
    account: Account?,
    now: Instant,
    onLogin: () -> Unit,
    onLogOut: () -> Unit,
    onOpenSettings: () -> Unit,
    onExport: () -> Unit,
    onImport: () -> Unit,
    modifier: Modifier = Modifier,
    onCheckUpdates: (() -> Unit)? = null,
    snackbar: SnackbarHostState = remember { SnackbarHostState() },
    zone: ZoneId = ZoneId.systemDefault(),
) {
    Scaffold(
        modifier = modifier,
        topBar = { KadrTopAppBar(stringResource(R.string.feature_account_profile_title)) },
        snackbarHost = { SnackbarHost(snackbar) },
        containerColor = KadrTheme.palette.background,
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(vertical = KadrSpacing.Space4),
            verticalArrangement = Arrangement.spacedBy(KadrSpacing.Space4),
        ) {
            Column(
                Modifier.padding(horizontal = KadrSpacing.Space4),
                verticalArrangement = Arrangement.spacedBy(KadrSpacing.Space4),
            ) {
                when (account) {
                    null -> Unit
                    Account.Guest -> Guest(onLogin)
                    is Account.User -> User(account, now, zone, onLogOut)
                }
            }
            HorizontalDivider()
            Column {
                MenuRow(KadrIcons.Settings, stringResource(R.string.feature_account_settings), onOpenSettings)
                MenuRow(KadrIcons.Upload, stringResource(R.string.feature_account_export), onExport)
                MenuRow(KadrIcons.Download, stringResource(R.string.feature_account_import), onImport)
                onCheckUpdates?.let {
                    MenuRow(KadrIcons.Update, stringResource(R.string.feature_account_check_updates), it)
                }
            }
            Text(
                stringResource(R.string.feature_account_local_library_hint),
                Modifier.padding(horizontal = KadrSpacing.Space4),
                style = MaterialTheme.typography.bodySmall,
                color = KadrTheme.palette.inkMuted,
            )
        }
    }
}

@Composable
private fun MenuRow(
    @DrawableRes icon: Int,
    label: String,
    onClick: () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = KadrLayout.TouchTarget)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = KadrSpacing.Space4),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(KadrSpacing.Space4),
    ) {
        Icon(painterResource(icon), contentDescription = null, tint = KadrTheme.palette.inkMuted)
        Text(label, style = MaterialTheme.typography.bodyLarge)
    }
}

/** «Импорт Списка»: заменить текущий или объединить (docs/screens.md, «Диалоги»). */
@Composable
private fun ImportModeDialog(
    onSelect: (ImportMode) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.feature_account_import)) },
        text = { Text(stringResource(R.string.feature_account_import_text)) },
        confirmButton = {
            Column(horizontalAlignment = Alignment.End) {
                TextButton(modifier = Modifier.tvFocusIndicator(), onClick = { onSelect(ImportMode.Merge) }) {
                    Text(stringResource(R.string.feature_account_import_merge).uppercase())
                }
                TextButton(modifier = Modifier.tvFocusIndicator(), onClick = { onSelect(ImportMode.Replace) }) {
                    Text(stringResource(R.string.feature_account_import_replace).uppercase())
                }
                TextButton(modifier = Modifier.tvFocusIndicator(), onClick = onDismiss) {
                    Text(stringResource(R.string.feature_account_cancel).uppercase())
                }
            }
        },
    )
}

@Composable
private fun Guest(onLogin: () -> Unit) {
    Text(stringResource(R.string.feature_account_guest_hint), style = MaterialTheme.typography.bodyLarge)
    Button(modifier = Modifier.tvFocusIndicator(), onClick = onLogin) {
        Text(stringResource(R.string.feature_account_log_in).uppercase())
    }
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
    TextButton(modifier = Modifier.tvFocusIndicator(), onClick = onLogOut) {
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
        OutlinedButton(modifier = Modifier.tvFocusIndicator(), onClick = { uriHandler.openUri(SiteLinks.SITE) }) {
            Text(stringResource(R.string.feature_account_subscribe_on_site).uppercase())
        }
    }
}
