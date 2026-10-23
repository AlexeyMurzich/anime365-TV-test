import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `kotlin-dsl`
}

group = "io.github.arenoros.anime365.buildlogic"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.detekt.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id =
                libs.plugins.anime365.android.application
                    .get()
                    .pluginId
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        register("androidLibrary") {
            id =
                libs.plugins.anime365.android.library
                    .get()
                    .pluginId
            implementationClass = "AndroidLibraryConventionPlugin"
        }
        register("androidCompose") {
            id =
                libs.plugins.anime365.android.compose
                    .get()
                    .pluginId
            implementationClass = "AndroidComposeConventionPlugin"
        }
        register("androidFeature") {
            id =
                libs.plugins.anime365.android.feature
                    .get()
                    .pluginId
            implementationClass = "AndroidFeatureConventionPlugin"
        }
        register("hilt") {
            id =
                libs.plugins.anime365.hilt
                    .get()
                    .pluginId
            implementationClass = "HiltConventionPlugin"
        }
        register("jvmLibrary") {
            id =
                libs.plugins.anime365.jvm.library
                    .get()
                    .pluginId
            implementationClass = "JvmLibraryConventionPlugin"
        }
        register("designTokens") {
            id =
                libs.plugins.anime365.design.tokens
                    .get()
                    .pluginId
            implementationClass = "DesignTokensConventionPlugin"
        }
    }
}
