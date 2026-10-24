plugins {
    alias(libs.plugins.android.test)
    alias(libs.plugins.androidx.baselineprofile)
}

// Снимает Baseline Profile с prod-сборки на эмуляторе (docs/release.md) и меряет запуск.
android {
    namespace = "io.github.arenoros.anime365.baselineprofile"
    compileSdk = 37
    targetProjectPath = ":app"

    defaultConfig {
        // Снимать профиль Android даёт с API 28 на эмуляторе или с root, без root — с API 33.
        minSdk = 28
        targetSdk = 36
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        missingDimensionStrategy("env", "prod")
    }

    testOptions.managedDevices.localDevices.create("pixel6Api34") {
        device = "Pixel 6"
        apiLevel = 34
        systemImageSource = "aosp"
    }
}

baselineProfile {
    managedDevices += "pixel6Api34"
    useConnectedDevices = false
}

dependencies {
    implementation(libs.androidx.benchmark.macro.junit4)
    implementation(libs.androidx.test.ext.junit)
    implementation(libs.androidx.test.uiautomator)
}
