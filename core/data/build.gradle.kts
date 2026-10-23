plugins {
    alias(libs.plugins.anime365.android.library)
    alias(libs.plugins.anime365.hilt)
}

dependencies {
    api(projects.core.model)
    api(libs.androidx.paging.common)
    api(libs.kotlinx.coroutines.core)
    implementation(projects.core.database)
    implementation(projects.core.network)

    testImplementation(libs.androidx.room.runtime)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.robolectric)
}
