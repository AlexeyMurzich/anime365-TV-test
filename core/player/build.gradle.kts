plugins {
    alias(libs.plugins.anime365.android.library)
    alias(libs.plugins.anime365.android.compose)
    alias(libs.plugins.anime365.hilt)
    alias(libs.plugins.kotlin.serialization)
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
    // EpisodeDownload и Перевод — в публичном API Скачивания.
    api(projects.core.model)
    // Токен для ссылок Скачивания и настройки «Только по Wi-Fi» и Разрешение.
    implementation(projects.core.datastore)
    // Что скачано — JSON в самом запросе Media3 (DownloadRecord).
    implementation(libs.kotlinx.serialization.core)
    implementation(libs.kotlinx.serialization.json)
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
