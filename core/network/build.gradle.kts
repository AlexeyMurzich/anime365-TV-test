plugins {
    alias(libs.plugins.anime365.android.library)
    alias(libs.plugins.anime365.hilt)
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    // DTO — @Serializable, интерфейс API размечен аннотациями Retrofit.
    api(libs.kotlinx.serialization.core)
    api(libs.retrofit)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.okhttp)
    implementation(libs.retrofit.kotlinx.serialization)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.okhttp.mockwebserver)
}

// Тесты против живого API (README): ./gradlew :core:network:testDebugUnitTest -PliveApi
val liveApi = providers.gradleProperty("liveApi").isPresent
tasks.withType<Test>().configureEach {
    if (!liveApi) exclude("**/live/**")
}
