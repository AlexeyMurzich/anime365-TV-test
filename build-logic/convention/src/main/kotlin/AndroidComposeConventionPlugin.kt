import com.android.build.api.dsl.CommonExtension
import io.github.arenoros.anime365.buildlogic.library
import io.github.arenoros.anime365.buildlogic.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/** Подключается поверх anime365.android.application или anime365.android.library. */
class AndroidComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("org.jetbrains.kotlin.plugin.compose")
            (extensions.getByName("android") as CommonExtension).buildFeatures.compose = true
            dependencies {
                "implementation"(platform(libs.library("androidx-compose-bom")))
                "implementation"(libs.library("androidx-compose-ui-tooling-preview"))
                "debugImplementation"(libs.library("androidx-compose-ui-tooling"))
            }
        }
    }
}
