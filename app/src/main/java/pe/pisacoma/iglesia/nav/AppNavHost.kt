package pe.pisacoma.iglesia.nav

import androidx.compose.foundation.layout.Box
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
import pe.pisacoma.iglesia.ui.chrome.SiteScaffold
import pe.pisacoma.iglesia.ui.theme.LocalChrome
import pe.pisacoma.iglesia.ui.theme.Fraunces

/** Navigation Compose graph with one destination per site route. */
@Composable
fun AppNavHost(nav: NavHostController = rememberNavController(), start: Route = Route.Home) {
    val go: (Route) -> Unit = { r ->
        nav.navigate(r.path) {
            launchSingleTop = true
        }
    }
    NavHost(nav, startDestination = start.path) {
        screen(Route.HOME, { Route.Home }, go)
        screen(Route.VISITANOS, { Route.Visitanos }, go)
        screen(Route.HIMNARIO, { Route.Himnario }, go)
        screen(
            Route.HYMN,
            { Route.Hymn(it.getString("variant")!!, it.getInt("number")) },
            go,
            listOf(navArgument("variant") { type = NavType.StringType }, navArgument("number") { type = NavType.IntType }),
        )
        screen(Route.VIDEOS, { Route.Videos }, go)
        screen(Route.VIDEO, { Route.Video(it.getString("id")!!) }, go, listOf(navArgument("id") { type = NavType.StringType }))
        screen(Route.ESCUELA, { Route.Escuela }, go)
        screen(
            Route.LECCION,
            { Route.Leccion(it.getInt("number"), it.getString("dia")?.toIntOrNull()) },
            go,
            listOf(navArgument("number") { type = NavType.IntType }, optional("dia")),
        )
        screen(
            Route.BIBLIA,
            { Route.Biblia(it.getString("libro")?.toIntOrNull(), it.getString("cap")?.toIntOrNull(), it.getString("v")?.toIntOrNull()) },
            go,
            listOf(optional("libro"), optional("cap"), optional("v")),
        )
        screen(Route.DICCIONARIO, { Route.Diccionario }, go)
        screen(Route.EGW, { Route.Egw }, go)
        screen(
            Route.EGW_BOOK,
            { Route.EgwBook(it.getString("slug")!!, it.getString("ch")?.toIntOrNull()) },
            go,
            listOf(navArgument("slug") { type = NavType.StringType }, optional("ch")),
        )
        screen(Route.QUIZ, { Route.Quiz }, go)
        screen(Route.QUIZ_PACK, { Route.QuizPack(it.getString("slug")!!) }, go, listOf(navArgument("slug") { type = NavType.StringType }))
        screen(Route.QUIEN, { Route.Quien }, go)
        screen(Route.BUSCAR, { Route.Buscar }, go)
    }
}

/** An optional query argument (`?dia={dia}`): absent → null. */
private fun optional(name: String) = navArgument(name) { type = NavType.StringType; nullable = true; defaultValue = null }

private fun NavGraphBuilder.screen(
    pattern: String,
    route: (android.os.Bundle) -> Route,
    go: (Route) -> Unit,
    args: List<androidx.navigation.NamedNavArgument> = emptyList(),
) {
    composable(pattern, arguments = args) { entry ->
        val r = route(entry.arguments ?: android.os.Bundle())
        SiteScaffold(route = r, onNavigate = go) { modifier -> ScreenFor(r, modifier, go) }
    }
}

/** Dispatches a route to its screen. Screens are added task by task; the rest are placeholders. */
@Composable
fun ScreenFor(route: Route, modifier: Modifier, go: (Route) -> Unit) {
    PlaceholderScreen(route, modifier)
}

@Composable
fun PlaceholderScreen(route: Route, modifier: Modifier) {
    Box(modifier.padding(24.dp).testTag("screen-${route.base}"), contentAlignment = Alignment.TopStart) {
        Text(route.path, fontFamily = Fraunces, color = LocalChrome.current.bodyText)
    }
}
