package pe.pisacoma.iglesia.nav

import pe.pisacoma.iglesia.R
import pe.pisacoma.iglesia.ui.theme.Skin

/**
 * One destination per site route, with the site's path shapes (including the query strings
 * the site's own links carry: `/biblia/?libro=43&cap=3&v=16`, `/egw/cc/?ch=2`,
 * `/escuela-sabatica/4/?dia=2`) so a deep link or a bundled href maps one to one.
 */
sealed class Route(val path: String) {
    data object Home : Route("/")
    data object Visitanos : Route("/visitanos")
    data object Himnario : Route("/himnario")
    data class Hymn(val variant: String, val number: Int) : Route("/himnario/$variant/$number")
    data object Videos : Route("/videos")
    data class Video(val id: String) : Route("/videos/$id")
    data object Escuela : Route("/escuela-sabatica")
    data class Leccion(val number: Int, val day: Int? = null) :
        Route("/escuela-sabatica/$number" + (day?.let { "?dia=$it" } ?: ""))
    data class Biblia(val book: Int? = null, val chapter: Int? = null, val verse: Int? = null) :
        Route("/biblia" + if (book != null) "?libro=$book&cap=${chapter ?: 1}" + (verse?.let { "&v=$it" } ?: "") else "")
    data object Diccionario : Route("/diccionario")
    data object Egw : Route("/egw")
    data class EgwBook(val slug: String, val chapter: Int? = null) : Route("/egw/$slug" + (chapter?.let { "?ch=$it" } ?: ""))
    data object Quiz : Route("/quiz")
    data class QuizPack(val slug: String) : Route("/quiz/$slug")
    data object Quien : Route("/quien")
    data object Buscar : Route("/buscar")

    /** The path without its query string, for "is this section active" checks. */
    val base: String get() = path.substringBefore('?')

    /** The look the site gives this route (dark on quiz / quien / videos, Pampa on the home). */
    val skin: Skin
        get() = when (this) {
            is Home -> Skin.Home
            is Videos, is Video, is Quiz, is QuizPack, is Quien -> Skin.Dark
            else -> Skin.Pastel
        }

    companion object {
        /** Navigation Compose patterns. Optional query args are declared on the destination. */
        const val HOME = "/"
        const val VISITANOS = "/visitanos"
        const val HIMNARIO = "/himnario"
        const val HYMN = "/himnario/{variant}/{number}"
        const val VIDEOS = "/videos"
        const val VIDEO = "/videos/{id}"
        const val ESCUELA = "/escuela-sabatica"
        const val LECCION = "/escuela-sabatica/{number}?dia={dia}"
        const val BIBLIA = "/biblia?libro={libro}&cap={cap}&v={v}"
        const val DICCIONARIO = "/diccionario"
        const val EGW = "/egw"
        const val EGW_BOOK = "/egw/{slug}?ch={ch}"
        const val QUIZ = "/quiz"
        const val QUIZ_PACK = "/quiz/{slug}"
        const val QUIEN = "/quien"
        const val BUSCAR = "/buscar"

        /** Parses a site path (from a deep link or a bundled href) into a route, or null. */
        fun parse(path: String): Route? {
            val trimmed = path.trim()
            val query = trimmed.substringAfter('?', "")
            val q = query.split('&').filter { it.contains('=') }
                .associate { it.substringBefore('=') to it.substringAfter('=') }
            val int = { k: String -> q[k]?.toIntOrNull() }
            val p = trimmed.substringBefore('?').trimEnd('/').ifEmpty { "/" }
            val parts = p.trimStart('/').split('/')
            return when {
                p == "/" -> Home
                p == VISITANOS -> Visitanos
                p == HIMNARIO -> Himnario
                parts.size == 3 && parts[0] == "himnario" -> parts[2].toIntOrNull()?.let { Hymn(parts[1], it) }
                p == VIDEOS -> Videos
                parts.size == 2 && parts[0] == "videos" -> Video(parts[1])
                p == ESCUELA -> Escuela
                parts.size == 2 && parts[0] == "escuela-sabatica" -> parts[1].toIntOrNull()?.let { Leccion(it, int("dia")) }
                p == "/biblia" -> Biblia(int("libro"), int("cap"), int("v"))
                p == DICCIONARIO -> Diccionario
                p == EGW -> Egw
                parts.size == 2 && parts[0] == "egw" -> EgwBook(parts[1], int("ch"))
                p == QUIZ -> Quiz
                parts.size == 2 && parts[0] == "quiz" -> QuizPack(parts[1])
                p == QUIEN -> Quien
                p == BUSCAR -> Buscar
                else -> null
            }
        }
    }
}

/** The site's menu (layout.tsx NAV), in order. */
data class NavItem(val route: Route, val label: String, val desc: String, val iconRes: Int)

val NAV_ITEMS = listOf(
    NavItem(Route.Home, "Inicio", "Hub principal", R.drawable.ic_nav_home),
    NavItem(Route.Visitanos, "Visítanos", "Horarios · cómo llegar", R.drawable.ic_nav_pin),
    NavItem(Route.Himnario, "Himnario", "1.823 himnos · audio", R.drawable.ic_nav_music),
    NavItem(Route.Videos, "Videos", "Catálogo · Sermones", R.drawable.ic_nav_video),
    NavItem(Route.Escuela, "Esc. Sabática", "", R.drawable.ic_nav_book), // desc = quarterLabel(now)
    NavItem(Route.Biblia(), "Biblia", "Reina-Valera 1960", R.drawable.ic_nav_cross),
    NavItem(Route.Diccionario, "Diccionario", "Términos · Wiktionary", R.drawable.ic_nav_dict),
    NavItem(Route.Egw, "EGW", "25 libros · 4 pasillos", R.drawable.ic_nav_feather),
    NavItem(Route.Quiz, "Quiz", "Lección de la semana", R.drawable.ic_nav_target),
    NavItem(Route.Quien, "¿Quién?", "Ruleta de nombres", R.drawable.ic_nav_spinner),
    NavItem(Route.Buscar, "Buscar", "Himnos · EGW · Biblia · lecciones", R.drawable.ic_nav_search),
)

/** layout.tsx quarterLabel: "Trimestre 2026·Q4" in Peru time (UTC−5 all year). */
fun quarterLabel(nowUtcMillis: Long): String {
    val lima = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")).apply {
        timeInMillis = nowUtcMillis - 5 * 3600_000L
    }
    val q = lima.get(java.util.Calendar.MONTH) / 3 + 1
    return "Trimestre ${lima.get(java.util.Calendar.YEAR)}·Q$q"
}
