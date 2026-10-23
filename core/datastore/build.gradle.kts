plugins {
    alias(libs.plugins.anime365.android.library)
    alias(libs.plugins.anime365.hilt)
}

dependencies {
    api(libs.kotlinx.coroutines.core)
    implementation(libs.androidx.datastore.preferences)
}
