package pe.pisacoma.iglesia.ui.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight

// The site's three families, converted from its woff2 files by tools/fonts.mjs. Each is a
// variable font, so one file serves every weight through the wght axis (applied on API 26+;
// older phones get the default instance with synthetic bold).

private fun variable(resId: Int, weight: FontWeight, style: FontStyle = FontStyle.Normal) =
    Font(resId, weight, style, variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight)))

private val WEIGHTS = listOf(FontWeight.Normal, FontWeight.Medium, FontWeight.SemiBold, FontWeight.Bold)

val Fraunces = FontFamily(
    WEIGHTS.map { variable(pe.pisacoma.iglesia.R.font.fraunces, it) } +
        WEIGHTS.map { variable(pe.pisacoma.iglesia.R.font.fraunces_italic, it, FontStyle.Italic) },
)

val Inter = FontFamily(WEIGHTS.map { variable(pe.pisacoma.iglesia.R.font.inter, it) })

val DmSans = FontFamily(WEIGHTS.map { variable(pe.pisacoma.iglesia.R.font.dm_sans, it) })

/** The dark routes' heading face (site global.css h1–h3 and quiz-view.css): Playfair Display 700–800. */
val Playfair = FontFamily(listOf(FontWeight.SemiBold, FontWeight.Bold, FontWeight.ExtraBold).map { variable(pe.pisacoma.iglesia.R.font.playfair, it) })
