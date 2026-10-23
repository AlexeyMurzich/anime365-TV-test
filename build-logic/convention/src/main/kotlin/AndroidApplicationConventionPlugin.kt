import com.android.build.api.dsl.ApplicationExtension
import io.github.arenoros.anime365.buildlogic.TARGET_SDK
import io.github.arenoros.anime365.buildlogic.configureAndroid
import io.github.arenoros.anime365.buildlogic.configureQualityChecks
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.android.application")
            configureQualityChecks()
            extensions.configure<ApplicationExtension> {
                configureAndroid(this)
                defaultConfig.targetSdk = TARGET_SDK
            }
        }
    }
}
