package pe.pisacoma.iglesia.ui.chrome

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pe.pisacoma.iglesia.nav.Route
import pe.pisacoma.iglesia.ui.theme.Inter
import pe.pisacoma.iglesia.ui.theme.LocalChrome
import pe.pisacoma.iglesia.ui.theme.PisacomaTheme

/**
 * The site's layout around every route: top bar, the route's content, the footer line, and
 * the menu overlay. `content` receives the modifier it should fill; screens that scroll put
 * the footer inside their own list by calling [SiteFooter] themselves and passing
 * `footer = false`.
 */
@Composable
fun SiteScaffold(
    route: Route,
    onNavigate: (Route) -> Unit,
    footer: Boolean = false,
    content: @Composable (Modifier) -> Unit,
) {
    PisacomaTheme(route.skin) {
        val c = LocalChrome.current
        var menuOpen by rememberSaveable { mutableStateOf(false) }
        Box(Modifier.fillMaxSize().background(c.background)) {
            Column(Modifier.fillMaxSize()) {
                TopBar(
                    menuOpen = menuOpen,
                    onBrand = { menuOpen = false; onNavigate(Route.Home) },
                    onSearch = { menuOpen = false; onNavigate(Route.Buscar) },
                    onMenu = { menuOpen = !menuOpen },
                )
                Box(Modifier.fillMaxWidth().weight(1f)) {
                    content(Modifier.fillMaxSize())
                }
                if (footer) SiteFooter()
            }
            FullMenu(
                open = menuOpen,
                current = route,
                onNavigate = { menuOpen = false; onNavigate(it) },
                onClose = { menuOpen = false },
            )
        }
    }
}

/** `.site-foot`: one centred 12px line over a hairline, with the bottom inset. */
@Composable
fun SiteFooter(modifier: Modifier = Modifier) {
    val c = LocalChrome.current
    Column(modifier.fillMaxWidth()) {
        Box(Modifier.fillMaxWidth().height(1.dp).background(c.footerBorder))
        Text(
            "Iglesia Adventista del Séptimo Día · Pisacoma, Chucuito, Puno, Perú · Misión Peruana del Lago Titicaca",
            fontFamily = Inter,
            fontSize = 12.sp,
            lineHeight = 18.sp,
            color = c.footerText,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 28.dp, end = 28.dp, top = 32.dp, bottom = 48.dp)
                .navigationBarsPadding(),
        )
    }
}
