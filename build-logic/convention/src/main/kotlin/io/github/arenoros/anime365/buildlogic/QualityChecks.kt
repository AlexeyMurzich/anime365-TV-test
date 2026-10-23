package io.github.arenoros.anime365.buildlogic

import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

/** detekt и dependency-analysis на каждом модуле. Отчёт buildHealth собирает корневой проект. */
internal fun Project.configureQualityChecks() {
    pluginManager.apply("io.gitlab.arturbosch.detekt")
    extensions.configure<DetektExtension> {
        buildUponDefaultConfig = true
        parallel = true
        config.setFrom(isolated.rootProject.projectDirectory.file("config/detekt/detekt.yml"))
    }
    // С версии 2.0 плагин не распространяется на модули из корня: без этой строки buildHealth молча пуст.
    pluginManager.apply("com.autonomousapps.dependency-analysis")
}
