import com.android.build.api.dsl.LibraryExtension
import com.android.build.api.variant.HostTestBuilder
import com.android.build.api.variant.LibraryAndroidComponentsExtension
import io.github.arenoros.anime365.buildlogic.configureAndroid
import io.github.arenoros.anime365.buildlogic.configureQualityChecks
import io.github.arenoros.anime365.buildlogic.defaultNamespace
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.android.library")
            configureQualityChecks()
            extensions.configure<LibraryExtension> {
                configureAndroid(this)
                namespace = defaultNamespace
            }
            extensions.configure<LibraryAndroidComponentsExtension> {
                // Unit-тесты библиотек гоняем только на debug: release ничего не добавляет, а время удваивает.
                beforeVariants(selector().withBuildType("release")) {
                    it.hostTests[HostTestBuilder.UNIT_TEST_TYPE]?.enable = false
                }
            }
        }
    }
}
