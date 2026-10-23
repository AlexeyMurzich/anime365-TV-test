package io.github.arenoros.anime365.core.designsystem.tokens

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.junit.Assert.assertEquals
import org.junit.Test

/** Проверяет генератор целиком: значения взяты прямо из kadr-tokens.json. */
class KadrTokensTest {
    @Test
    fun hexColorsPerTheme() {
        assertEquals(Color(0xFF1B5E20), KadrLightPalette.brand)
        assertEquals(Color(0xFF2E7D32), KadrLightPalette.primary)
        assertEquals(Color(0xFF81C784), KadrDarkPalette.primary)
    }

    @Test
    fun rgbaColorKeepsAlpha() {
        assertEquals(Color(0x9E000000), KadrLightPalette.scrim)
    }

    @Test
    fun referenceResolvesWithinTheme() {
        assertEquals(KadrLightPalette.primary, KadrLightPalette.focusRing)
        assertEquals(KadrDarkPalette.primary, KadrDarkPalette.focusRing)
    }

    @Test
    fun typographyInSp() {
        assertEquals(24.sp, KadrTypography.Headline.fontSize)
        assertEquals(1.25.sp, KadrTypography.Button.letterSpacing)
        assertEquals(500, KadrTypography.Button.fontWeight?.weight)
    }

    @Test
    fun dimensionsAndLayout() {
        assertEquals(16.dp, KadrSpacing.Space4)
        assertEquals(2.dp, KadrRadius.Xs)
        assertEquals(56.dp, KadrLayout.AppBarHeight)
        assertEquals(2f / 3f, KadrLayout.PosterRatio)
        assertEquals(3, KadrLayout.GridColumnsPhone)
    }
}
