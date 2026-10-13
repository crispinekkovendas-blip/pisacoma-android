package pe.pisacoma.iglesia.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pe.pisacoma.iglesia.ui.theme.Fraunces
import pe.pisacoma.iglesia.ui.theme.Tokens

/**
 * The readers' text-size steps (site: a signal from 0.92 to 1.42 rem in 0.08 steps, default 1.05).
 * Pure arithmetic kept here so the three readers (lección, biblia, EGW) agree.
 */
object TextScale {
    const val DEFAULT = 1.05f
    const val MIN = 0.92f
    const val MAX = 1.42f
    const val STEP = 0.08f
    fun smaller(s: Float) = maxOf(MIN, s - STEP)
    fun larger(s: Float) = minOf(MAX, s + STEP)
}

/** A 32 dp toolbar button (`.tb-icon`, 38 px on the site; 32 dp so six fit a 360 dp phone): sage outline, pressed/active fill. */
@Composable
fun ToolbarIcon(
    iconRes: Int? = null,
    label: String,
    active: Boolean = false,
    testTag: String = "",
    onClick: () -> Unit,
    content: (@Composable () -> Unit)? = null,
) {
    val shape = RoundedCornerShape(10.dp)
    Box(
        Modifier
            .size(32.dp)
            .clip(shape)
            .background(if (active) Tokens.forest else Tokens.sageA(0.06f))
            .border(1.dp, if (active) Tokens.forest else Tokens.sageA(0.20f), shape)
            .pressable(onClick = onClick)
            .testTag(testTag)
            .semantics { contentDescription = label },
        contentAlignment = Alignment.Center,
    ) {
        if (content != null) content()
        else if (iconRes != null) Icon(painterResource(iconRes), null, tint = if (active) Tokens.cream else Tokens.forest, modifier = Modifier.size(15.dp))
    }
}

/** The «A» / «A» pair (`.tb-letter sm` / `lg`). */
@Composable
fun TextSizeControls(onSmaller: () -> Unit, onLarger: () -> Unit) {
    ToolbarIcon(label = "Texto más pequeño", testTag = "text-smaller", onClick = onSmaller) {
        Text("A", fontFamily = Fraunces, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Tokens.forest)
    }
    ToolbarIcon(label = "Texto más grande", testTag = "text-larger", onClick = onLarger) {
        Text("A", fontFamily = Fraunces, fontWeight = FontWeight.SemiBold, fontSize = 17.sp, color = Tokens.forest)
    }
}
