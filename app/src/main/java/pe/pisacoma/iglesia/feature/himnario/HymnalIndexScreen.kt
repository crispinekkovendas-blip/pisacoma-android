package pe.pisacoma.iglesia.feature.himnario

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import pe.pisacoma.iglesia.App
import pe.pisacoma.iglesia.R
import pe.pisacoma.iglesia.data.model.HymnVariant
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

/** The site's /himnario: hero, three hymnal tabs, search, «última visita», «Himnos populares», «Ir a», the typeset list. */
@Composable
fun HymnalIndexScreen(modifier: Modifier, go: (Route) -> Unit) {
    val vm: HymnalViewModel = viewModel { HymnalViewModel(App.repos.hymns, App.repos.userState) }
    val s by vm.state.collectAsState()
    LaunchedEffect(Unit) { vm.refreshRecent() }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val filtered = s.filtered
    // Items before the hymn rows: hero/tabs/search (1) + recent (0/1) + picks (0/1) + range bar (0/1) + count (1)
    val headerCount = 1 + (if (!s.isSearching && s.recentForVariant.isNotEmpty()) 1 else 0) + (if (!s.isSearching && s.featured.isNotEmpty()) 1 else 0) + (if (!s.isSearching && s.ranges.size > 1) 1 else 0) + 1

    LazyColumn(modifier.testTag("hymn-list"), state = listState) {
        item(key = "head") {
            Column(Modifier.padding(horizontal = 20.dp)) {
                Spacer(Modifier.height(44.dp))
                EyebrowLine("— Himnario · Canto congregacional")
                Spacer(Modifier.height(18.dp))
                PageTitle(titleWithEm("Tres himnarios.", "Una sola alabanza."))
                Spacer(Modifier.height(22.dp))
                Lede("Más de 1.800 himnos en español y portugués, con letras completas, referencias bíblicas y audio. Selecciona un himnario abajo y empieza a cantar.")
                Spacer(Modifier.height(22.dp))
                // .tabs — one column on phones
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    HymnVariant.entries.forEachIndexed { i, v -> VariantTab(i + 1, v, v == s.variant) { vm.select(v) } }
                }
                Spacer(Modifier.height(16.dp))
                SearchField(s.query, vm::query, "Buscar por número, título o pasaje bíblico…", testTag = "hymn-search")
                Spacer(Modifier.height(22.dp))
            }
        }
        if (!s.isSearching && s.recentForVariant.isNotEmpty()) item(key = "recent") {
            Column(
                Modifier.padding(horizontal = 20.dp).padding(top = 4.dp, bottom = 30.dp)
                    .background(Brush.horizontalGradient(0f to Color(0x8CF8F0D7), 0.7f to Color.Transparent))
                    .border(0.dp, Color.Transparent),
            ) {
                Box(Modifier.fillMaxWidth().height(1.dp).background(Color(0x38B8924F)))
                Column(Modifier.padding(start = 4.dp, end = 4.dp, top = 12.dp, bottom = 14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    EyebrowLine("— última visita", Ochre.label, size = 10f)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.Bottom, modifier = Modifier.horizontalScroll(rememberScrollState())) {
                        s.recentForVariant.forEachIndexed { i, r ->
                            if (i > 0) Text("·", fontFamily = Fraunces, fontStyle = FontStyle.Italic, color = Tokens.sageA(0.35f))
                            Row(
                                Modifier.clip(RoundedCornerShape(4.dp)).pressable(scale = 0.98f) { go(Route.Hymn(r.variant, r.number)) }
                                    .padding(horizontal = 6.dp, vertical = 4.dp).testTag("recent-${r.number}"),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.Bottom,
                            ) {
                                Text(r.number.toString(), fontFamily = Fraunces, fontStyle = FontStyle.Italic, fontWeight = FontWeight.Bold, fontSize = 15.2.sp, color = Ochre.label)
                                Text(r.title, fontFamily = Fraunces, fontWeight = FontWeight.Medium, fontSize = 14.7.sp, color = Tokens.forest, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                }
                Box(Modifier.fillMaxWidth().height(1.dp).background(Color(0x38B8924F)))
            }
        }
        if (!s.isSearching && s.featured.isNotEmpty()) item(key = "picks") {
            Column(Modifier.padding(horizontal = 20.dp).padding(top = 4.dp, bottom = 38.dp)) {
                OrnamentRule()
                Column(Modifier.padding(vertical = 22.dp)) {
                    EyebrowLine("— selección del editor", Ochre.label, size = 10.5f)
                    Spacer(Modifier.height(10.dp))
                    PageTitle(titleWithEm("Himnos", "populares"), size = 24.8.sp)
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "Ocho himnos amados de la congregación — " + (if (s.variant == HymnVariant.ES) "para empezar a cantar" else "para começar a cantar") + ".",
                        fontFamily = Fraunces, fontStyle = FontStyle.Italic, fontSize = 14.7.sp, lineHeight = 22.sp, color = Tokens.sageText,
                    )
                    Spacer(Modifier.height(22.dp))
                    val lbl = when (s.variant) { HymnVariant.ES -> "Himno"; HymnVariant.PT -> "Hino"; HymnVariant.PT_NOVO -> "Hino · NHA" }
                    s.featured.forEachIndexed { i, h ->
                        Row(
                            Modifier.fillMaxWidth().pressable(scale = 0.99f) { go(Route.Hymn(s.variant.slug, h.number)) }
                                .padding(vertical = 13.dp).testTag("pick-${h.number}"),
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                (i + 1).toString().padStart(2, '0'),
                                fontFamily = Fraunces, fontStyle = FontStyle.Italic, fontWeight = FontWeight.Bold, fontSize = 24.8.sp,
                                letterSpacing = (-0.04 * 24.8).sp, color = Ochre.numeral, modifier = Modifier.width(38.dp),
                            )
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(h.title, fontFamily = Fraunces, fontWeight = FontWeight.Medium, fontSize = 16.8.sp, lineHeight = 20.sp, color = Tokens.inkDeep, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                Row(horizontalArrangement = Arrangement.spacedBy(7.dp), verticalAlignment = Alignment.Bottom) {
                                    Text("${lbl.uppercase()} ${h.number}", fontFamily = Fraunces, fontWeight = FontWeight.SemiBold, fontSize = 10.6.sp, letterSpacing = 0.6.sp, color = Tokens.sage)
                                    if (h.bibleRef != null) {
                                        Text("·", color = Tokens.sageA(0.4f), fontFamily = Fraunces)
                                        Text(h.bibleRef, fontFamily = Fraunces, fontStyle = FontStyle.Italic, fontSize = 12.5.sp, color = Tokens.sageText)
                                    }
                                }
                            }
                        }
                        Box(Modifier.fillMaxWidth().height(1.dp).padding(horizontal = 4.dp).background(Tokens.sageA(0.12f)))
                    }
                }
                OrnamentRule()
            }
        }
        if (!s.isSearching && s.ranges.size > 1) item(key = "ranges") {
            val shape = RoundedCornerShape(12.dp)
            Row(
                Modifier.padding(horizontal = 20.dp).padding(bottom = 18.dp).fillMaxWidth().clip(shape)
                    .background(Color(0xF0FBF9EF)).border(1.dp, Tokens.sageA(0.16f), shape).padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                EyebrowLine("Ir a", Tokens.sageText, size = 10.5f, tracking = 2.2f)
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    s.ranges.forEachIndexed { ri, r ->
                        Box(
                            Modifier.clip(CircleShape).background(Color(0x99FFFFFF)).border(1.dp, Tokens.sageA(0.22f), CircleShape)
                                .pressable {
                                    val idx = filtered.indexOfFirst { it.number >= r.first }
                                    if (idx >= 0) scope.launch { listState.animateScrollToItem(headerCount + idx + ri) }
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                        ) { Text("${r.first}–${r.last}", fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 11.5.sp, color = Tokens.forest) }
                    }
                }
            }
        }
        item(key = "count") {
            Row(Modifier.padding(horizontal = 24.dp).padding(top = 6.dp, bottom = 18.dp).testTag("hymn-count"), verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(filtered.size.toString(), fontFamily = Fraunces, fontStyle = FontStyle.Italic, fontWeight = FontWeight.Bold, fontSize = 17.6.sp, color = Tokens.inkDeep)
                EyebrowLine(if (filtered.size == 1) "himno" else "himnos", Tokens.sageText, size = 11f, tracking = 1.8f)
                if (s.query.isNotEmpty()) Text("para «${s.query}»", fontFamily = Fraunces, fontStyle = FontStyle.Italic, fontSize = 11.sp, color = Tokens.sage)
            }
            Box(Modifier.padding(horizontal = 20.dp)) { OrnamentRule() }
        }
        val last = s.rows.lastOrNull()?.number ?: 1
        filtered.forEachIndexed { i, h ->
            val isBreak = !s.isSearching && (h.number - 1) % HymnalUiState.RANGE_SIZE == 0 && h.number > 1
            if (isBreak) item(key = "break-${h.number}") {
                val start = (h.number - 1) / HymnalUiState.RANGE_SIZE * HymnalUiState.RANGE_SIZE + 1
                val end = minOf(start + HymnalUiState.RANGE_SIZE - 1, last)
                Row(Modifier.padding(horizontal = 24.dp).padding(top = 28.dp, bottom = 16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                    Box(Modifier.weight(1f).height(1.dp).background(Brush.horizontalGradient(listOf(Color.Transparent, Tokens.sageA(0.22f), Color.Transparent))))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Bottom) {
                        Text(start.toString(), fontFamily = Fraunces, fontStyle = FontStyle.Italic, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Ochre.numeral)
                        Text("✦", fontSize = 11.sp, color = Color(0x99B8924F), modifier = Modifier.padding(bottom = 3.dp))
                        Text("$start–$end", fontFamily = Fraunces, fontStyle = FontStyle.Italic, fontWeight = FontWeight.Medium, fontSize = 11.8.sp, letterSpacing = 0.5.sp, color = Tokens.sageText)
                    }
                    Box(Modifier.weight(1f).height(1.dp).background(Brush.horizontalGradient(listOf(Color.Transparent, Tokens.sageA(0.22f), Color.Transparent))))
                }
            }
            item(key = "row-${h.number}") {
                HymnIndexRow(h, first = i == 0) { go(Route.Hymn(s.variant.slug, h.number)) }
            }
        }
        item(key = "foot") { Spacer(Modifier.height(50.dp)); SiteFooter() }
    }
}

/** `.tab`: numeral · label / sub; the active one is the sage→forest gradient. */
@Composable
private fun VariantTab(index: Int, v: HymnVariant, active: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(14.dp)
    Row(
        Modifier.fillMaxWidth().clip(shape)
            .background(if (active) Brush.linearGradient(listOf(Tokens.sage, Tokens.forest)) else Brush.linearGradient(listOf(Color(0x8CFFFFFF), Color(0x8CFFFFFF))))
            .border(1.dp, if (active) Tokens.forest else Tokens.sageA(0.18f), shape)
            .pressable(scale = 0.985f, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .testTag("tab-${v.slug}"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("0$index", fontFamily = Fraunces, fontStyle = FontStyle.Italic, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = if (active) Color(0xFFE8D091) else Ochre.label, modifier = Modifier.width(26.dp))
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(v.tabLabel, fontFamily = Fraunces, fontWeight = FontWeight.SemiBold, fontSize = 16.8.sp, letterSpacing = (-0.2).sp, color = if (active) Tokens.cream else Tokens.inkDeep)
            Text(v.tabSub.uppercase(), fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 10.sp, letterSpacing = 1.4.sp, color = if (active) Tokens.cream.copy(alpha = 0.7f) else Tokens.sageText)
        }
    }
}

/** `.list .row`: ochre italic numeral, Fraunces title, italic reference, faint arrow. */
@Composable
fun HymnIndexRow(h: HymnRow, first: Boolean, onClick: () -> Unit) {
    Column(Modifier.padding(horizontal = 20.dp)) {
        if (!first) Box(Modifier.fillMaxWidth().height(1.dp).background(Tokens.sageA(0.10f)))
        Row(
            Modifier.fillMaxWidth().pressable(scale = 0.99f, onClick = onClick).padding(end = 8.dp, top = 14.dp, bottom = 14.dp).testTag("hymn-row-${h.number}"),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                h.number.toString(),
                fontFamily = Fraunces, fontStyle = FontStyle.Italic, fontWeight = FontWeight.Bold, fontSize = 22.7.sp,
                letterSpacing = (-0.035 * 22.7).sp, color = Ochre.numeral,
                modifier = Modifier.width(50.dp), textAlign = androidx.compose.ui.text.style.TextAlign.End,
            )
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(h.title, fontFamily = Fraunces, fontWeight = FontWeight.Medium, fontSize = 16.sp, lineHeight = 19.5.sp, color = Tokens.ink, maxLines = 2, overflow = TextOverflow.Ellipsis)
                if (h.bibleRef != null) Text(h.bibleRef, fontFamily = Fraunces, fontStyle = FontStyle.Italic, fontSize = 11.5.sp, color = Color(0xFF657158), maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Box(Modifier.size(26.dp).alpha(0.5f), contentAlignment = Alignment.Center) {
                Icon(painterResource(R.drawable.ic_nav_arrow), null, tint = Tokens.sage, modifier = Modifier.size(14.dp))
            }
        }
    }
}
