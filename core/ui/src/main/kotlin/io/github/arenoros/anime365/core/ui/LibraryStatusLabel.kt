package io.github.arenoros.anime365.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import io.github.arenoros.anime365.core.model.LibraryStatus

@Composable
fun LibraryStatus.label(): String =
    stringResource(
        when (this) {
            LibraryStatus.Watching -> R.string.core_ui_status_watching
            LibraryStatus.Planned -> R.string.core_ui_status_planned
            LibraryStatus.Completed -> R.string.core_ui_status_completed
            LibraryStatus.Dropped -> R.string.core_ui_status_dropped
        },
    )
