package io.github.arenoros.anime365.core.designsystem.component

import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable

/** Avoid focusing a touch-only drag handle that collapses the sheet on Down. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun tvAwareSheetDragHandle(): (@Composable () -> Unit)? =
    if (isTelevision()) null else ({ BottomSheetDefaults.DragHandle() })
