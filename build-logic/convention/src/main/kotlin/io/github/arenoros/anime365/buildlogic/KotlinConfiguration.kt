package io.github.arenoros.anime365.buildlogic

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

// compileSdk открывает новые API при компиляции (Compose 1.12 требует 37); поведение на устройстве задаёт targetSdk.
internal const val COMPILE_SDK = 37
internal const val TARGET_SDK = 36
internal const val MIN_SDK = 26

private val JAVA_VERSION = JavaVersion.VERSION_17
private val JVM_TARGET = JvmTarget.JVM_17

internal fun Project.configureAndroid(android: CommonExtension) {
    android.apply {
        compileSdk = COMPILE_SDK
        defaultConfig.minSdk = MIN_SDK
        compileOptions.sourceCompatibility = JAVA_VERSION
        compileOptions.targetCompatibility = JAVA_VERSION
        lint.apply {
            abortOnError = true
            warningsAsErrors = true
            // Зависят от даты выхода новых версий, а не от нашего кода: CI краснел бы сам по себе.
            disable += setOf("GradleDependency", "NewerVersionAvailable", "AndroidGradlePluginVersion", "OldTargetApi")
        }
    }
    extensions.configure<KotlinAndroidProjectExtension> {
        compilerOptions {
            jvmTarget.set(JVM_TARGET)
            allWarningsAsErrors.set(true)
        }
    }
}

internal fun Project.configureKotlinJvm() {
    extensions.configure<JavaPluginExtension> {
        sourceCompatibility = JAVA_VERSION
        targetCompatibility = JAVA_VERSION
    }
    extensions.configure<KotlinJvmProjectExtension> {
        compilerOptions {
            jvmTarget.set(JVM_TARGET)
            allWarningsAsErrors.set(true)
        }
    }
}
