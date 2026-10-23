package io.github.arenoros.anime365.core.designsystem.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource

/** BottomNav из Kadr: фон `surface`, активный пункт — пилюля `primary-container`. */
@Composable
fun KadrNavigationBar(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        content = content,
    )
}

/** Подпись обязательна: она же служит описанием пункта для TalkBack, поэтому у иконки его нет. */
@Composable
fun RowScope.KadrNavigationBarItem(
    selected: Boolean,
    onClick: () -> Unit,
    @DrawableRes icon: Int,
    label: String,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = { Icon(painterResource(icon), contentDescription = null) },
        label = { Text(label) },
        modifier = modifier,
        colors =
            NavigationBarItemDefaults.colors(
                selectedIconColor = colors.onPrimaryContainer,
                selectedTextColor = colors.onSurface,
                indicatorColor = colors.primaryContainer,
                unselectedIconColor = colors.onSurfaceVariant,
                unselectedTextColor = colors.onSurfaceVariant,
            ),
    )
}
