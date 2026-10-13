package pe.pisacoma.iglesia.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pe.pisacoma.iglesia.ui.theme.Fraunces
import pe.pisacoma.iglesia.ui.theme.Inter
import pe.pisacoma.iglesia.ui.theme.Tokens

// The cream pages share one editorial vocabulary (site: .eyebrow / h1 with em / .lede / rules).

/** `.eyebrow`: 11px Inter 700 uppercase, 2.4px tracking, sage. The site writes the leading dash in the text. */
@Composable
fun EyebrowLine(text: String, color: Color = Tokens.sage, size: Float = 11f, tracking: Float = 2.4f, modifier: Modifier = Modifier) {
    Text(
        text.uppercase(),
        fontFamily = Inter,
        fontSize = size.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = tracking.sp,
        lineHeight = (size + 4).sp,
        color = color,
        modifier = modifier,
    )
}

/** Builds «Plain text <em>italic sage</em>» the way the site's h1s are written. */
fun titleWithEm(plain: String, em: String, emColor: Color = Tokens.sage, tail: String = ""): AnnotatedString = buildAnnotatedString {
    append(plain)
    if (em.isNotEmpty()) {
        if (plain.isNotEmpty() && !plain.endsWith(" ") && !plain.endsWith("\n")) append(" ")
        withStyle(SpanStyle(fontStyle = FontStyle.Italic, fontWeight = FontWeight.Normal, color = emColor)) { append(em) }
    }
    append(tail)
}

/** `.hero h1`: Fraunces 500, clamp(2rem…) → 32sp on a phone, line-height 1, −0.022em. */
@Composable
fun PageTitle(text: AnnotatedString, size: TextUnit = 32.sp, color: Color = Tokens.inkDeep, modifier: Modifier = Modifier) {
    Text(
        text,
        fontFamily = Fraunces,
        fontWeight = FontWeight.Medium,
        fontSize = size,
        lineHeight = size * 1.02f,
        letterSpacing = size * -0.022f,
        color = color,
        modifier = modifier,
    )
}

/** `.lede`: 16sp Inter, line-height 1.6, muted sage ink. */
@Composable
fun Lede(text: String, color: Color = Color(0xFF4D6044), modifier: Modifier = Modifier) {
    Text(text, fontFamily = Inter, fontSize = 16.sp, lineHeight = 25.6.sp, color = color, modifier = modifier)
}

/** The site's gradient hairline (transparent → sage → ochre → sage → transparent). */
@Composable
fun OrnamentRule(modifier: Modifier = Modifier) {
    Box(
        modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(
                Brush.horizontalGradient(
                    0f to Color.Transparent,
                    0.12f to Tokens.sageA(0.22f),
                    0.5f to Color(0x80B8924F),
                    0.88f to Tokens.sageA(0.22f),
                    1f to Color.Transparent,
                ),
            ),
    )
}

/** A plain hairline in the sage used by the cream pages. */
@Composable
fun Hairline(alpha: Float = 0.12f, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth().height(1.dp).background(Tokens.sageA(alpha)))
}

/** The ochre used for numerals all over the cream pages (`#a78447`) and its darker label tone (`#846939`). */
object Ochre {
    val numeral = Color(0xFFA78447)
    val label = Color(0xFF846939)
    val rule = Color(0xFFB8924F)
}
