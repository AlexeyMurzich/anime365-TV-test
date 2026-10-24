plugins {
    alias(libs.plugins.anime365.android.library)
    alias(libs.plugins.anime365.android.compose)
    alias(libs.plugins.anime365.hilt)
}

dependencies {
    // VideoSurface — Composable: в его сигнатуре Modifier и Composer.
    api(libs.androidx.compose.runtime)
    api(libs.androidx.compose.ui)
    api(libs.androidx.media3.common)
    // PlaybackService — наследник MediaSessionService и точка входа Hilt.
    api(libs.androidx.media3.session)
    api(libs.hilt.android)
    api(libs.kotlinx.coroutines.core)
    implementation(libs.androidx.annotation)
    // @OptIn для нестабильного API Media3.
    implementation(libs.androidx.annotation.experimental)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.datasource.okhttp)
    implementation(libs.androidx.media3.ui)
    // Ответ MediaSession на команды уведомления — ListenableFuture.
    implementation(libs.guava)
    // Отдельные субтитры рисует libass (ADR 0008). Вся работа с библиотекой — только в этом модуле.
    implementation(libs.ass.media)
    implementation(libs.okhttp)

    testImplementation(libs.junit)
    testImplementation(libs.okhttp.mockwebserver)
}
