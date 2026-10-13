package pe.pisacoma.iglesia.nav

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import pe.pisacoma.iglesia.feature.biblia.BibleScreen
import pe.pisacoma.iglesia.feature.buscar.SearchScreen
import pe.pisacoma.iglesia.feature.descargas.DescargasScreen
import pe.pisacoma.iglesia.feature.diccionario.DictionaryScreen
import pe.pisacoma.iglesia.feature.quien.WheelScreen
import pe.pisacoma.iglesia.feature.videos.VideoPlayerScreen
import pe.pisacoma.iglesia.feature.videos.VideosScreen
import pe.pisacoma.iglesia.feature.visitanos.VisitanosScreen
import pe.pisacoma.iglesia.feature.egw.EgwBookScreen
import pe.pisacoma.iglesia.feature.egw.EgwCatalogScreen
import pe.pisacoma.iglesia.feature.escuela.LessonScreen
import pe.pisacoma.iglesia.feature.escuela.QuarterScreen
import pe.pisacoma.iglesia.feature.himnario.HymnScreen
import pe.pisacoma.iglesia.feature.home.HomeScreen
import pe.pisacoma.iglesia.feature.quiz.QuizIndexScreen
import pe.pisacoma.iglesia.feature.quiz.QuizScreen
import pe.pisacoma.iglesia.feature.himnario.HymnalIndexScreen
import pe.pisacoma.iglesia.ui.chrome.SiteScaffold
import pe.pisacoma.iglesia.ui.theme.LocalChrome
import pe.pisacoma.iglesia.ui.theme.Fraunces
import pe.pisacoma.iglesia.ui.theme.PisacomaTheme
import pe.pisacoma.iglesia.ui.theme.Skin

/**
 * Navigation Compose graph with one destination per site route. [after] is where a first open
 * continues once the phone has chosen what to keep (a deep link, else the home).
 */
@Composable
fun AppNavHost(nav: NavHostController = rememberNavController(), start: Route = Route.Home, after: Route? = null) {
    val go: (Route) -> Unit = { r ->
        nav.navigate(r.path) {
            launchSingleTop = true
        }
    }
    // «Reintentar» on the missing-content notice: a fresh destination (and view model) for the same route.
    val reload: (Route) -> Unit = { r ->
        nav.navigate(r.path) {
            popUpTo(r.path) { inclusive = true }
        }
    }
    NavHost(nav, startDestination = start.path) {
        screen(Route.HOME, { Route.Home }, go, reload)
        screen(Route.VISITANOS, { Route.Visitanos }, go, reload)
        screen(Route.HIMNARIO, { Route.Himnario }, go, reload)
        screen(
            Route.HYMN,
            { Route.Hymn(it.getString("variant")!!, it.getInt("number")) },
            go, reload,
            listOf(navArgument("variant") { type = NavType.StringType }, navArgument("number") { type = NavType.IntType }),
        )
        screen(Route.VIDEOS, { Route.Videos }, go, reload)
        screen(Route.VIDEO, { Route.Video(it.getString("id")!!) }, go, reload, listOf(navArgument("id") { type = NavType.StringType }))
        screen(Route.ESCUELA, { Route.Escuela }, go, reload)
        screen(
            Route.LECCION,
            { Route.Leccion(it.getInt("number"), it.getString("dia")?.toIntOrNull()) },
            go, reload,
            listOf(navArgument("number") { type = NavType.IntType }, optional("dia")),
        )
        screen(
            Route.BIBLIA,
            { Route.Biblia(it.getString("libro")?.toIntOrNull(), it.getString("cap")?.toIntOrNull(), it.getString("v")?.toIntOrNull()) },
            go, reload,
            listOf(optional("libro"), optional("cap"), optional("v")),
        )
        screen(Route.DICCIONARIO, { Route.Diccionario }, go, reload)
        screen(Route.EGW, { Route.Egw }, go, reload)
        screen(
            Route.EGW_BOOK,
            { Route.EgwBook(it.getString("slug")!!, it.getString("ch")?.toIntOrNull()) },
            go, reload,
            listOf(navArgument("slug") { type = NavType.StringType }, optional("ch")),
        )
        screen(Route.QUIZ, { Route.Quiz }, go, reload)
        screen(Route.QUIZ_PACK, { Route.QuizPack(it.getString("slug")!!) }, go, reload, listOf(navArgument("slug") { type = NavType.StringType }))
        screen(Route.QUIEN, { Route.Quien }, go, reload)
        screen(Route.BUSCAR, { Route.Buscar }, go, reload)
        composable(Route.DESCARGAS) {
            // The first open: nothing behind it on the back stack, so no top bar or menu to escape through.
            val bare = nav.previousBackStackEntry == null
            if (bare) {
                PisacomaTheme(Skin.Pastel) {
                    Box(Modifier.fillMaxSize().background(LocalChrome.current.background)) {
                        DescargasScreen(
                            Modifier.fillMaxSize(),
                            bare = true,
                            onContinue = {
                                nav.navigate((after ?: Route.Home).path) {
                                    popUpTo(Route.DESCARGAS) { inclusive = true }
                                }
                            },
                        )
                    }
                }
            } else {
                SiteScaffold(route = Route.Descargas, onNavigate = go) { modifier -> DescargasScreen(modifier, bare = false, onContinue = {}) }
            }
        }
    }
}

/** An optional query argument (`?dia={dia}`): absent → null. */
private fun optional(name: String) = navArgument(name) { type = NavType.StringType; nullable = true; defaultValue = null }

private fun NavGraphBuilder.screen(
    pattern: String,
    route: (android.os.Bundle) -> Route,
    go: (Route) -> Unit,
    reload: (Route) -> Unit,
    args: List<androidx.navigation.NamedNavArgument> = emptyList(),
) {
    composable(pattern, arguments = args) { entry ->
        val r = route(entry.arguments ?: android.os.Bundle())
        SiteScaffold(route = r, onNavigate = go, onReload = { reload(r) }) { modifier -> ScreenFor(r, modifier, go) }
    }
}

/** Dispatches a route to its screen. */
@Composable
fun ScreenFor(route: Route, modifier: Modifier, go: (Route) -> Unit) {
    when (route) {
        is Route.Home -> HomeScreen(modifier, go)
        is Route.Himnario -> HymnalIndexScreen(modifier, go)
        is Route.Hymn -> HymnScreen(route.variant, route.number, modifier, go)
        is Route.Escuela -> QuarterScreen(modifier, go)
        is Route.Leccion -> LessonScreen(route.number, route.day, modifier, go)
        is Route.Biblia -> BibleScreen(route.book, route.chapter, route.verse, modifier, go)
        is Route.Egw -> EgwCatalogScreen(modifier, go)
        is Route.EgwBook -> EgwBookScreen(route.slug, route.chapter, modifier, go)
        is Route.Quiz -> QuizIndexScreen(modifier, go)
        is Route.QuizPack -> QuizScreen(route.slug, modifier, go)
        is Route.Quien -> WheelScreen(modifier, go)
        is Route.Videos -> VideosScreen(modifier, go)
        is Route.Video -> VideoPlayerScreen(route.id, modifier, go)
        is Route.Buscar -> SearchScreen(modifier, go)
        is Route.Visitanos -> VisitanosScreen(modifier, go)
        is Route.Diccionario -> DictionaryScreen(modifier, go)
        is Route.Descargas -> DescargasScreen(modifier, bare = false, onContinue = {})
    }
}

@Composable
fun PlaceholderScreen(route: Route, modifier: Modifier) {
    androidx.compose.foundation.layout.Column(modifier.testTag("screen-${route.base}")) {
        Box(Modifier.padding(24.dp).weight(1f), contentAlignment = Alignment.TopStart) {
            Text(route.path, fontFamily = Fraunces, color = LocalChrome.current.bodyText)
        }
        pe.pisacoma.iglesia.ui.chrome.SiteFooter()
    }
}
