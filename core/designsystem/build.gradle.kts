plugins {
    alias(libs.plugins.anime365.android.library)
    alias(libs.plugins.anime365.android.compose)
    alias(libs.plugins.anime365.design.tokens)
}

dependencies {
    api(libs.androidx.compose.material3)
    implementation(libs.androidx.annotation)
    implementation(libs.coil.compose)

    testImplementation(libs.junit)
}
