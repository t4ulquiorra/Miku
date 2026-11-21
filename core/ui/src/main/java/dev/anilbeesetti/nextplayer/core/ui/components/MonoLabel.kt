package dev.anilbeesetti.nextplayer.core.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import dev.anilbeesetti.nextplayer.core.ui.theme.JetBrainsMono
import dev.anilbeesetti.nextplayer.core.ui.theme.PrototypeInkMuted

/**
 * Reusable MonoLabel composable for small, letter-spaced, uppercase typography in JetBrains Mono.
 */
@Composable
fun MonoLabel(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = 11.sp,
    fontWeight: FontWeight = FontWeight.Medium,
    letterSpacing: TextUnit = 0.14.em,
    textAlign: TextAlign? = null,
    overflow: TextOverflow = TextOverflow.Clip,
    maxLines: Int = Int.MAX_VALUE,
) {
    Text(
        text = text.uppercase(),
        modifier = modifier,
        color = if (color != Color.Unspecified) color else PrototypeInkMuted,
        fontSize = fontSize,
        fontWeight = fontWeight,
        fontFamily = JetBrainsMono,
        letterSpacing = letterSpacing,
        textAlign = textAlign,
        overflow = overflow,
        maxLines = maxLines,
    )
}
