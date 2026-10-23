import io.github.arenoros.anime365.buildlogic.configureKotlinJvm
import io.github.arenoros.anime365.buildlogic.configureQualityChecks
import org.gradle.api.Plugin
import org.gradle.api.Project

/** Модуль без Android SDK: доменные модели и общие типы. */
class JvmLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("org.jetbrains.kotlin.jvm")
            configureQualityChecks()
            configureKotlinJvm()
        }
    }
}
