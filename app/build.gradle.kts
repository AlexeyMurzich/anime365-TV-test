plugins {
    alias(libs.plugins.anime365.android.application)
    alias(libs.plugins.anime365.android.compose)
    alias(libs.plugins.anime365.hilt)
}

android {
    namespace = "io.github.arenoros.anime365"

    defaultConfig {
        applicationId = "io.github.arenoros.anime365"
        versionCode = 1
        versionName = "0.1.0"
    }

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

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
}

dependencies {
    implementation(projects.core.designsystem)
    implementation(projects.feature.account)
    implementation(projects.feature.catalog)
    implementation(projects.feature.library)

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.annotation)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
}
