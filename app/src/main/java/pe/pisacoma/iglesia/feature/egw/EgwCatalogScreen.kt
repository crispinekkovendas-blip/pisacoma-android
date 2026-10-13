package pe.pisacoma.iglesia.feature.egw

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import pe.pisacoma.iglesia.App
import pe.pisacoma.iglesia.data.model.EgwBookMeta
import pe.pisacoma.iglesia.nav.Route
import pe.pisacoma.iglesia.ui.chrome.SiteFooter
import pe.pisacoma.iglesia.ui.components.EyebrowLine
import pe.pisacoma.iglesia.ui.components.Lede
import pe.pisacoma.iglesia.ui.components.PageTitle
import pe.pisacoma.iglesia.ui.components.pressable
import pe.pisacoma.iglesia.ui.components.titleWithEm
import pe.pisacoma.iglesia.ui.theme.Fraunces
import pe.pisacoma.iglesia.ui.theme.Inter
import pe.pisacoma.iglesia.ui.theme.Tokens

private fun hex(c: String): Color = runCatching { Color(android.graphics.Color.parseColor(c)) }.getOrDefault(Tokens.sage)

/** The site's /egw: hero with the library numbers, then four «pasillos», each a horizontal rail of covers with reading progress. */
@Composable
fun EgwCatalogScreen(modifier: Modifier, go: (Route) -> Unit) {
    val vm: EgwCatalogViewModel = viewModel { EgwCatalogViewModel(App.repos.egw, App.repos.userState) }
    val s by vm.state.collectAsState()
    LaunchedEffect(Unit) { vm.refresh() }

    LazyColumn(modifier.testTag("egw-page")) {
        item {
            Column(Modifier.padding(horizontal = 20.dp)) {
                Spacer(Modifier.height(44.dp))
                EyebrowLine("— Biblioteca de pioneros · Don de profecía")
                Spacer(Modifier.height(18.dp))
                PageTitle(titleWithEm("Veinticinco libros,", "cuatro pasillos", tail = "."))
                Spacer(Modifier.height(22.dp))
                Lede("Un curso por volumen, organizados como una pequeña biblioteca: cada pasillo agrupa los libros por su intención original — clásicos pioneros, devocionales, vida práctica, y los tiempos finales.")
                Spacer(Modifier.height(26.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(18.dp), verticalAlignment = Alignment.Bottom) {
                    Meta(s.books.size.toString(), "volúmenes")
                    Box(Modifier.width(1.dp).height(28.dp).background(Tokens.sageA(0.2f)))
                    Meta(String.format(java.util.Locale("es", "PE"), "%,d", s.totalChapters).replace(',', '.'), "capítulos")
                    Box(Modifier.width(1.dp).height(28.dp).background(Tokens.sageA(0.2f)))
                    Meta(s.totalDone.toString(), "leídos")
                    if (s.totalDone > 0) {
                        Box(Modifier.width(1.dp).height(28.dp).background(Tokens.sageA(0.2f)))
                        Meta("${s.overallPct}%", "avance global", small = true)
                    }
                }
                Spacer(Modifier.height(36.dp))
            }
        }
        s.rails.forEach { (aisle, books) ->
            item(key = "aisle-${aisle.roman}") {
                val aisleCh = books.sumOf { it.chapters }
                val aisleDone = books.sumOf { s.stats[it.slug]?.done ?: 0 }
                Column(Modifier.testTag("rail-${aisle.roman}")) {
                    Row(Modifier.padding(horizontal = 20.dp), verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text(aisle.roman, fontFamily = Fraunces, fontStyle = FontStyle.Italic, fontWeight = FontWeight.Bold, fontSize = 40.sp, lineHeight = 40.sp, color = Color(0x40A78447))
                        Column(Modifier.weight(1f)) {
                            EyebrowLine("— Pasillo ${aisle.roman}", Tokens.sage, size = 10.5f)
                            Spacer(Modifier.height(6.dp))
                            Text(aisle.label, fontFamily = Fraunces, fontStyle = FontStyle.Italic, fontSize = 24.sp, lineHeight = 26.sp, color = Tokens.inkDeep)
                            Spacer(Modifier.height(4.dp))
                            Text(aisle.lede, fontFamily = Inter, fontSize = 13.sp, lineHeight = 19.sp, color = Tokens.sageText)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(books.size.toString(), fontFamily = Fraunces, fontStyle = FontStyle.Italic, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Tokens.inkDeep)
                            EyebrowLine("volúmenes", Tokens.sageText, size = 9.5f, tracking = 1.4f)
                            if (aisleDone > 0) {
                                Text("${Math.round(aisleDone * 100f / aisleCh)}%", fontFamily = Fraunces, fontStyle = FontStyle.Italic, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Tokens.sage)
                                EyebrowLine("avance", Tokens.sageText, size = 9.5f, tracking = 1.4f)
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    LazyRow(contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.testTag("rail-row-${aisle.roman}")) {
                        items(books.size, key = { books[it].slug }) { i -> Cover(books[i], s.stats[books[i].slug] ?: BookStats(0, null)) { go(Route.EgwBook(books[i].slug, it)) } }
                    }
                    Spacer(Modifier.height(40.dp))
                }
            }
        }
        item { SiteFooter() }
    }
}

@Composable
private fun Meta(num: String, label: String, small: Boolean = false) {
    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(num, fontFamily = Fraunces, fontStyle = FontStyle.Italic, fontWeight = FontWeight.Bold, fontSize = if (small) 16.sp else 25.6.sp, lineHeight = if (small) 18.sp else 26.sp, color = Tokens.inkDeep)
        EyebrowLine(label, Tokens.sageText, size = 11f, tracking = 1.6f)
    }
}

/** `.card`: a 2:3 poster with the book's colour behind it, the badge for featured books, the progress bar, title and chapter count. */
@Composable
private fun Cover(b: EgwBookMeta, st: BookStats, onOpen: (chapter: Int?) -> Unit) {
    val accent = hex(b.color)
    val pct = if (b.chapters == 0) 0 else Math.round(st.done * 100f / b.chapters)
    val isDone = b.chapters > 0 && st.done == b.chapters
    val continueAt = st.lastCh ?: 1
    Column(Modifier.width(132.dp).pressable(scale = 0.97f) { onOpen(if (st.hasProgress) continueAt else null) }.testTag("cover-${b.slug}")) {
        Box(Modifier.width(132.dp).height(198.dp).clip(RoundedCornerShape(10.dp)).background(accent)) {
            AsyncImage(model = "file:///android_asset/covers/${b.cover}", contentDescription = "Portada de ${b.title}", contentScale = ContentScale.Crop, modifier = Modifier.fillMaxWidth().height(198.dp))
            Box(Modifier.fillMaxWidth().height(198.dp).background(Brush.verticalGradient(0.55f to Color.Transparent, 1f to Color(0x66000000))))
            if (b.slug in EGW_FEATURED) {
                Row(Modifier.padding(8.dp).clip(CircleShape).background(Color(0xCC16241A)).padding(horizontal = 8.dp, vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(5.dp).clip(CircleShape).background(Color(0xFFE8D091)))
                    Text(if (isDone) "completado" else if (st.hasProgress) "en curso" else "destacado", fontFamily = Inter, fontWeight = FontWeight.Bold, fontSize = 8.5.sp, letterSpacing = 1.sp, color = Tokens.cream)
                }
            }
            if (st.hasProgress) {
                Column(Modifier.align(Alignment.BottomStart).fillMaxWidth().padding(8.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text(if (isDone) "Releer" else "Continuar · cap. $continueAt", fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 10.sp, color = Tokens.cream, modifier = Modifier.testTag("continue-${b.slug}"))
                    Box(Modifier.fillMaxWidth().height(3.dp).clip(CircleShape).background(Color(0x55FFFFFF))) {
                        Box(Modifier.fillMaxWidth(pct / 100f).height(3.dp).clip(CircleShape).background(Color(0xFFE8D091)))
                    }
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        Text(b.title, fontFamily = Fraunces, fontWeight = FontWeight.Medium, fontSize = 13.5.sp, lineHeight = 16.sp, color = Tokens.inkDeep, maxLines = 2, overflow = TextOverflow.Ellipsis)
        Spacer(Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(b.id, fontFamily = Inter, fontWeight = FontWeight.Bold, fontSize = 9.5.sp, letterSpacing = 0.8.sp, color = accent)
            Text("·", color = Tokens.sageText)
            Text(if (st.hasProgress) "${st.done}/${b.chapters} cap." else "${b.chapters} cap.", fontFamily = Inter, fontSize = 10.5.sp, color = Tokens.sageText)
            if (st.hasProgress) Text("$pct%", fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 10.5.sp, color = Tokens.sage)
        }
    }
}
