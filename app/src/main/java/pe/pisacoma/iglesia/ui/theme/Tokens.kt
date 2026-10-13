package pe.pisacoma.iglesia.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * The site's design tokens, verbatim, under the site's own names. Every value here names the
 * CSS it came from (quiz-qwik/src/...). Nothing is eyeballed: a colour that is not here is
 * added here with its source before it is used.
 */
object Tokens {
    // ---- global.css :root (the dark base used by quiz, quien, videos) ----
    val bg = Color(0xFF0D1117)           // --bg
    val bg2 = Color(0xFF161B22)          // --bg-2
    val border = Color(0xFF21262D)       // --border
    val primary = Color(0xFF1E3A5F)      // --primary
    val primaryLight = Color(0xFF2D5A8E) // --primary-light
    val accent = Color(0xFFC9963B)       // --accent
    val accentLight = Color(0xFFE8B85A)  // --accent-light
    val text = Color(0xEBFFFFFF)         // --text rgba(255,255,255,.92)
    val textMuted = Color(0x8CFFFFFF)    // --text-muted rgba(255,255,255,.55)
    val textFaint = Color(0x80FFFFFF)    // --text-faint rgba(255,255,255,.5)
    val green = Color(0xFF3BD68A)        // --green
    val red = Color(0xFFE65B5B)          // --red

    // ---- utils/pastel-theme.ts + the cream routes (the pastel design system) ----
    val cream = Color(0xFFF5F3E6)        // body background
    val creamNav = Color(0xDBF7F4E6)     // .site-nav rgba(247,244,230,.86)
    val ink = Color(0xFF1F2B1C)          // body color
    val forest = Color(0xFF2C4128)       // brand strong, headings
    val sage = Color(0xFF4A6741)         // brand mark, accents
    val sageText = Color(0xFF627253)     // brand span, footer text
    val sageSoft = Color(0xFF6F8E57)     // .mm-num
    val inkDeep = Color(0xFF16241A)      // .mm-title
    val sageLeaf = Color(0xFF7A9D52)     // rgba(122,157,82,…) radial glow / icon tiles
    val ochrePastel = Color(0xFFB8904F)  // rgba(184,144,79,…) radial glow
    val menuTop = Color(0xFFF7F4EA)      // .mm-panel gradient start
    val menuBottom = Color(0xFFECE9D6)   // .mm-panel gradient end

    /** rgba(74,103,65,a) — the sage used at an alpha all over the cream chrome. */
    fun sageA(alpha: Float) = sage.copy(alpha = alpha)
    /** rgba(122,157,82,a) */
    fun leafA(alpha: Float) = sageLeaf.copy(alpha = alpha)

    // ---- routes/index.tsx — Pampa Abierta palette of the home ----
    val basalt = Color(0xFF2A2620)
    val parchment = Color(0xFFF0E8D0)
    val olive = Color(0xFF5D6B3F)
    val ichu = Color(0xFF7D8A5A)
    val olivePale = Color(0xFFD4D6B0)
    val ochre = Color(0xFFC08428)
    val ochreLight = Color(0xFFD9B070)
    val ochreDeep = Color(0xFF8A5418)
    val titicaca = Color(0xFF1C5D8E)
}

/** Layout constants from the site's CSS (px → dp one to one, as on a 390 px phone). */
object Metrics {
    const val navHeight = 66          // .site-nav: 14px padding ×2 + 38px content (42px buttons − overlap)
    const val navPaddingH = 20        // .nav-inner @ ≤840px
    const val brandMarkW = 40         // .brand-mark svg
    const val brandMarkH = 36
    const val navButton = 42          // .nav-search / .hamburger
    const val navButtonRadius = 12
    const val menuPaddingH = 22       // .mm-panel @ ≤480px
    const val menuPaddingTop = 76
    const val menuIcon = 40           // .mm-icon @ ≤480px
    const val menuIconRadius = 13
    const val menuRowRadius = 14
}
