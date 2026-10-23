plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.room) apply false
    alias(libs.plugins.roborazzi) apply false
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
    abi {
        exclusions {
            // Фабрики Hilt и реализации Room публичны в байт-коде, но API модуля не являются.
            ignoreGeneratedCode()
            // Модули Hilt и ViewModel — точки входа графа Hilt, а не API для соседних модулей.
            excludeAnnotations("dagger\\.Module", "dagger\\.hilt\\.android\\.lifecycle\\.HiltViewModel")
            // Реализации базы и DAO, сгенерированные Room.
            excludeClasses(".*_Impl.*")
        }
    }
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
            includeDependency("javax.inject:javax.inject")
            includeDependency("androidx.fragment:fragment")
        }
        // hiltViewModel() инлайнится в вызовы lifecycle-viewmodel-compose.
        bundle("lifecycle") {
            includeGroup("androidx.lifecycle")
            includeGroup("androidx.hilt")
        }
        // Код, сгенерированный Room, ссылается на room-common, sqlite и collection.
        bundle("room") {
            includeGroup("androidx.room")
            includeGroup("androidx.sqlite")
            includeDependency("androidx.collection:collection")
        }
        bundle("paging") {
            includeGroup("androidx.paging")
        }
        bundle("coil") {
            includeGroup("io.coil-kt.coil3")
        }
        bundle("roborazzi") {
            includeGroup("io.github.takahirom.roborazzi")
            includeGroup("com.dropbox.differ")
        }
        bundle("robolectric") {
            includeGroup("org.robolectric")
        }
        bundle("activity") {
            includeGroup("androidx.activity")
        }
        // Артефакты Media3 — одна библиотека; guava — в её API (ImmutableSet у параметров дорожек).
        bundle("media3") {
            includeGroup("androidx.media3")
            includeDependency("com.google.guava:guava")
        }
        bundle("okhttp") {
            includeGroup("com.squareup.okhttp3")
            includeGroup("com.squareup.okio")
        }
        bundle("datastore") {
            includeGroup("androidx.datastore")
        }
        bundle("navigation3") {
            includeGroup("androidx.navigation3")
        }
        bundle("core") {
            includeGroup("androidx.core")
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
