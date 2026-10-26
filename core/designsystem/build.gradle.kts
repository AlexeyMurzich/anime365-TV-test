plugins {
    alias(libs.plugins.anime365.android.library)
    alias(libs.plugins.anime365.android.compose)
    alias(libs.plugins.anime365.design.tokens)
}

dependencies {
    api(libs.androidx.compose.material3)
    // Лист Выбора Перевода — сцена Navigation 3 (BottomSheetSceneStrategy).
    api(libs.androidx.navigation3.ui)
    implementation(libs.androidx.annotation)
    implementation(libs.coil.compose)
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(libs.junit)
}
