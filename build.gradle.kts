plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.spotless)
    alias(libs.plugins.dependency.analysis)
}

spotless {
    val ktlintVersion = libs.versions.ktlint.get()
    kotlin {
        target("**/*.kt")
        targetExclude("**/build/**")
        ktlint(ktlintVersion)
    }
    kotlinGradle {
        target("**/*.gradle.kts")
        targetExclude("**/build/**")
        ktlint(ktlintVersion)
    }
}

dependencyAnalysis {
    structure {
        // Фичи получают Compose через :core:designsystem (api material3): это одна зависимость, а не десяток.
        bundle("compose") {
            includeDependency(":core:designsystem")
            includeGroup("androidx.compose.animation")
            includeGroup("androidx.compose.foundation")
            includeGroup("androidx.compose.material3")
            includeGroup("androidx.compose.runtime")
            includeGroup("androidx.compose.ui")
        }
        // Классы, на которые ссылается код, сгенерированный Hilt.
        bundle("hilt") {
            includeGroup("com.google.dagger")
            includeDependency("androidx.fragment:fragment")
            includeDependency("androidx.lifecycle:lifecycle-viewmodel")
            includeDependency("androidx.lifecycle:lifecycle-viewmodel-savedstate")
        }
        bundle("activity") {
            includeGroup("androidx.activity")
        }
    }
    issues {
        all {
            onAny {
                severity("fail")
            }
            onUnusedDependencies {
                // Подключается convention-плагином во все Compose-модули; @Preview появляются вместе с экранами.
                exclude("androidx.compose.ui:ui-tooling-preview")
            }
        }
    }
}
