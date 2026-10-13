package pe.pisacoma.iglesia.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material3.ripple
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role

/**
 * The site's tap feedback for buttons and rows: `:active { transform: scale(.96) }` plus a
 * soft ripple. No hover states anywhere — the site gates them behind (hover: hover), which a
 * phone never matches.
 */
fun Modifier.pressable(
    scale: Float = 0.96f,
    role: Role = Role.Button,
    enabled: Boolean = true,
    onClick: () -> Unit,
): Modifier = composed {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val s by animateFloatAsState(if (pressed) scale else 1f, label = "press")
    this
        .graphicsLayer { scaleX = s; scaleY = s }
        .clickable(
            interactionSource = interaction,
            indication = ripple(),
            enabled = enabled,
            role = role,
            onClick = onClick,
        )
}
