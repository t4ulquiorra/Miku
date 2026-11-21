package dev.anilbeesetti.nextplayer.core.ui.theme

import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// ==========================================================================
// CORNER RADII TOKENS (prototype.html)
// ==========================================================================
val CornerRadiusExtraSmall = 6.dp
val CornerRadiusSmall = 8.dp
val CornerRadiusMedium = 12.dp
val CornerRadiusLarge = 16.dp
val CornerRadiusExtraLarge = 22.dp
val CornerRadiusRound = 28.dp
val CornerRadiusPill = 30.dp

val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(CornerRadiusExtraSmall),
    small = RoundedCornerShape(CornerRadiusSmall),
    medium = RoundedCornerShape(CornerRadiusMedium),
    large = RoundedCornerShape(CornerRadiusLarge),
    extraLarge = RoundedCornerShape(CornerRadiusExtraLarge),
)

// Active accent CompositionLocal
val LocalAppAccent = staticCompositionLocalOf { AppAccent.current }

/**
 * Builds a dark-only ColorScheme using the prototype design tokens.
 * Accent color is driven by the given AppAccent (defaulting to AppAccent.current).
 */
fun createPrototypeColorScheme(
    accent: AppAccent = AppAccent.current,
    pureBlack: Boolean = false,
): ColorScheme {
    val bg = if (pureBlack) Color(0xFF000000) else PrototypeBg
    val surfaceDeep = if (pureBlack) Color(0xFF070605) else PrototypeSurfaceDeep
    val surface = if (pureBlack) Color(0xFF0A0907) else PrototypeSurface
    val surfaceKey = if (pureBlack) Color(0xFF12100B) else PrototypeSurfaceKey

    return darkColorScheme(
        primary = accent.color,
        onPrimary = PrototypeBg,
        primaryContainer = accent.shadow,
        onPrimaryContainer = PrototypeInk,
        inversePrimary = accent.color,

        secondary = PrototypeInkSec,
        onSecondary = PrototypeBg,
        secondaryContainer = surfaceKey,
        onSecondaryContainer = PrototypeInk,

        tertiary = PrototypePaper0,
        onTertiary = PrototypeInkPaper,
        tertiaryContainer = PrototypePaper3,
        onTertiaryContainer = PrototypeInkPaper,

        background = bg,
        onBackground = PrototypeInk,

        surface = surface,
        onSurface = PrototypeInk,
        surfaceVariant = surfaceDeep,
        onSurfaceVariant = PrototypeInkSec,

        surfaceDim = surfaceDeep,
        surfaceBright = surfaceKey,
        surfaceContainerLowest = bg,
        surfaceContainerLow = surfaceDeep,
        surfaceContainer = surface,
        surfaceContainerHigh = surfaceKey,
        surfaceContainerHighest = PrototypeBorderStrong,

        outline = PrototypeBorderInput,
        outlineVariant = PrototypeBorder,

        error = PrototypeStamp,
        onError = PrototypeBg,
        errorContainer = accent.shadow,
        onErrorContainer = PrototypeInk,

        scrim = Color(0xFF000000),
        inverseSurface = PrototypeInk,
        inverseOnSurface = PrototypeBg,
    )
}

@Composable
fun NextPlayerTheme(
    darkTheme: Boolean = true,
    highContrastDarkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    accent: AppAccent = AppAccent.current,
    content: @Composable () -> Unit,
) {
    // Dark-only scheme using prototype colors (dynamic color disabled)
    val colorScheme = createPrototypeColorScheme(
        accent = accent,
        pureBlack = highContrastDarkTheme,
    )

    CompositionLocalProvider(
        LocalAppAccent provides accent,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = AppShapes,
            motionScheme = MotionScheme.expressive(),
            content = content,
        )
    }
}

@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.S)
fun supportsDynamicTheming() = false
