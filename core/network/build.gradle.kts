plugins {
    alias(libs.plugins.anime365.android.library)
    alias(libs.plugins.anime365.hilt)
    alias(libs.plugins.kotlin.serialization)
}

/** Значение из secrets.properties в корне (файл в .gitignore, README). */
fun secret(key: String): Provider<String> =
    providers.fileContents(rootProject.layout.projectDirectory.file("secrets.properties")).asText.map { text ->
        text
            .lineSequence()
            .firstOrNull { it.startsWith("$key=") }
            ?.substringAfter('=')
            ?.trim()
            .orEmpty()
    }

android {
    buildFeatures.buildConfig = true
    // Приложение (GLOSSARY) — публичный id клиента, как у сайта. Без него вход отвечает
    // «Authorization required.», остальное API работает.
    defaultConfig.buildConfigField("String", "ANIME365_APP", "\"app-68d5ca614f251f6156ffe38e\"")
}

dependencies {
    // DTO — @Serializable, интерфейс API размечен аннотациями Retrofit.
    api(libs.kotlinx.serialization.core)
    api(libs.retrofit)
    // createOkHttpClient() — общий клиент для API и плеера.
    api(libs.okhttp)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.retrofit.kotlinx.serialization)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.okhttp.mockwebserver)
}

// Тесты против живого API (README): ./gradlew :core:network:testDebugUnitTest -PliveApi
// Тесты Видеопотока берут Токен из secrets.properties и без него пропускаются.
val liveApi = providers.gradleProperty("liveApi").isPresent
tasks.withType<Test>().configureEach {
    if (liveApi) {
        systemProperty("anime365.accessToken", secret("ANIME365_ACCESS_TOKEN").orElse("").get())
    } else {
        exclude("**/live/**")
    }
}
