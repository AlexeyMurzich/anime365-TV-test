package io.github.arenoros.anime365

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import io.github.arenoros.anime365.core.designsystem.theme.KadrTheme
import io.github.arenoros.anime365.ui.Anime365App

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            KadrTheme {
                Anime365App()
            }
        }
    }
}
