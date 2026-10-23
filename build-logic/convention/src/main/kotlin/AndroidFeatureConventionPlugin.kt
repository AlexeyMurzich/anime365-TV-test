import io.github.arenoros.anime365.buildlogic.library
import io.github.arenoros.anime365.buildlogic.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/**
 * Фича: экраны Compose и ключи навигации Navigation 3 (ADR 0009).
 * Фичи не зависят друг от друга (ADR 0002): переходы между ними склеивает :app.
 * Hilt фича подключает сама (`anime365.hilt`) вместе с первой ViewModel.
 */
class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("anime365.android.library")
            pluginManager.apply("anime365.android.compose")
            pluginManager.apply("org.jetbrains.kotlin.plugin.serialization")
            dependencies {
                "implementation"(project(":core:designsystem"))
                // Ключи навигации — публичный API фичи: их видит :app.
                "api"(libs.library("androidx-navigation3-runtime"))
                "api"(libs.library("kotlinx-serialization-core"))
            }
        }
    }
}
