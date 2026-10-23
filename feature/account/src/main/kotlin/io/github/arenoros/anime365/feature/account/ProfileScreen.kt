package io.github.arenoros.anime365.feature.account

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource

// M0: заглушка. Вход — в M2, остальной Профиль — по мере появления фич (docs/roadmap.md).
@Composable
internal fun ProfileScreen(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(stringResource(R.string.feature_account_profile_title), style = MaterialTheme.typography.titleLarge)
    }
}
