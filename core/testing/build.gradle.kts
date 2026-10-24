plugins {
    alias(libs.plugins.anime365.android.library)
    alias(libs.plugins.anime365.android.compose)
}

// Для unit-тестов фич: фейки репозиториев (интерфейсы — в :core:data), настроек и Скачивания, снимки экранов Roborazzi.
dependencies {
    api(projects.core.data)
    api(projects.core.datastore)
    api(projects.core.player)
    api(projects.core.model)
    api(libs.junit)
    api(libs.kotlinx.coroutines.core)
    api(libs.kotlinx.coroutines.test)
    api(libs.androidx.compose.ui.test.junit4)
    api(libs.roborazzi)
    implementation(projects.core.designsystem)
}
