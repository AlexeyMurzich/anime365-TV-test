package io.github.arenoros.anime365.core.designsystem.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import io.github.arenoros.anime365.core.designsystem.R
import io.github.arenoros.anime365.core.designsystem.icon.KadrIcons
import io.github.arenoros.anime365.core.designsystem.theme.KadrTheme
import io.github.arenoros.anime365.core.designsystem.tokens.KadrLayout
import io.github.arenoros.anime365.core.designsystem.tokens.KadrSpacing

@Composable
fun KadrSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth().tvFocusIndicator(),
        placeholder = { Text(placeholder) },
        leadingIcon = { Icon(painterResource(KadrIcons.Search), contentDescription = null) },
        trailingIcon = {
            if (value.isNotEmpty()) {
                IconButton(modifier = Modifier.tvFocusIndicator(), onClick = { onValueChange("") }) {
                    Icon(painterResource(KadrIcons.Close), stringResource(R.string.core_designsystem_clear))
                }
            }
        },
        singleLine = true,
        shape = MaterialTheme.shapes.small,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
        colors =
            TextFieldDefaults.colors(
                focusedContainerColor = KadrTheme.palette.surface,
                unfocusedContainerColor = KadrTheme.palette.surface,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
            ),
    )
}

/** Поле поиска без ввода: касание ведёт туда, где ищут по-настоящему. */
@Composable
fun KadrSearchButton(
    placeholder: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier =
            modifier
                .fillMaxWidth()
                .heightIn(min = KadrLayout.TouchTarget)
                .tvClickable(role = Role.Button, onClick = onClick),
        shape = MaterialTheme.shapes.small,
        color = KadrTheme.palette.surface,
        contentColor = KadrTheme.palette.inkMuted,
    ) {
        Row(Modifier.padding(KadrSpacing.Space4), verticalAlignment = Alignment.CenterVertically) {
            Icon(painterResource(KadrIcons.Search), contentDescription = null)
            Text(placeholder, Modifier.padding(start = KadrSpacing.Space4), style = MaterialTheme.typography.bodyLarge)
        }
    }
}
