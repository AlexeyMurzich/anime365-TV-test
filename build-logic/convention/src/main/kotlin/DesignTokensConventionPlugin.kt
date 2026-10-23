import com.android.build.api.variant.LibraryAndroidComponentsExtension
import io.github.arenoros.anime365.buildlogic.tokens.GenerateDesignTokensTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.register

/** Генерирует Kotlin-код темы из `tokens/kadr-tokens.json` модуля (ADR 0002). */
class DesignTokensConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            val tokens = layout.projectDirectory.file("tokens/kadr-tokens.json")
            extensions.configure<LibraryAndroidComponentsExtension> {
                onVariants { variant ->
                    val task =
                        tasks.register<GenerateDesignTokensTask>(
                            "generate${variant.name.replaceFirstChar(Char::uppercaseChar)}DesignTokens",
                        ) {
                            tokensFile.set(tokens)
                            packageName.set("${variant.namespace.get()}.tokens")
                        }
                    variant.sources.kotlin?.addGeneratedSourceDirectory(task, GenerateDesignTokensTask::outputDir)
                }
            }
        }
    }
}
