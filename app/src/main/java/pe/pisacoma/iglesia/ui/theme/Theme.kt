package pe.pisacoma.iglesia.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp

/**
 * Which of the site's three looks a screen uses. The site repaints body/nav/footer per route:
 * cream + sage (`pastel-theme.ts`) on most routes, the original dark base on quiz / quien /
 * videos, and the Pampa Abierta palette on the home.
 */
enum class Skin { Pastel, Dark, Home }

/** Chrome colours that the top bar, menu and footer read, per skin. */
data class ChromeColors(
    val background: Color,
    val navBackground: Color,
    val navBorder: Color,
    val brandMark: Color,
    val brandStrong: Color,
    val brandSub: Color,
    val navButtonBg: Color,
    val navButtonBorder: Color,
    val navButtonFg: Color,
    val footerText: Color,
    val footerBorder: Color,
    val bodyText: Color,
    val isLight: Boolean,
)

val PastelChrome = ChromeColors(
    background = Tokens.cream,
    navBackground = Tokens.creamNav,
    navBorder = Tokens.sageA(0.12f),
    brandMark = Tokens.sage,
    brandStrong = Tokens.forest,
    brandSub = Tokens.sageText,
    navButtonBg = Tokens.sageA(0.06f),
    navButtonBorder = Tokens.sageA(0.20f),
    navButtonFg = Tokens.forest,
    footerText = Tokens.sageText,
    footerBorder = Tokens.sageA(0.12f),
    bodyText = Tokens.ink,
    isLight = true,
)

val DarkChrome = ChromeColors(
    background = Tokens.bg,
    navBackground = Color(0xE00D1117),            // rgba(13,17,23,.88)
    navBorder = Tokens.border,
    brandMark = Tokens.accentLight,
    brandStrong = Color.White,
    brandSub = Tokens.textFaint,
    navButtonBg = Color(0x0AFFFFFF),              // rgba(255,255,255,.04)
    navButtonBorder = Color(0x1AFFFFFF),          // rgba(255,255,255,.10)
    navButtonFg = Tokens.text,
    footerText = Tokens.textFaint,
    footerBorder = Tokens.border,
    bodyText = Tokens.text,
    isLight = false,
)

/** The home keeps the cream chrome; its own sections paint the Pampa palette. */
val HomeChrome = PastelChrome

val LocalChrome = staticCompositionLocalOf { PastelChrome }
val LocalSkin = staticCompositionLocalOf { Skin.Pastel }

/** The body background of the cream routes: cream with the two soft radial glows. */
fun pastelBodyBrush() = Brush.radialGradient(
    0f to Tokens.leafA(0.20f), 0.55f to Color.Transparent,
    center = androidx.compose.ui.geometry.Offset(0.08f, 0f),
)

private val pastelScheme: ColorScheme = lightColorScheme(
    primary = Tokens.sage,
    onPrimary = Tokens.cream,
    secondary = Tokens.forest,
    background = Tokens.cream,
    onBackground = Tokens.ink,
    surface = Tokens.cream,
    onSurface = Tokens.ink,
    surfaceVariant = Tokens.menuBottom,
    onSurfaceVariant = Tokens.sageText,
    outline = Tokens.sageA(0.20f),
)

private val darkScheme: ColorScheme = darkColorScheme(
    primary = Tokens.accent,
    onPrimary = Tokens.bg,
    secondary = Tokens.accentLight,
    background = Tokens.bg,
    onBackground = Tokens.text,
    surface = Tokens.bg2,
    onSurface = Tokens.text,
    surfaceVariant = Tokens.bg2,
    onSurfaceVariant = Tokens.textMuted,
    outline = Tokens.border,
)

/** Body text is Inter everywhere on the site; headings set Fraunces explicitly. */
private val typography = Typography(
    bodyLarge = TextStyle(fontFamily = Inter, fontSize = 16.sp, lineHeight = 26.sp),
    bodyMedium = TextStyle(fontFamily = Inter, fontSize = 14.sp, lineHeight = 22.sp),
    bodySmall = TextStyle(fontFamily = Inter, fontSize = 12.sp, lineHeight = 18.sp),
    labelLarge = TextStyle(fontFamily = Inter, fontSize = 14.sp),
    labelMedium = TextStyle(fontFamily = Inter, fontSize = 12.sp),
    labelSmall = TextStyle(fontFamily = Inter, fontSize = 10.5.sp),
    titleLarge = TextStyle(fontFamily = Fraunces, fontSize = 22.sp),
    titleMedium = TextStyle(fontFamily = Fraunces, fontSize = 18.sp),
    headlineMedium = TextStyle(fontFamily = Fraunces, fontSize = 28.sp),
    headlineLarge = TextStyle(fontFamily = Fraunces, fontSize = 32.sp),
    displaySmall = TextStyle(fontFamily = Fraunces, fontSize = 36.sp),
)

@Composable
fun PisacomaTheme(skin: Skin, content: @Composable () -> Unit) {
    val chrome = when (skin) {
        Skin.Pastel -> PastelChrome
        Skin.Dark -> DarkChrome
        Skin.Home -> HomeChrome
    }
    // The site ignores the OS dark mode: each route has one fixed look.
    @Suppress("UNUSED_VARIABLE") val ignored = isSystemInDarkTheme()
    CompositionLocalProvider(LocalChrome provides chrome, LocalSkin provides skin) {
        MaterialTheme(
            colorScheme = if (chrome.isLight) pastelScheme else darkScheme,
            typography = typography,
            content = content,
        )
    }
}
