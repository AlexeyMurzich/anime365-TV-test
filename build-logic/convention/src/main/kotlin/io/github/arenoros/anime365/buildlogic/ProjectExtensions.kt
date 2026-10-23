package io.github.arenoros.anime365.buildlogic

import org.gradle.api.Project
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.getByType

internal val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

internal fun VersionCatalog.library(alias: String): Provider<MinimalExternalModuleDependency> =
    findLibrary(alias).orElseThrow { IllegalArgumentException("Нет библиотеки '$alias' в libs.versions.toml") }

/** `:feature:catalog` -> `io.github.arenoros.anime365.feature.catalog`. */
internal val Project.defaultNamespace: String
    get() = "io.github.arenoros.anime365" + path.replace(':', '.')
