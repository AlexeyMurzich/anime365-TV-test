package io.github.arenoros.anime365.buildlogic.tokens

import groovy.json.JsonSlurper
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

@CacheableTask
abstract class GenerateDesignTokensTask : DefaultTask() {
    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val tokensFile: RegularFileProperty

    @get:Input
    abstract val packageName: Property<String>

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @TaskAction
    fun generate() {
        val tokens = JsonSlurper().parse(tokensFile.get().asFile, Charsets.UTF_8.name())
        val source = DesignTokensRenderer(packageName.get()).render(tokens)
        val root = outputDir.get().asFile
        root.deleteRecursively()
        val file = root.resolve(packageName.get().replace('.', '/')).resolve("KadrTokens.kt")
        file.parentFile.mkdirs()
        file.writeText(source)
    }
}
