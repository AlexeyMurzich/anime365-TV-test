package io.github.arenoros.anime365.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import io.github.arenoros.anime365.core.designsystem.tokens.KadrDarkPalette
import io.github.arenoros.anime365.core.designsystem.tokens.KadrLightPalette
import io.github.arenoros.anime365.core.designsystem.tokens.KadrPalette
import io.github.arenoros.anime365.core.designsystem.tokens.KadrRadius
import io.github.arenoros.anime365.core.designsystem.tokens.KadrTypography

private val LocalKadrPalette = staticCompositionLocalOf { KadrLightPalette }

/** Роли Kadr, которых нет в Material 3: `brand`, `star`, `ongoing`, `subs` и другие. */
object KadrTheme {
    val palette: KadrPalette
        @Composable
        @ReadOnlyComposable
        get() = LocalKadrPalette.current
}

@Composable
fun KadrTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val palette = if (darkTheme) KadrDarkPalette else KadrLightPalette
    CompositionLocalProvider(LocalKadrPalette provides palette) {
        MaterialTheme(
            colorScheme = palette.toColorScheme(darkTheme),
            typography = KadrMaterialTypography,
            shapes = KadrShapes,
            content = content,
        )
    }
}

/**
 * Все роли Material 3 сводятся к ролям Kadr: иначе компоненты, которые берут secondary, tertiary
 * или surfaceContainer, подмешивали бы базовую фиолетовую палитру Material.
 */
private fun KadrPalette.toColorScheme(darkTheme: Boolean): ColorScheme =
    (if (darkTheme) darkColorScheme() else lightColorScheme()).copy(
        primary = primary,
        onPrimary = onPrimary,
        primaryContainer = primaryContainer,
        onPrimaryContainer = onPrimaryContainer,
        inversePrimary = primary,
        secondary = primary,
        onSecondary = onPrimary,
        secondaryContainer = primaryContainer,
        onSecondaryContainer = onPrimaryContainer,
        tertiary = primary,
        onTertiary = onPrimary,
        tertiaryContainer = primaryContainer,
        onTertiaryContainer = onPrimaryContainer,
        background = background,
        onBackground = ink,
        surface = surface,
        onSurface = ink,
        surfaceVariant = surfaceVariant,
        onSurfaceVariant = inkMuted,
        surfaceTint = surface,
        surfaceBright = surfaceRaised,
        surfaceDim = background,
        surfaceContainerLowest = surface,
        surfaceContainerLow = surface,
        surfaceContainer = surface,
        surfaceContainerHigh = surfaceRaised,
        surfaceContainerHighest = surfaceVariant,
        inverseSurface = ink,
        inverseOnSurface = surface,
        outline = outline,
        outlineVariant = outlineVariant,
        error = error,
        onError = onError,
        errorContainer = error,
        onErrorContainer = onError,
        scrim = scrim,
    )

private val KadrMaterialTypography =
    Typography(
        headlineSmall = KadrTypography.Headline,
        titleLarge = KadrTypography.TitleLg,
        titleMedium = KadrTypography.TitleMd,
        titleSmall = KadrTypography.TitleSm,
        bodyLarge = KadrTypography.BodyLg,
        bodyMedium = KadrTypography.BodyMd,
        bodySmall = KadrTypography.Caption,
        labelLarge = KadrTypography.Button,
        labelMedium = KadrTypography.Label,
        labelSmall = KadrTypography.Overline,
    )

private val KadrShapes =
    Shapes(
        extraSmall = RoundedCornerShape(KadrRadius.Xs),
        small = RoundedCornerShape(KadrRadius.Sm),
        medium = RoundedCornerShape(KadrRadius.Sm),
        large = RoundedCornerShape(KadrRadius.Md),
        extraLarge = RoundedCornerShape(KadrRadius.Md),
    )
