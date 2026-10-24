plugins {
    alias(libs.plugins.anime365.android.application)
    alias(libs.plugins.anime365.android.compose)
    alias(libs.plugins.anime365.hilt)
    alias(libs.plugins.androidx.baselineprofile)
    alias(libs.plugins.sentry)
}

// Версия — из последнего git-тега vX.Y.Z (docs/roadmap.md, M6); без тегов — 0.0.0.
val versionTag =
    providers
        .exec {
            commandLine("git", "describe", "--tags", "--match", "v*", "--abbrev=0")
            isIgnoreExitValue = true
        }.standardOutput.asText
        .get()
        .trim()
        .ifEmpty { "v0.0.0" }
val versionParts =
    requireNotNull(Regex("""v(\d+)\.(\d+)\.(\d+)""").matchEntire(versionTag)) { "Тег $versionTag — не vX.Y.Z" }
        .groupValues
        .drop(1)
        .map(String::toInt)
require(versionParts[1] < 100 && versionParts[2] < 100) { "В $versionTag minor и patch должны быть меньше 100" }

// Секреты релиза есть только в CI (docs/release.md).
val signingKeystore = providers.environmentVariable("SIGNING_KEYSTORE").orNull
val sentryDsn = providers.environmentVariable("SENTRY_DSN").orElse("").get()

android {
    namespace = "io.github.arenoros.anime365"

    defaultConfig {
        applicationId = "io.github.arenoros.anime365"
        // Ноль Android не принимает: сборка без тегов получает 1.
        versionCode = (versionParts[0] * 10000 + versionParts[1] * 100 + versionParts[2]).coerceAtLeast(1)
        versionName = versionParts.joinToString(".")
        // Пусто — отчёты о сбоях не отправляются и переключателя в Настройках нет (ADR 0006).
        buildConfigField("String", "SENTRY_DSN", "\"$sentryDsn\"")
    }
    buildFeatures.buildConfig = true

    flavorDimensions += "env"
    productFlavors {
        create("dev") {
            dimension = "env"
            applicationIdSuffix = ".dev"
            versionNameSuffix = "-dev"
        }
        create("prod") {
            dimension = "env"
        }
    }

    // libass — нативная: APK под каждую архитектуру вместо одного на все (ADR 0008).
    splits.abi {
        isEnable = true
        reset()
        include("arm64-v8a", "armeabi-v7a", "x86_64")
        isUniversalApk = false
    }

    signingConfigs {
        create("release") {
            storeFile = signingKeystore?.let(::file)
            storePassword = providers.environmentVariable("SIGNING_PASSWORD").orNull
            keyAlias = "anime365"
            keyPassword = providers.environmentVariable("SIGNING_PASSWORD").orNull
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            // Без ключа релизов — debug-ключ: сборку можно поставить локально и снять с неё Baseline Profile.
            signingConfig = signingConfigs.getByName(if (signingKeystore != null) "release" else "debug")
        }
    }
}

baselineProfile {
    // Код у dev и prod один: профиль общий.
    mergeIntoMain = true
}

sentry {
    // mapping.txt уходит в Sentry только из CI релиза, где есть токен; локально и в форках — нет.
    autoUploadProguardMapping = !providers.environmentVariable("SENTRY_AUTH_TOKEN").orNull.isNullOrEmpty()
    ignoredFlavors = setOf("dev")
    ignoredBuildTypes = setOf("debug", "benchmarkRelease", "nonMinifiedRelease")
    // SDK и интеграции подключены руками ниже, байткод не трогаем, о сборке плагин никуда не сообщает.
    autoInstallation.enabled = false
    tracingInstrumentation.enabled = false
    includeDependenciesReport = false
    telemetry = false
}

dependencies {
    implementation(projects.core.designsystem)
    implementation(projects.feature.account)
    implementation(projects.feature.catalog)
    implementation(projects.feature.library)
    implementation(projects.feature.player)
    implementation(projects.feature.series)

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.annotation)
    implementation(libs.androidx.hilt.lifecycle.viewmodel.compose)
    implementation(libs.androidx.hilt.work)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.coil)
    implementation(libs.coil.network.okhttp)
    // Граф Hilt собирается здесь: сгенерированный код видит OkHttpClient и CoroutineScope из :core.
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.okhttp)
    implementation(libs.androidx.work.runtime)
    implementation(projects.core.data)
    implementation(projects.core.datastore)
    implementation(projects.core.model)
    implementation(projects.core.player)
    // Граф Hilt видит ListenableFuture из API MediaSession в :core:player.
    implementation(libs.guava)
    implementation(libs.sentry.core)
    implementation(libs.sentry.android.core)
    // Падения внутри libass.
    runtimeOnly(libs.sentry.android.ndk)
    // Ставит Baseline Profile при первом запуске: APK приходит не из магазина, который сделал бы это сам.
    runtimeOnly(libs.androidx.profileinstaller)

    baselineProfile(projects.baselineprofile)

    testImplementation(libs.junit)
}
