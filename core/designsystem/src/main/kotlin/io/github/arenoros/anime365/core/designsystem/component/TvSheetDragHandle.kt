package io.github.arenoros.anime365.core.designsystem.component

import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable

/** На телевизоре ручки нет: она нужна только для касаний, а в фокусе пульта сворачивает лист по «Вниз». */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun tvAwareSheetDragHandle(): (@Composable () -> Unit)? =
    if (isTelevision()) null else ({ BottomSheetDefaults.DragHandle() })
