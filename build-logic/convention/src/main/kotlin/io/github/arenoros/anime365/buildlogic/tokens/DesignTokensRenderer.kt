package io.github.arenoros.anime365.buildlogic.tokens

import org.gradle.api.GradleException
import kotlin.math.roundToInt

/**
 * Переводит tokens.json дизайн-системы Kadr в Kotlin для Compose.
 *
 * Тени (`shadow`) не переносятся: в Compose глубину задаёт elevation, а не CSS box-shadow.
 */
internal class DesignTokensRenderer(
    private val packageName: String,
) {
    fun render(tokens: Any?): String {
        val root = tokens.obj("корень")
        return buildString {
            appendLine("// Сгенерировано задачей generate*DesignTokens из tokens/kadr-tokens.json. Не править руками.")
            appendLine("package $packageName")
            appendLine()
            appendLine("import androidx.compose.runtime.Immutable")
            appendLine("import androidx.compose.ui.graphics.Color")
            appendLine("import androidx.compose.ui.text.TextStyle")
            appendLine("import androidx.compose.ui.text.font.FontWeight")
            appendLine("import androidx.compose.ui.unit.dp")
            appendLine("import androidx.compose.ui.unit.sp")
            appendLine()
            renderPalette(root.obj("color"))
            renderTypography(root.obj("type"))
            renderDimensions("KadrSpacing", root.obj("spacing"), stripPrefix = "")
            renderDimensions("KadrRadius", root.obj("radius"), stripPrefix = "radius-")
            renderLayout(root.obj("layout"))
        }
    }

    private fun StringBuilder.renderPalette(color: Map<String, Any?>) {
        val themes = color.array("themes").map { it.obj("color.themes").str("id") }
        val tokens = color.array("tokens").map { it.obj("color.tokens") }
        appendLine("@Immutable")
        appendLine("data class KadrPalette(")
        tokens.forEach { appendLine("    val ${camel(it.str("name"))}: Color,") }
        appendLine(")")
        themes.forEach { theme ->
            val values = tokens.associate { it.str("name") to it.obj("value").str(theme) }
            appendLine()
            appendLine("val Kadr${pascal(theme)}Palette = KadrPalette(")
            values.keys.forEach { name -> appendLine("    ${camel(name)} = ${color(name, values)},") }
            appendLine(")")
        }
        appendLine()
    }

    private fun color(
        name: String,
        theme: Map<String, String>,
        path: List<String> = emptyList(),
    ): String {
        if (name in path) fail("цикл ссылок между цветами: ${(path + name).joinToString(" -> ")}")
        val raw = theme[name] ?: fail("нет цвета '$name'")
        REFERENCE.matchEntire(raw)?.let { return color(it.groupValues[1], theme, path + name) }
        if (HEX.matches(raw)) return "Color(0xFF${raw.drop(1).uppercase()})"
        val rgba = RGBA.matchEntire(raw) ?: fail("цвет '$name' в неизвестном формате: $raw")
        val (r, g, b) = rgba.groupValues.subList(1, 4).map { channel(it, name) }
        val alpha = (rgba.groupValues[4].toDouble() * MAX_CHANNEL).roundToInt()
        return "Color(0x%02X%02X%02X%02X)".format(alpha, r, g, b)
    }

    private fun channel(
        value: String,
        name: String,
    ): Int = value.toInt().takeIf { it in 0..MAX_CHANNEL } ?: fail("канал цвета '$name' вне 0..255: $value")

    private fun StringBuilder.renderTypography(type: Map<String, Any?>) {
        appendLine("object KadrTypography {")
        type
            .array("groups")
            .flatMap { it.obj("type.groups").array("styles") }
            .map { it.obj("type.styles") }
            .forEach { style ->
                val args =
                    buildList {
                        add("fontSize = ${number(style.str("fontSize"))}.sp")
                        add("lineHeight = ${number(style.str("lineHeight"))}.sp")
                        add("fontWeight = FontWeight(${style.int("fontWeight")})")
                        (style["letterSpacing"] as? String)?.let { add("letterSpacing = ${number(it)}.sp") }
                    }
                appendLine("    val ${pascal(style.str("name"))} = TextStyle(${args.joinToString()})")
            }
        appendLine("}")
        appendLine()
    }

    private fun StringBuilder.renderDimensions(
        objectName: String,
        group: Map<String, Any?>,
        stripPrefix: String,
    ) {
        appendLine("object $objectName {")
        group.array("tokens").map { it.obj("$objectName.tokens") }.forEach { token ->
            val name = pascal(token.str("name").removePrefix(stripPrefix))
            appendLine("    val $name = ${number(token.str("value"))}.dp")
        }
        appendLine("}")
        appendLine()
    }

    private fun StringBuilder.renderLayout(layout: Map<String, Any?>) {
        appendLine("object KadrLayout {")
        layout.array("tokens").map { it.obj("layout.tokens") }.forEach { token ->
            val name = pascal(token.str("name"))
            val value = token.str("value")
            val ratio = RATIO.matchEntire(value)
            val line =
                when {
                    value.endsWith(PX) -> "val $name = ${number(value)}.dp"
                    ratio != null -> "const val $name = ${ratio.groupValues[1]}f / ${ratio.groupValues[2]}f"
                    INTEGER.matches(value) -> "const val $name = $value"
                    else -> fail("значение '$name' в неизвестном формате: $value")
                }
            appendLine("    $line")
        }
        appendLine("}")
    }

    /** `"24px"` -> `24`, `"1.25px"` -> `1.25`. */
    private fun number(value: String): String {
        val digits = value.removeSuffix(PX)
        val parsed = digits.toDoubleOrNull() ?: fail("ожидалось число в px: $value")
        return if (parsed == parsed.toLong().toDouble()) parsed.toLong().toString() else digits
    }

    private fun pascal(name: String): String {
        val result =
            name
                .split(
                    '-',
                ).filter { it.isNotEmpty() }
                .joinToString("") { it.replaceFirstChar(Char::uppercaseChar) }
        if (!IDENTIFIER.matches(result)) fail("имя '$name' не превращается в идентификатор Kotlin")
        return result
    }

    private fun camel(name: String): String = pascal(name).replaceFirstChar(Char::lowercaseChar)

    private companion object {
        const val PX = "px"
        const val MAX_CHANNEL = 255
        val REFERENCE = Regex("""\{([a-z0-9-]+)}""")
        val HEX = Regex("#[0-9a-fA-F]{6}")
        val RGBA = Regex("""rgba\(\s*(\d{1,3})\s*,\s*(\d{1,3})\s*,\s*(\d{1,3})\s*,\s*([01](?:\.\d+)?)\s*\)""")
        val RATIO = Regex("""(\d+)\s*/\s*(\d+)""")
        val INTEGER = Regex("""\d+""")
        val IDENTIFIER = Regex("[A-Za-z][A-Za-z0-9]*")
    }
}

@Suppress("UNCHECKED_CAST")
private fun Any?.obj(where: String): Map<String, Any?> =
    this as? Map<String, Any?> ?: fail("ожидался объект в '$where'")

private fun Map<String, Any?>.obj(key: String): Map<String, Any?> = this[key].obj(key)

private fun Map<String, Any?>.array(key: String): List<Any?> =
    this[key] as? List<Any?> ?: fail("ожидался массив '$key'")

private fun Map<String, Any?>.str(key: String): String = this[key] as? String ?: fail("ожидалась строка '$key'")

private fun Map<String, Any?>.int(key: String): Int = (this[key] as? Number)?.toInt() ?: fail("ожидалось число '$key'")

private fun fail(message: String): Nothing = throw GradleException("kadr-tokens.json: $message")
