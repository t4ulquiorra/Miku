package dev.anilbeesetti.nextplayer.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import dev.anilbeesetti.nextplayer.core.ui.R

// Bundled fonts from prototype.html
val BricolageGrotesque = FontFamily(
    Font(R.font.bricolage_grotesque_regular, FontWeight.Normal),
    Font(R.font.bricolage_grotesque_medium, FontWeight.Medium),
    Font(R.font.bricolage_grotesque_bold, FontWeight.Bold),
)

val JetBrainsMono = FontFamily(
    Font(R.font.jetbrains_mono_regular, FontWeight.Normal),
    Font(R.font.jetbrains_mono_medium, FontWeight.Medium),
    Font(R.font.jetbrains_mono_bold, FontWeight.Bold),
)

private val defaultTypography = Typography()

// Whole app uses Bricolage Grotesque across all Material3 typography styles
val Typography = Typography(
    displayLarge = defaultTypography.displayLarge.copy(fontFamily = BricolageGrotesque),
    displayMedium = defaultTypography.displayMedium.copy(fontFamily = BricolageGrotesque),
    displaySmall = defaultTypography.displaySmall.copy(fontFamily = BricolageGrotesque),
    headlineLarge = defaultTypography.headlineLarge.copy(fontFamily = BricolageGrotesque),
    headlineMedium = defaultTypography.headlineMedium.copy(fontFamily = BricolageGrotesque),
    headlineSmall = defaultTypography.headlineSmall.copy(fontFamily = BricolageGrotesque),
    titleLarge = defaultTypography.titleLarge.copy(fontFamily = BricolageGrotesque),
    titleMedium = defaultTypography.titleMedium.copy(fontFamily = BricolageGrotesque),
    titleSmall = defaultTypography.titleSmall.copy(fontFamily = BricolageGrotesque),
    bodyLarge = defaultTypography.bodyLarge.copy(fontFamily = BricolageGrotesque),
    bodyMedium = defaultTypography.bodyMedium.copy(fontFamily = BricolageGrotesque),
    bodySmall = defaultTypography.bodySmall.copy(fontFamily = BricolageGrotesque),
    labelLarge = defaultTypography.labelLarge.copy(fontFamily = BricolageGrotesque),
    labelMedium = defaultTypography.labelMedium.copy(fontFamily = BricolageGrotesque),
    labelSmall = defaultTypography.labelSmall.copy(fontFamily = BricolageGrotesque),
)
