package pe.pisacoma.iglesia.feature.buscar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pe.pisacoma.iglesia.App
import pe.pisacoma.iglesia.R
import pe.pisacoma.iglesia.data.model.SearchIndex
import pe.pisacoma.iglesia.nav.Route
import pe.pisacoma.iglesia.ui.chrome.SiteFooter
import pe.pisacoma.iglesia.ui.components.EyebrowLine
import pe.pisacoma.iglesia.ui.components.Lede
import pe.pisacoma.iglesia.ui.components.Ochre
import pe.pisacoma.iglesia.ui.components.OrnamentRule
import pe.pisacoma.iglesia.ui.components.PageTitle
import pe.pisacoma.iglesia.ui.components.SearchField
import pe.pisacoma.iglesia.ui.components.pressable
import pe.pisacoma.iglesia.ui.components.titleWithEm
import pe.pisacoma.iglesia.ui.theme.Fraunces
import pe.pisacoma.iglesia.ui.theme.Inter
import pe.pisacoma.iglesia.ui.theme.Tokens

private val GROUPS = listOf("bible" to "Biblia", "hymns" to "Himnos", "egw" to "Elena G. de White", "lessons" to "Escuela Sabática")
private const val PREVIEW = 6
private val EXAMPLES = listOf("307", "Roca de la eternidad", "Juan 3:16", "Camino a Cristo", "falsos maestros")

/** The site's /buscar: one field over the bundled index, four groups of typeset rows, «Ver N resultados». */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(modifier: Modifier, go: (Route) -> Unit) {
    var index by remember { mutableStateOf<SearchIndex?>(null) }
    var query by rememberSaveable { mutableStateOf("") }
    val expanded = remember { mutableStateMapOf<String, Boolean>() }
    LaunchedEffect(Unit) { index = App.repos.search.index() }
    val results = remember(index, query) { Search.search(index, query) }
    val hasQuery = query.trim().isNotEmpty()
    fun open(href: String) { Route.parse(href)?.let(go) }

    LazyColumn(modifier.testTag("search-page")) {
        item {
            Column(Modifier.padding(horizontal = 20.dp)) {
                Spacer(Modifier.height(44.dp))
                EyebrowLine("— Buscar · Todo el sitio")
                Spacer(Modifier.height(18.dp))
                PageTitle(titleWithEm("Busca en", "todo el sitio."))
                Spacer(Modifier.height(22.dp))
                Lede("Himnos por número o título, capítulos de Elena G. de White, lecciones del trimestre y pasajes de la Biblia — desde un solo lugar.")
                Spacer(Modifier.height(26.dp))
                SearchField(
                    query, { query = it; expanded.clear() }, "Himno, capítulo, lección o Juan 3:16…", testTag = "search-input",
                    onSearch = { (results.bible.firstOrNull() ?: results.hymns.firstOrNull() ?: results.egw.firstOrNull() ?: results.lessons.firstOrNull())?.let { open(it.href) } },
                    onClear = { query = "" },
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    when {
                        index == null -> "Cargando el índice…"
                        hasQuery && results.total == 0 -> "Sin resultados para «${query.trim()}»."
                        hasQuery -> "${results.total} ${if (results.total == 1) "resultado" else "resultados"} · Enter abre el primero"
                        else -> "Consejo: toca la lupa de arriba desde cualquier pantalla para buscar."
                    },
                    fontFamily = Fraunces, fontStyle = FontStyle.Italic, fontSize = 13.5.sp, color = Tokens.sageText, modifier = Modifier.testTag("search-status"),
                )
                if (!hasQuery && index != null) {
                    Spacer(Modifier.height(14.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        EXAMPLES.forEach { ex ->
                            Box(Modifier.clip(CircleShape).background(androidx.compose.ui.graphics.Color(0x99FFFFFF)).border(1.dp, Tokens.sageA(0.22f), CircleShape).pressable(scale = 0.97f) { query = ex }.padding(horizontal = 12.dp, vertical = 7.dp).testTag("example-$ex")) {
                                Text(ex, fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Tokens.forest)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(22.dp))
            }
        }
        GROUPS.forEach { (key, label) ->
            val hits = when (key) { "bible" -> results.bible; "hymns" -> results.hymns; "egw" -> results.egw; else -> results.lessons }
            if (hits.isEmpty()) return@forEach
            val open = expanded[key] == true
            val shown = if (open) hits.take(60) else hits.take(PREVIEW)
            item(key = "head-$key") {
                Row(Modifier.padding(horizontal = 20.dp).padding(top = 10.dp, bottom = 6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    EyebrowLine("— $label", Tokens.sage, size = 10.5f)
                    Box(Modifier.weight(1f)) { OrnamentRule() }
                    Text(hits.size.toString(), fontFamily = Fraunces, fontStyle = FontStyle.Italic, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Ochre.label)
                }
            }
            items(shown.size, key = { "$key-${shown[it].href}-$it" }) { i ->
                val h = shown[i]
                Column(Modifier.padding(horizontal = 20.dp)) {
                    if (i > 0) Box(Modifier.fillMaxWidth().height(1.dp).background(Tokens.sageA(0.10f)))
                    Row(
                        Modifier.fillMaxWidth().pressable(scale = 0.99f) { open(h.href) }.padding(end = 8.dp, top = 13.dp, bottom = 13.dp).testTag("hit-${h.href}"),
                        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        Text(h.num, fontFamily = Fraunces, fontStyle = FontStyle.Italic, fontWeight = FontWeight.Bold, fontSize = 20.sp, letterSpacing = (-0.03 * 20).sp, color = Ochre.numeral, textAlign = TextAlign.End, modifier = Modifier.width(48.dp), maxLines = 1)
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(h.title, fontFamily = Fraunces, fontWeight = FontWeight.Medium, fontSize = 16.sp, lineHeight = 19.5.sp, color = Tokens.ink, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            Text(h.caption, fontFamily = Fraunces, fontStyle = FontStyle.Italic, fontSize = 11.5.sp, color = androidx.compose.ui.graphics.Color(0xFF657158), maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        Box(Modifier.size(26.dp).alpha(0.5f), contentAlignment = Alignment.Center) { Icon(painterResource(R.drawable.ic_nav_arrow), null, tint = Tokens.sage, modifier = Modifier.size(14.dp)) }
                    }
                }
            }
            if (hits.size > PREVIEW && !open) item(key = "more-$key") {
                Text(
                    "Ver ${minOf(hits.size, 60)} ${if (label == "Himnos") "himnos" else "resultados"} ↓",
                    fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Tokens.sage,
                    modifier = Modifier.padding(horizontal = 20.dp).padding(top = 8.dp, bottom = 18.dp).pressable(scale = 0.98f) { expanded[key] = true }.testTag("more-$key"),
                )
            }
        }
        item { Spacer(Modifier.height(50.dp)); SiteFooter() }
    }
}
