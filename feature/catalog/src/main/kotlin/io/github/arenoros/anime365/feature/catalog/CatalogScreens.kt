package io.github.arenoros.anime365.feature.catalog

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource

// M0: заглушки. Содержимое — в M1 (docs/roadmap.md).

@Composable
internal fun HomeScreen(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(stringResource(R.string.feature_catalog_home_title), style = MaterialTheme.typography.titleLarge)
    }
}

@Composable
internal fun CatalogScreen(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(stringResource(R.string.feature_catalog_catalog_title), style = MaterialTheme.typography.titleLarge)
    }
}
