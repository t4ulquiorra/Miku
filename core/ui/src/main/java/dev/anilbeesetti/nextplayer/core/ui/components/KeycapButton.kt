package dev.anilbeesetti.nextplayer.core.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.anilbeesetti.nextplayer.core.ui.theme.LocalAppAccent
import dev.anilbeesetti.nextplayer.core.ui.theme.PrototypeBorderStrong
import dev.anilbeesetti.nextplayer.core.ui.theme.PrototypeKeyShadow
import dev.anilbeesetti.nextplayer.core.ui.theme.PrototypeSurfaceKey

/**
 * Reusable KeycapButton with raised physical appearance and bottom shadow
 * that sinks upon press, matching the prototype tactile keycap design.
 */
@Composable
fun KeycapButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isAccent: Boolean = false,
    containerColor: Color = Color.Unspecified,
    contentColor: Color = Color.Unspecified,
    shadowColor: Color = Color.Unspecified,
    borderColor: Color = Color.Unspecified,
    shape: Shape = RoundedCornerShape(16.dp),
    shadowHeight: Dp = 5.dp,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    contentPadding: PaddingValues = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
    content: @Composable RowScope.() -> Unit,
) {
    val accent = LocalAppAccent.current

    val resolvedContainerColor = when {
        containerColor != Color.Unspecified -> containerColor
        isAccent -> accent.color
        else -> PrototypeSurfaceKey
    }

    val resolvedContentColor = when {
        contentColor != Color.Unspecified -> contentColor
        isAccent -> Color(0xFF0D0C0A)
        else -> MaterialTheme.colorScheme.onSurface
    }

    val resolvedShadowColor = when {
        shadowColor != Color.Unspecified -> shadowColor
        isAccent -> accent.shadow
        else -> PrototypeKeyShadow
    }

    val resolvedBorderColor = when {
        borderColor != Color.Unspecified -> borderColor
        isAccent -> accent.color
        else -> PrototypeBorderStrong
    }

    val isPressed by interactionSource.collectIsPressedAsState()
    val isFocused by interactionSource.collectIsFocusedAsState()

    val pressOffset by animateDpAsState(
        targetValue = if (isPressed && enabled) shadowHeight else 0.dp,
        animationSpec = tween(durationMillis = 80),
        label = "keycap_sink",
    )

    val activeBorderColor = if (isFocused) accent.color else resolvedBorderColor

    Box(
        modifier = modifier
            .alpha(if (enabled) 1f else 0.45f)
            .padding(bottom = shadowHeight),
    ) {
        // Physical bottom shadow
        Surface(
            modifier = Modifier
                .matchParentSize()
                .offset(y = shadowHeight),
            shape = shape,
            color = resolvedShadowColor,
        ) {}

        // Raised button surface that sinks when pressed
        Surface(
            modifier = Modifier
                .offset(y = pressOffset)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    enabled = enabled,
                    role = Role.Button,
                    onClick = onClick,
                ),
            shape = shape,
            color = resolvedContainerColor,
            contentColor = resolvedContentColor,
            border = BorderStroke(1.dp, activeBorderColor),
        ) {
            CompositionLocalProvider(LocalContentColor provides resolvedContentColor) {
                ProvideTextStyle(
                    value = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                ) {
                    Row(
                        modifier = Modifier.padding(contentPadding),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        content = content,
                    )
                }
            }
        }
    }
}

@Composable
fun KeycapButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isAccent: Boolean = false,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    containerColor: Color = Color.Unspecified,
    contentColor: Color = Color.Unspecified,
    shadowColor: Color = Color.Unspecified,
    borderColor: Color = Color.Unspecified,
    shape: Shape = RoundedCornerShape(16.dp),
    shadowHeight: Dp = 5.dp,
) {
    KeycapButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        isAccent = isAccent,
        containerColor = containerColor,
        contentColor = contentColor,
        shadowColor = shadowColor,
        borderColor = borderColor,
        shape = shape,
        shadowHeight = shadowHeight,
    ) {
        if (leadingIcon != null) {
            leadingIcon()
            Box(modifier = Modifier.padding(start = 8.dp))
        }
        Text(text = text)
        if (trailingIcon != null) {
            Box(modifier = Modifier.padding(start = 8.dp))
            trailingIcon()
        }
    }
}
