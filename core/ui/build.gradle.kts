plugins {
    alias(libs.plugins.anime365.android.library)
    alias(libs.plugins.anime365.android.compose)
}

// Компоненты, которые знают про Аниме и Эпизод и нужны нескольким фичам (ADR 0002).
dependencies {
    api(projects.core.model)
    implementation(projects.core.designsystem)
    // Разрешение на уведомления о Новых Эпизодах.
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.ktx)

    testImplementation(libs.junit)
}
