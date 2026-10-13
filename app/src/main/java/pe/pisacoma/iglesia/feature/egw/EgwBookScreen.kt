package pe.pisacoma.iglesia.feature.egw

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import pe.pisacoma.iglesia.App
import pe.pisacoma.iglesia.R
import pe.pisacoma.iglesia.feature.escuela.PagerPill
import pe.pisacoma.iglesia.feature.escuela.relativeTimeEs
import pe.pisacoma.iglesia.nav.Route
import pe.pisacoma.iglesia.ui.chrome.SiteFooter
import pe.pisacoma.iglesia.ui.components.EyebrowLine
import pe.pisacoma.iglesia.ui.components.Hairline
import pe.pisacoma.iglesia.ui.components.TextSizeControls
import pe.pisacoma.iglesia.ui.components.ToolbarIcon
import pe.pisacoma.iglesia.ui.components.pressable
import pe.pisacoma.iglesia.ui.theme.Fraunces
import pe.pisacoma.iglesia.ui.theme.Inter
import pe.pisacoma.iglesia.ui.theme.Tokens

private fun hex(c: String): Color = runCatching { Color(android.graphics.Color.parseColor(c)) }.getOrDefault(Tokens.sage)

/** The site's /egw/<slug>: toolbar (TOC, back, book id, progress, tools), the chapter, done CTA, pager; the chapter list and the notes drawer as sheets. */
@Composable
fun EgwBookScreen(slug: String, chapter: Int?, modifier: Modifier, go: (Route) -> Unit) {
    val vm: EgwBookViewModel = viewModel(key = "egw-$slug") { EgwBookViewModel(App.repos.egw, App.repos.userState, slug, chapter) }
    val s by vm.state.collectAsState()
    var toc by remember { mutableStateOf(false) }
    var drawer by remember { mutableStateOf<String?>(null) }
    val list = rememberLazyListState()
    LaunchedEffect(s.chapter) { list.scrollToItem(0) }

    if (s.notFound) {
        Column(modifier.padding(20.dp)) {
            Text("Libro no encontrado", fontFamily = Fraunces, fontSize = 24.sp, color = Tokens.inkDeep)
            Text("← Volver al catálogo", fontFamily = Inter, color = Tokens.sage, modifier = Modifier.padding(top = 12.dp).pressable { go(Route.Egw) })
        }
        return
    }
    val meta = s.meta ?: return
    val book = s.book ?: return
    val cur = s.current ?: return
    val accent = hex(meta.color)

    Column(modifier.fillMaxSize()) {
        // ======= toolbar =======
        Row(Modifier.fillMaxWidth().background(Color(0xF2F7F4E6)).padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ToolbarIcon(R.drawable.ic_chapters, "Capítulos", testTag = "egw-chapters", onClick = { toc = true })
            Box(Modifier.clip(CircleShape).pressable(scale = 0.97f) { go(Route.Egw) }.padding(8.dp)) {
                Icon(painterResource(R.drawable.ic_back), "Volver al catálogo", tint = Tokens.sage, modifier = Modifier.size(14.dp))
            }
            Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(Modifier.clip(RoundedCornerShape(6.dp)).background(accent).padding(horizontal = 6.dp, vertical = 3.dp)) {
                    Text(meta.id, fontFamily = Inter, fontWeight = FontWeight.Bold, fontSize = 10.sp, letterSpacing = 0.8.sp, color = Tokens.cream)
                }
                Text("Cap. ${cur.number} / ${s.total}", fontFamily = Inter, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Tokens.sageText)
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(Modifier.width(40.dp).height(4.dp).clip(CircleShape).background(Tokens.sageA(0.14f))) { Box(Modifier.fillMaxWidth(s.pct / 100f).height(4.dp).clip(CircleShape).background(accent)) }
                Text("${s.pct}%", fontFamily = Inter, fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = Tokens.sageText)
            }
            ToolbarIcon(R.drawable.ic_check, if (s.isDone) "Marcar como no leído" else "Marcar como completado", active = s.isDone, testTag = "chapter-done-top", onClick = vm::toggleDone)
            ToolbarIcon(R.drawable.ic_bookmark, if (s.isBookmarked) "Quitar marcador" else "Agregar marcador", active = s.isBookmarked, testTag = "egw-bookmark", onClick = vm::toggleBookmark)
            TextSizeControls(vm::smaller, vm::larger)
            ToolbarIcon(R.drawable.ic_note, "Notas y marcadores", active = drawer != null, testTag = "egw-notes", onClick = { drawer = "notes" })
        }
        Hairline(0.12f)

        LazyColumn(Modifier.fillMaxSize(), state = list) {
            item {
                Column(Modifier.padding(horizontal = 20.dp)) {
                    Spacer(Modifier.height(26.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        EyebrowLine("Capítulo ${cur.number} · ${meta.id}", accent, size = 10.5f)
                        Box(Modifier.width(18.dp).height(1.dp).background(Tokens.sageA(0.3f)))
                        Text(meta.title, fontFamily = Fraunces, fontStyle = FontStyle.Italic, fontSize = 13.sp, color = Tokens.sageText)
                    }
                    Spacer(Modifier.height(14.dp))
                    Text(cur.title, fontFamily = Fraunces, fontWeight = FontWeight.SemiBold, fontSize = 28.sp, lineHeight = 31.sp, letterSpacing = (-0.022 * 28).sp, color = Tokens.inkDeep, modifier = Modifier.testTag("egw-chapter-title"))
                    Spacer(Modifier.height(22.dp))
                }
            }
            items(cur.paragraphs.size, key = { "p-${cur.number}-$it" }) { i ->
                Text(
                    cur.paragraphs[i],
                    fontFamily = Fraunces, fontSize = (17 * s.scale).sp, lineHeight = (29 * s.scale).sp, color = Tokens.ink,
                    modifier = Modifier.padding(horizontal = 20.dp).padding(bottom = (16 * s.scale).dp),
                )
            }
            item {
                Column(Modifier.padding(horizontal = 20.dp)) {
                    Spacer(Modifier.height(20.dp))
                    val shape = RoundedCornerShape(14.dp)
                    Box(
                        Modifier.fillMaxWidth().clip(shape).background(if (s.isDone) Tokens.sageA(0.12f) else accent).border(1.dp, if (s.isDone) Tokens.sageA(0.3f) else accent, shape)
                            .pressable(scale = 0.98f, onClick = vm::toggleDone).padding(vertical = 15.dp).testTag("chapter-done"),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(if (s.isDone) "Capítulo completado · Toca para deshacer" else "Marcar como completado", fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = if (s.isDone) Tokens.forest else Tokens.cream)
                    }
                    Spacer(Modifier.height(36.dp))
                    Hairline(0.18f)
                    Spacer(Modifier.height(22.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        if (cur.number > 1) PagerPill("← Cap. ${cur.number - 1}") { vm.prev() } else Spacer(Modifier.width(1.dp))
                        Text("${cur.number} / ${s.total}", fontFamily = Fraunces, fontStyle = FontStyle.Italic, fontSize = 13.sp, color = Tokens.sageText)
                        if (cur.number < s.total) PagerPill("Cap. ${cur.number + 1} →") { vm.next() } else Spacer(Modifier.width(1.dp))
                    }
                    Spacer(Modifier.height(60.dp))
                }
                SiteFooter()
            }
        }
    }

    // ======= chapter list (site .toc) =======
    if (toc) {
        val sheet = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(onDismissRequest = { toc = false }, sheetState = sheet, containerColor = Tokens.cream, modifier = Modifier.testTag("egw-toc")) {
            Column(Modifier.fillMaxHeight(0.92f).padding(horizontal = 20.dp).padding(bottom = 24.dp)) {
                EyebrowLine("— Curso · ${s.total} capítulos", Tokens.sage, size = 10.5f)
                Spacer(Modifier.height(6.dp))
                Text(meta.title, fontFamily = Fraunces, fontWeight = FontWeight.SemiBold, fontSize = 22.sp, color = Tokens.inkDeep)
                Text(meta.subtitle, fontFamily = Fraunces, fontStyle = FontStyle.Italic, fontSize = 13.sp, color = Tokens.sageText)
                Spacer(Modifier.height(10.dp))
                Box(Modifier.fillMaxWidth().height(4.dp).clip(CircleShape).background(Tokens.sageA(0.14f))) { Box(Modifier.fillMaxWidth(s.pct / 100f).height(4.dp).clip(CircleShape).background(accent)) }
                Spacer(Modifier.height(6.dp))
                Text("${s.completedCount} / ${s.total} completados · ${s.pct}%", fontFamily = Inter, fontSize = 12.sp, color = Tokens.sageText)
                Spacer(Modifier.height(12.dp))
                Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                    book.chapters.forEach { c ->
                        val isCur = c.number == s.chapter
                        val done = c.number in s.done
                        Row(
                            Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(if (isCur) Tokens.sageA(0.12f) else Color.Transparent)
                                .pressable(scale = 0.99f) { toc = false; vm.open(c.number) }.padding(horizontal = 10.dp, vertical = 11.dp).testTag("chapter-${c.number}"),
                            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            if (done) Icon(painterResource(R.drawable.ic_check), null, tint = Tokens.sage, modifier = Modifier.size(16.dp).width(30.dp))
                            else Text(c.number.toString().padStart(2, '0'), fontFamily = Fraunces, fontStyle = FontStyle.Italic, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Tokens.sageText, modifier = Modifier.width(30.dp))
                            Text(c.title, fontFamily = Fraunces, fontWeight = if (isCur) FontWeight.SemiBold else FontWeight.Medium, fontSize = 15.sp, color = if (done && !isCur) Tokens.sageText else Tokens.ink, modifier = Modifier.weight(1f))
                            if (c.number in s.hasNotes) Icon(painterResource(R.drawable.ic_note), "Con notas", tint = Tokens.sage, modifier = Modifier.size(12.dp))
                            if (s.bookmarks.any { it.slug == slug && it.chapter == c.number }) Icon(painterResource(R.drawable.ic_bookmark), "Con marcador", tint = Tokens.sage, modifier = Modifier.size(12.dp))
                        }
                    }
                }
                Text("Volver al catálogo", fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Tokens.sage, modifier = Modifier.padding(top = 10.dp).pressable(scale = 0.98f) { toc = false; go(Route.Egw) })
            }
        }
    }

    // ======= drawer: Notas / Marcadores / Progreso =======
    if (drawer != null) {
        val sheet = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(onDismissRequest = { drawer = null }, sheetState = sheet, containerColor = Tokens.cream, modifier = Modifier.testTag("egw-drawer")) {
            Column(Modifier.padding(horizontal = 20.dp).padding(bottom = 28.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    for ((id, label) in listOf("notes" to "Notas", "bookmarks" to "Marcadores", "progress" to "Progreso")) {
                        val active = drawer == id
                        Box(Modifier.clip(CircleShape).background(if (active) Tokens.forest else Tokens.sageA(0.08f)).pressable(scale = 0.97f) { drawer = id }.padding(horizontal = 14.dp, vertical = 8.dp).testTag("egw-tab-$id")) {
                            Text(label + (if (id == "bookmarks" && s.bookmarks.isNotEmpty()) " ${s.bookmarks.size}" else ""), fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = if (active) Tokens.cream else Tokens.forest)
                        }
                    }
                }
                Spacer(Modifier.height(20.dp))
                when (drawer) {
                    "notes" -> {
                        EyebrowLine("— Notas · Capítulo ${cur.number}", Tokens.sage, size = 10.5f)
                        Spacer(Modifier.height(6.dp))
                        Text(cur.title, fontFamily = Fraunces, fontWeight = FontWeight.SemiBold, fontSize = 20.sp, color = Tokens.inkDeep)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            run { val saved = s.savedAt; when { saved != null -> "Guardado ${relativeTimeEs(saved)}"; s.notes.isNotEmpty() -> "Tus apuntes anteriores"; else -> "Empieza a escribir — guardamos automáticamente" } },
                            fontFamily = Inter, fontSize = 12.sp, color = Tokens.sageText,
                        )
                        Spacer(Modifier.height(12.dp))
                        val areaShape = RoundedCornerShape(12.dp)
                        Box(Modifier.fillMaxWidth().height(180.dp).clip(areaShape).background(Color(0xB3FFFFFF)).border(1.dp, Tokens.sageA(0.2f), areaShape).padding(14.dp)) {
                            if (s.notes.isEmpty()) Text("Escribe lo que te habló de este capítulo…", fontFamily = Inter, fontSize = 14.sp, color = Color(0xFF9AA792))
                            BasicTextField(s.notes, vm::notes, textStyle = TextStyle(fontFamily = Inter, fontSize = 14.5.sp, lineHeight = 22.sp, color = Tokens.ink), cursorBrush = SolidColor(Tokens.sage), modifier = Modifier.fillMaxSize().testTag("notes-area"))
                        }
                        if (s.notes.isNotEmpty()) Text("Borrar nota", fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, color = Color(0xFF8A3A2A), modifier = Modifier.padding(top = 8.dp).pressable(scale = 0.97f, onClick = vm::clearNotes))
                    }
                    "bookmarks" -> {
                        if (s.bookmarks.isEmpty()) Text("Aún no hay marcadores. Toca el marcador de la barra para guardar un capítulo.", fontFamily = Inter, fontSize = 14.sp, lineHeight = 21.sp, color = Tokens.sageText)
                        s.bookmarks.forEach { bm ->
                            Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text(bm.bookId, fontFamily = Inter, fontWeight = FontWeight.Bold, fontSize = 10.sp, letterSpacing = 0.8.sp, color = Tokens.sage, modifier = Modifier.width(40.dp))
                                Column(Modifier.weight(1f).pressable(scale = 0.98f) { drawer = null; if (bm.slug == slug) vm.open(bm.chapter) else go(Route.EgwBook(bm.slug, bm.chapter)) }) {
                                    Text("Cap. ${bm.chapter} · ${bm.chapterTitle}", fontFamily = Fraunces, fontWeight = FontWeight.Medium, fontSize = 15.sp, color = Tokens.ink)
                                    Text("${bm.bookTitle} · ${relativeTimeEs(bm.addedAt)}", fontFamily = Inter, fontSize = 11.sp, color = Tokens.sageText)
                                }
                                Icon(painterResource(R.drawable.ic_nav_close), "Quitar marcador", tint = Tokens.sageText, modifier = Modifier.size(16.dp).pressable { vm.removeBookmark(bm.slug, bm.chapter) })
                            }
                            Hairline(0.12f)
                        }
                    }
                    "progress" -> {
                        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("${s.pct}", fontFamily = Fraunces, fontStyle = FontStyle.Italic, fontWeight = FontWeight.Bold, fontSize = 40.sp, color = accent)
                            Text("%", fontFamily = Fraunces, fontSize = 18.sp, color = Tokens.sage, modifier = Modifier.padding(bottom = 6.dp))
                            Spacer(Modifier.width(10.dp))
                            Text("${s.completedCount} de ${s.total} capítulos", fontFamily = Inter, fontSize = 13.sp, color = Tokens.sageText, modifier = Modifier.padding(bottom = 8.dp))
                        }
                        Spacer(Modifier.height(14.dp))
                        Text("Reiniciar progreso del libro", fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Color(0xFF8A3A2A), modifier = Modifier.pressable(scale = 0.97f, onClick = vm::resetProgress))
                    }
                }
            }
        }
    }
}
