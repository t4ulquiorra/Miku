package dev.anilbeesetti.nextplayer.core.ui.theme

import androidx.compose.ui.graphics.Color

// ==========================================================================
// DESIGN TOKENS — STRICT 3-COLOR SWISS EDITORIAL PALETTE (prototype.html)
// ==========================================================================

// Background & Surfaces
val PrototypeBg = Color(0xFF0D0C0A)
val PrototypeSurfaceDeep = Color(0xFF14120D)
val PrototypeSurface = Color(0xFF17150F)
val PrototypeSurfaceKey = Color(0xFF1B1812)

// Borders
val PrototypeBorder = Color(0xFF26221A)
val PrototypeBorderStrong = Color(0xFF2E2819)
val PrototypeBorderInput = Color(0xFF3A3220)

// Ink (Typography)
val PrototypeInk = Color(0xFFECE4D2)
val PrototypeInkSec = Color(0xFFB9B09B)
val PrototypeInkMuted = Color(0xFF8F8774)
val PrototypeInkDim = Color(0xFF6F6858)
val PrototypeInkPaper = Color(0xFF1A150C)

// Accents (from ACCENTS in prototype.html)
val PrototypeAccentOrange = Color(0xFFFF5A2E)
val PrototypeAccentOrangeShadow = Color(0xFF7A2A12)

val PrototypeAccentLime = Color(0xFFC8F23D)
val PrototypeAccentLimeShadow = Color(0xFF546812)

val PrototypeAccentMint = Color(0xFF7FE7D0)
val PrototypeAccentMintShadow = Color(0xFF266859)

val PrototypeAccentAmber = Color(0xFFFFC247)
val PrototypeAccentAmberShadow = Color(0xFF7A540E)

// Key & Stamp
val PrototypeKeyShadow = Color(0xFF070605)
val PrototypeStamp = Color(0xFFC23A12)

// Paper
val PrototypePaper0 = Color(0xFFD9CBA6)
val PrototypePaper1 = Color(0xFFCDBE96)
val PrototypePaper2 = Color(0xFFE2D6B8)
val PrototypePaper3 = Color(0xFFC4B48A)

/**
 * 4 Swappable Accents extracted from ACCENTS list in prototype.html:
 * - International Orange (#FF5A2E, shadow #7A2A12)
 * - Acid Lime (#C8F23D, shadow #546812)
 * - Archival Mint (#7FE7D0, shadow #266859)
 * - Studio Amber (#FFC247, shadow #7A540E)
 */
data class AppAccent(
    val name: String,
    val color: Color,
    val shadow: Color,
) {
    companion object {
        val InternationalOrange = AppAccent(
            name = "International Orange",
            color = PrototypeAccentOrange,
            shadow = PrototypeAccentOrangeShadow,
        )
        val AcidLime = AppAccent(
            name = "Acid Lime",
            color = PrototypeAccentLime,
            shadow = PrototypeAccentLimeShadow,
        )
        val ArchivalMint = AppAccent(
            name = "Archival Mint",
            color = PrototypeAccentMint,
            shadow = PrototypeAccentMintShadow,
        )
        val StudioAmber = AppAccent(
            name = "Studio Amber",
            color = PrototypeAccentAmber,
            shadow = PrototypeAccentAmberShadow,
        )

        val ACCENTS = listOf(
            InternationalOrange,
            AcidLime,
            ArchivalMint,
            StudioAmber,
        )

        /** Single value driving the active accent throughout the app */
        var current: AppAccent = InternationalOrange
    }
}

// ==========================================================================
// Theme Color Role Mappings (Dark-Only Palette)
// ==========================================================================

val primaryDark = PrototypeAccentOrange
val onPrimaryDark = PrototypeBg
val primaryContainerDark = PrototypeAccentOrangeShadow
val onPrimaryContainerDark = PrototypeInk
val secondaryDark = PrototypeInkSec
val onSecondaryDark = PrototypeBg
val secondaryContainerDark = PrototypeSurfaceKey
val onSecondaryContainerDark = PrototypeInk
val tertiaryDark = PrototypePaper0
val onTertiaryDark = PrototypeInkPaper
val tertiaryContainerDark = PrototypePaper3
val onTertiaryContainerDark = PrototypeInkPaper
val errorDark = PrototypeStamp
val onErrorDark = PrototypeBg
val errorContainerDark = PrototypeAccentOrangeShadow
val onErrorContainerDark = PrototypeInk
val backgroundDark = PrototypeBg
val onBackgroundDark = PrototypeInk
val surfaceDark = PrototypeSurface
val onSurfaceDark = PrototypeInk
val surfaceVariantDark = PrototypeSurfaceDeep
val onSurfaceVariantDark = PrototypeInkSec
val outlineDark = PrototypeBorderInput
val outlineVariantDark = PrototypeBorder
val scrimDark = Color(0xFF000000)
val inverseSurfaceDark = PrototypeInk
val inverseOnSurfaceDark = PrototypeBg
val inversePrimaryDark = PrototypeAccentOrange
val surfaceDimDark = PrototypeSurfaceDeep
val surfaceBrightDark = PrototypeSurfaceKey
val surfaceContainerLowestDark = PrototypeBg
val surfaceContainerLowDark = PrototypeSurfaceDeep
val surfaceContainerDark = PrototypeSurface
val surfaceContainerHighDark = PrototypeSurfaceKey
val surfaceContainerHighestDark = PrototypeBorderStrong

val backgroundPureBlack = Color(0xFF000000)
val surfacePureBlack = Color(0xFF000000)
val surfaceDimPureBlack = Color(0xFF000000)
val surfaceBrightPureBlack = Color(0xFF14120D)
val surfaceContainerLowestPureBlack = Color(0xFF000000)
val surfaceContainerLowPureBlack = Color(0xFF070605)
val surfaceContainerPureBlack = Color(0xFF0D0C0A)
val surfaceContainerHighPureBlack = Color(0xFF14120D)
val surfaceContainerHighestPureBlack = Color(0xFF1B1812)

val primaryLight = primaryDark
val onPrimaryLight = onPrimaryDark
val primaryContainerLight = primaryContainerDark
val onPrimaryContainerLight = onPrimaryContainerDark
val secondaryLight = secondaryDark
val onSecondaryLight = onSecondaryDark
val secondaryContainerLight = secondaryContainerDark
val onSecondaryContainerLight = onSecondaryContainerDark
val tertiaryLight = tertiaryDark
val onTertiaryLight = onTertiaryDark
val tertiaryContainerLight = tertiaryContainerDark
val onTertiaryContainerLight = onTertiaryContainerDark
val errorLight = errorDark
val onErrorLight = onErrorDark
val errorContainerLight = errorContainerDark
val onErrorContainerLight = onErrorContainerDark
val backgroundLight = backgroundDark
val onBackgroundLight = onBackgroundDark
val surfaceLight = surfaceDark
val onSurfaceLight = onSurfaceDark
val surfaceVariantLight = surfaceVariantDark
val onSurfaceVariantLight = onSurfaceVariantDark
val outlineLight = outlineDark
val outlineVariantLight = outlineVariantDark
val scrimLight = scrimDark
val inverseSurfaceLight = inverseSurfaceDark
val inverseOnSurfaceLight = inverseOnSurfaceDark
val inversePrimaryLight = inversePrimaryDark
val surfaceDimLight = surfaceDimDark
val surfaceBrightLight = surfaceBrightDark
val surfaceContainerLowestLight = surfaceContainerLowestDark
val surfaceContainerLowLight = surfaceContainerLowDark
val surfaceContainerLight = surfaceContainerDark
val surfaceContainerHighLight = surfaceContainerHighDark
val surfaceContainerHighestLight = surfaceContainerHighestDark

val primaryLightMediumContrast = primaryDark
val onPrimaryLightMediumContrast = onPrimaryDark
val primaryContainerLightMediumContrast = primaryContainerDark
val onPrimaryContainerLightMediumContrast = onPrimaryContainerDark
val secondaryLightMediumContrast = secondaryDark
val onSecondaryLightMediumContrast = onSecondaryDark
val secondaryContainerLightMediumContrast = secondaryContainerDark
val onSecondaryContainerLightMediumContrast = onSecondaryContainerDark
val tertiaryLightMediumContrast = tertiaryDark
val onTertiaryLightMediumContrast = onTertiaryDark
val tertiaryContainerLightMediumContrast = tertiaryContainerDark
val onTertiaryContainerLightMediumContrast = onTertiaryContainerDark
val errorLightMediumContrast = errorDark
val onErrorLightMediumContrast = onErrorDark
val errorContainerLightMediumContrast = errorContainerDark
val onErrorContainerLightMediumContrast = onErrorContainerDark
val backgroundLightMediumContrast = backgroundDark
val onBackgroundLightMediumContrast = onBackgroundDark
val surfaceLightMediumContrast = surfaceDark
val onSurfaceLightMediumContrast = onSurfaceDark
val surfaceVariantLightMediumContrast = surfaceVariantDark
val onSurfaceVariantLightMediumContrast = onSurfaceVariantDark
val outlineLightMediumContrast = outlineDark
val outlineVariantLightMediumContrast = outlineVariantDark
val scrimLightMediumContrast = scrimDark
val inverseSurfaceLightMediumContrast = inverseSurfaceDark
val inverseOnSurfaceLightMediumContrast = inverseOnSurfaceDark
val inversePrimaryLightMediumContrast = inversePrimaryDark
val surfaceDimLightMediumContrast = surfaceDimDark
val surfaceBrightLightMediumContrast = surfaceBrightDark
val surfaceContainerLowestLightMediumContrast = surfaceContainerLowestDark
val surfaceContainerLowLightMediumContrast = surfaceContainerLowDark
val surfaceContainerLightMediumContrast = surfaceContainerDark
val surfaceContainerHighLightMediumContrast = surfaceContainerHighDark
val surfaceContainerHighestLightMediumContrast = surfaceContainerHighestDark

val primaryLightHighContrast = primaryDark
val onPrimaryLightHighContrast = onPrimaryDark
val primaryContainerLightHighContrast = primaryContainerDark
val onPrimaryContainerLightHighContrast = onPrimaryContainerDark
val secondaryLightHighContrast = secondaryDark
val onSecondaryLightHighContrast = onSecondaryDark
val secondaryContainerLightHighContrast = secondaryContainerDark
val onSecondaryContainerLightHighContrast = onSecondaryContainerDark
val tertiaryLightHighContrast = tertiaryDark
val onTertiaryLightHighContrast = onTertiaryDark
val tertiaryContainerLightHighContrast = tertiaryContainerDark
val onTertiaryContainerLightHighContrast = onTertiaryContainerDark
val errorLightHighContrast = errorDark
val onErrorLightHighContrast = onErrorDark
val errorContainerLightHighContrast = errorContainerDark
val onErrorContainerLightHighContrast = onErrorContainerDark
val backgroundLightHighContrast = backgroundDark
val onBackgroundLightHighContrast = onBackgroundDark
val surfaceLightHighContrast = surfaceDark
val onSurfaceLightHighContrast = onSurfaceDark
val surfaceVariantLightHighContrast = surfaceVariantDark
val onSurfaceVariantLightHighContrast = onSurfaceVariantDark
val outlineLightHighContrast = outlineDark
val outlineVariantLightHighContrast = outlineVariantDark
val scrimLightHighContrast = scrimDark
val inverseSurfaceLightHighContrast = inverseSurfaceDark
val inverseOnSurfaceLightHighContrast = inverseOnSurfaceDark
val inversePrimaryLightHighContrast = inversePrimaryDark
val surfaceDimLightHighContrast = surfaceDimDark
val surfaceBrightLightHighContrast = surfaceBrightDark
val surfaceContainerLowestLightHighContrast = surfaceContainerLowestDark
val surfaceContainerLowLightHighContrast = surfaceContainerLowDark
val surfaceContainerLightHighContrast = surfaceContainerDark
val surfaceContainerHighLightHighContrast = surfaceContainerHighDark
val surfaceContainerHighestLightHighContrast = surfaceContainerHighestDark

val primaryDarkMediumContrast = primaryDark
val onPrimaryDarkMediumContrast = onPrimaryDark
val primaryContainerDarkMediumContrast = primaryContainerDark
val onPrimaryContainerDarkMediumContrast = onPrimaryContainerDark
val secondaryDarkMediumContrast = secondaryDark
val onSecondaryDarkMediumContrast = onSecondaryDark
val secondaryContainerDarkMediumContrast = secondaryContainerDark
val onSecondaryContainerDarkMediumContrast = onSecondaryContainerDark
val tertiaryDarkMediumContrast = tertiaryDark
val onTertiaryDarkMediumContrast = onTertiaryDark
val tertiaryContainerDarkMediumContrast = tertiaryContainerDark
val onTertiaryContainerDarkMediumContrast = onTertiaryContainerDark
val errorDarkMediumContrast = errorDark
val onErrorDarkMediumContrast = onErrorDark
val errorContainerDarkMediumContrast = errorContainerDark
val onErrorContainerDarkMediumContrast = onErrorContainerDark
val backgroundDarkMediumContrast = backgroundDark
val onBackgroundDarkMediumContrast = onBackgroundDark
val surfaceDarkMediumContrast = surfaceDark
val onSurfaceDarkMediumContrast = onSurfaceDark
val surfaceVariantDarkMediumContrast = surfaceVariantDark
val onSurfaceVariantDarkMediumContrast = onSurfaceVariantDark
val outlineDarkMediumContrast = outlineDark
val outlineVariantDarkMediumContrast = outlineVariantDark
val scrimDarkMediumContrast = scrimDark
val inverseSurfaceDarkMediumContrast = inverseSurfaceDark
val inverseOnSurfaceDarkMediumContrast = inverseOnSurfaceDark
val inversePrimaryDarkMediumContrast = inversePrimaryDark
val surfaceDimDarkMediumContrast = surfaceDimDark
val surfaceBrightDarkMediumContrast = surfaceBrightDark
val surfaceContainerLowestDarkMediumContrast = surfaceContainerLowestDark
val surfaceContainerLowDarkMediumContrast = surfaceContainerLowDark
val surfaceContainerDarkMediumContrast = surfaceContainerDark
val surfaceContainerHighDarkMediumContrast = surfaceContainerHighDark
val surfaceContainerHighestDarkMediumContrast = surfaceContainerHighestDark

val primaryDarkHighContrast = primaryDark
val onPrimaryDarkHighContrast = onPrimaryDark
val primaryContainerDarkHighContrast = primaryContainerDark
val onPrimaryContainerDarkHighContrast = onPrimaryContainerDark
val secondaryDarkHighContrast = secondaryDark
val onSecondaryDarkHighContrast = onSecondaryDark
val secondaryContainerDarkHighContrast = secondaryContainerDark
val onSecondaryContainerDarkHighContrast = onSecondaryContainerDark
val tertiaryDarkHighContrast = tertiaryDark
val onTertiaryDarkHighContrast = onTertiaryDark
val tertiaryContainerDarkHighContrast = tertiaryContainerDark
val onTertiaryContainerDarkHighContrast = onTertiaryContainerDark
val errorDarkHighContrast = errorDark
val onErrorDarkHighContrast = onErrorDark
val errorContainerDarkHighContrast = errorContainerDark
val onErrorContainerDarkHighContrast = onErrorContainerDark
val backgroundDarkHighContrast = backgroundDark
val onBackgroundDarkHighContrast = onBackgroundDark
val surfaceDarkHighContrast = surfaceDark
val onSurfaceDarkHighContrast = onSurfaceDark
val surfaceVariantDarkHighContrast = surfaceVariantDark
val onSurfaceVariantDarkHighContrast = onSurfaceVariantDark
val outlineDarkHighContrast = outlineDark
val outlineVariantDarkHighContrast = outlineVariantDark
val scrimDarkHighContrast = scrimDark
val inverseSurfaceDarkHighContrast = inverseSurfaceDark
val inverseOnSurfaceDarkHighContrast = inverseOnSurfaceDark
val inversePrimaryDarkHighContrast = inversePrimaryDark
val surfaceDimDarkHighContrast = surfaceDimDark
val surfaceBrightDarkHighContrast = surfaceBrightDark
val surfaceContainerLowestDarkHighContrast = surfaceContainerLowestDark
val surfaceContainerLowDarkHighContrast = surfaceContainerLowDark
val surfaceContainerDarkHighContrast = surfaceContainerDark
val surfaceContainerHighDarkHighContrast = surfaceContainerHighDark
val surfaceContainerHighestDarkHighContrast = surfaceContainerHighestDark

val seed = PrototypeAccentOrange
