plugins {
    alias(libs.plugins.anime365.android.library)
    alias(libs.plugins.anime365.hilt)
}

dependencies {
    api(projects.core.model)
    // @ApplicationScope — квалификатор javax.inject в публичном API.
    api(libs.hilt.android)
    api(libs.androidx.paging.common)
    api(libs.kotlinx.coroutines.core)
    implementation(projects.core.database)
    implementation(projects.core.datastore)
    implementation(projects.core.network)

    testImplementation(libs.androidx.room.runtime)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.robolectric)
}
