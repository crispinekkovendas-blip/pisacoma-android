package pe.pisacoma.iglesia.feature.biblia

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.withStyle
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
import pe.pisacoma.iglesia.ui.components.Lede
import pe.pisacoma.iglesia.ui.components.Ochre
import pe.pisacoma.iglesia.ui.components.PageTitle
import pe.pisacoma.iglesia.ui.components.ToolbarIcon
import pe.pisacoma.iglesia.ui.components.pressable
import pe.pisacoma.iglesia.ui.components.titleWithEm
import pe.pisacoma.iglesia.ui.theme.Fraunces
import pe.pisacoma.iglesia.ui.theme.Inter
import pe.pisacoma.iglesia.ui.theme.Tokens

/** The site's /biblia: hero, «Lectura» picker button + tools, the chapter, pager, book/chapter picker, drawer. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BibleScreen(book: Int?, chapter: Int?, verse: Int?, modifier: Modifier, go: (Route) -> Unit) {
    val vm: BibleViewModel = viewModel(key = "biblia") { BibleViewModel(App.repos.bible, App.repos.userState, book, chapter, verse) }
    val s by vm.state.collectAsState()
    val ctx = LocalContext.current
    var picker by remember { mutableStateOf(false) }
    var pickerBook by remember { mutableIntStateOf(0) } // 0 = book list
    var testament by remember { mutableStateOf("NT") }
    var drawer by remember { mutableStateOf<String?>(null) }
    val list = rememberLazyListState()
    val cur = s.book ?: return
    LaunchedEffect(s.bookNum, s.chapter, s.loading) {
        if (!s.loading) {
            if (s.targetVerse > 0) list.animateScrollToItem(1 + s.targetVerse.coerceAtMost(s.verses.size)) else list.scrollToItem(0)
        }
    }

    LazyColumn(modifier, state = list) {
        item {
            Column(Modifier.padding(horizontal = 20.dp)) {
                Spacer(Modifier.height(44.dp))
                EyebrowLine("— Reina-Valera 1960 · Biblia completa")
                Spacer(Modifier.height(18.dp))
                PageTitle(titleWithEm("La", "Palabra", tail = ",\nlibro por libro."))
                Spacer(Modifier.height(22.dp))
                Lede("Sesenta y seis libros, dos testamentos, una sola voz. Pulsa el título de abajo para saltar a cualquier libro y capítulo.")
                Spacer(Modifier.height(26.dp))
                // .nav-bar
                val barShape = RoundedCornerShape(16.dp)
                Column(Modifier.fillMaxWidth().clip(barShape).background(Color(0xF0FBF9EF)).border(1.dp, Tokens.sageA(0.18f), barShape).padding(12.dp)) {
                    Row(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).pressable(scale = 0.985f) { pickerBook = s.bookNum; testament = if (s.bookNum >= 40) "NT" else "AT"; picker = true }
                            .padding(horizontal = 6.dp, vertical = 6.dp).testTag("bible-title"),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f)) {
                            EyebrowLine("Lectura", Tokens.sageText, size = 10f, tracking = 2f)
                            Text(
                                buildAnnotatedString { append(cur.name); append(" "); withStyle(SpanStyle(fontStyle = FontStyle.Italic, color = Ochre.numeral)) { append(s.chapter.toString()) } },
                                fontFamily = Fraunces, fontWeight = FontWeight.SemiBold, fontSize = 22.sp, color = Tokens.inkDeep,
                            )
                        }
                        Icon(painterResource(R.drawable.ic_nav_arrow), null, tint = Tokens.sage, modifier = Modifier.size(18.dp))
                    }
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.alpha(if (s.isFirst) 0.35f else 1f)) { ToolbarIcon(R.drawable.ic_back, "Capítulo anterior", testTag = "bible-prev", onClick = { if (!s.isFirst) vm.prev() }) }
                        Box(Modifier.alpha(if (s.isLast) 0.35f else 1f)) { ToolbarIcon(R.drawable.ic_nav_arrow, "Capítulo siguiente", testTag = "bible-next", onClick = { if (!s.isLast) vm.next() }) }
                        Box(Modifier.width(1.dp).height(22.dp).background(Tokens.sageA(0.2f)))
                        ToolbarIcon(label = "Texto más pequeño", testTag = "text-smaller", onClick = vm::smaller) { Text("A", fontFamily = Fraunces, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Tokens.forest) }
                        ToolbarIcon(label = "Texto más grande", testTag = "text-larger", onClick = vm::larger) { Text("A", fontFamily = Fraunces, fontWeight = FontWeight.SemiBold, fontSize = 17.sp, color = Tokens.forest) }
                        Box(Modifier.width(1.dp).height(22.dp).background(Tokens.sageA(0.2f)))
                        val sel = s.verses.firstOrNull { it.first == s.targetVerse }
                        ToolbarIcon(R.drawable.ic_share, if (sel != null) "Compartir ${cur.name} ${s.chapter}:${sel.first}" else "Compartir capítulo", testTag = "bible-share", onClick = {
                            val text = if (sel != null) "«${sel.second}» — ${cur.name} ${s.chapter}:${sel.first} (RVR 1960)" else "${cur.name} ${s.chapter} · Biblia Reina-Valera 1960"
                            val url = "https://quiz-qwik.vercel.app/biblia/?libro=${s.bookNum}&cap=${s.chapter}" + (if (sel != null) "&v=${sel.first}" else "")
                            ctx.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, "$text\n$url") }, "Compartir"))
                        })
                        ToolbarIcon(R.drawable.ic_bookmark, if (s.isBookmarked) "Quitar marcador" else "Guardar capítulo", active = s.isBookmarked, testTag = "bible-bookmark", onClick = vm::toggleBookmark)
                        Row(
                            Modifier.clip(RoundedCornerShape(10.dp)).background(Tokens.sageA(0.06f)).border(1.dp, Tokens.sageA(0.2f), RoundedCornerShape(10.dp))
                                .pressable { drawer = "notes" }.padding(horizontal = 10.dp, vertical = 10.dp).testTag("bible-panel"),
                            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Icon(painterResource(R.drawable.ic_note), "Abrir panel de notas, marcadores y recientes", tint = Tokens.forest, modifier = Modifier.size(16.dp))
                            Text("Notas", fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Tokens.forest)
                            if (s.currentHasNotes) Box(Modifier.size(6.dp).clip(CircleShape).background(Ochre.numeral))
                        }
                    }
                }
                Spacer(Modifier.height(30.dp))
                // .ch-mark
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(s.chapter.toString(), fontFamily = Fraunces, fontStyle = FontStyle.Italic, fontWeight = FontWeight.Bold, fontSize = 44.sp, lineHeight = 40.sp, color = Ochre.numeral)
                    Box(Modifier.width(28.dp).height(1.dp).background(Tokens.sageA(0.3f)))
                    Text(cur.name, fontFamily = Fraunces, fontWeight = FontWeight.Medium, fontSize = 18.sp, color = Tokens.inkDeep, modifier = Modifier.weight(1f))
                }
                Spacer(Modifier.height(8.dp))
                Row(
                    Modifier.clip(CircleShape).pressable(scale = 0.98f) { ctx.startActivity(Intent(Intent.ACTION_VIEW, android.net.Uri.parse(vm.aymaraUrl()))) }.padding(vertical = 4.dp).testTag("bible-aymara"),
                    horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Leer en aimara", fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Tokens.sage)
                    Icon(painterResource(R.drawable.ic_external), null, tint = Tokens.sage, modifier = Modifier.size(12.dp))
                }
                if (s.currentHasNotes) {
                    Spacer(Modifier.height(12.dp))
                    Row(Modifier.clip(CircleShape).background(Color(0x33B8924F)).pressable { drawer = "notes" }.padding(horizontal = 12.dp, vertical = 7.dp), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(painterResource(R.drawable.ic_note), null, tint = Ochre.label, modifier = Modifier.size(13.dp))
                        Text("Has tomado notas en este capítulo", fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Ochre.label)
                    }
                }
                Spacer(Modifier.height(18.dp))
                if (s.loading) Text("Cargando ${cur.name}…", fontFamily = Inter, fontSize = 14.sp, color = Tokens.sageText)
                else if (s.verses.isEmpty()) Text("Capítulo no disponible.", fontFamily = Inter, fontSize = 14.sp, color = Tokens.sageText)
            }
        }
        items(s.verses.size, key = { "v-${s.bookNum}-${s.chapter}-${it}" }) { i ->
            val (n, text) = s.verses[i]
            val target = n == s.targetVerse
            Row(
                Modifier.padding(horizontal = 20.dp).fillMaxWidth()
                    .then(if (target) Modifier.background(Color(0x40F8F0D7), RoundedCornerShape(8.dp)) else Modifier)
                    .pressable(scale = 1f, role = androidx.compose.ui.semantics.Role.Button) { vm.tapVerse(n) }
                    .padding(vertical = 5.dp, horizontal = 4.dp)
                    .testTag("verse-$n"),
            ) {
                Text(
                    buildAnnotatedString {
                        withStyle(SpanStyle(fontFamily = Inter, fontSize = (11 * s.scale).sp, fontWeight = FontWeight.Bold, color = Ochre.label, baselineShift = BaselineShift.Superscript)) { append("$n ") }
                        append(text)
                    },
                    fontFamily = Fraunces, fontSize = (17 * s.scale).sp, lineHeight = (30 * s.scale).sp, color = Tokens.ink,
                )
            }
        }
        item {
            Column(Modifier.padding(horizontal = 20.dp)) {
                Spacer(Modifier.height(40.dp))
                Hairline(0.18f)
                Spacer(Modifier.height(22.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.alpha(if (s.isFirst) 0.35f else 1f)) { PagerPill("← Anterior") { if (!s.isFirst) vm.prev() } }
                    Text("${cur.abbr} · ${s.chapter} / ${cur.chapters}", fontFamily = Fraunces, fontStyle = FontStyle.Italic, fontSize = 13.sp, color = Tokens.sageText)
                    Box(Modifier.alpha(if (s.isLast) 0.35f else 1f)) { PagerPill("Siguiente →") { if (!s.isLast) vm.next() } }
                }
                Spacer(Modifier.height(60.dp))
            }
            SiteFooter()
        }
    }

    // ======= picker (books / chapters) =======
    if (picker) {
        val sheet = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(onDismissRequest = { picker = false }, sheetState = sheet, containerColor = Tokens.cream, modifier = Modifier.testTag("bible-picker")) {
            Column(Modifier.fillMaxHeight(0.9f).padding(horizontal = 20.dp).padding(bottom = 24.dp)) {
                if (pickerBook == 0) {
                    EyebrowLine("— Selecciona un libro", Tokens.sage)
                    Spacer(Modifier.height(14.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        for ((id, label, n) in listOf(Triple("AT", "Antiguo Testamento", 39), Triple("NT", "Nuevo Testamento", 27))) {
                            val active = testament == id
                            Row(
                                Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).background(if (active) Tokens.forest else Tokens.sageA(0.08f)).pressable(scale = 0.98f) { testament = id }
                                    .padding(horizontal = 12.dp, vertical = 12.dp).testTag("picker-tab-$id"),
                                horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(n.toString(), fontFamily = Fraunces, fontStyle = FontStyle.Italic, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = if (active) Color(0xFFE8D091) else Ochre.label)
                                Text(label, fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = if (active) Tokens.cream else Tokens.forest)
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Column(Modifier.verticalScroll(rememberScrollState())) {
                        s.books.filter { (testament == "AT") == it.isOldTestament }.forEach { b ->
                            val current = b.num == s.bookNum
                            Row(
                                Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(if (current) Tokens.sageA(0.12f) else Color.Transparent)
                                    .pressable(scale = 0.99f) { pickerBook = b.num }.padding(horizontal = 10.dp, vertical = 11.dp).testTag("picker-book-${b.num}"),
                                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                Text(b.abbr, fontFamily = Fraunces, fontStyle = FontStyle.Italic, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Ochre.label, modifier = Modifier.width(34.dp))
                                Text(b.name, fontFamily = Fraunces, fontWeight = FontWeight.Medium, fontSize = 16.sp, color = Tokens.ink, modifier = Modifier.weight(1f))
                                Text(b.chapters.toString(), fontFamily = Inter, fontSize = 11.sp, color = Tokens.sageText)
                            }
                        }
                    }
                } else {
                    val pb = s.books.first { it.num == pickerBook }
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Row(Modifier.clip(CircleShape).pressable(scale = 0.98f) { pickerBook = 0 }.padding(horizontal = 8.dp, vertical = 6.dp).testTag("picker-books"), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(painterResource(R.drawable.ic_back), null, tint = Tokens.sage, modifier = Modifier.size(14.dp))
                            Text("Cambiar libro", fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Tokens.sage)
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text(pb.abbr, fontFamily = Fraunces, fontStyle = FontStyle.Italic, fontWeight = FontWeight.Bold, fontSize = 26.sp, color = Ochre.numeral)
                        Column {
                            Text(pb.name, fontFamily = Fraunces, fontWeight = FontWeight.SemiBold, fontSize = 20.sp, color = Tokens.inkDeep)
                            Text("${pb.chapters} capítulos · ${if (pb.num < 40) "Antiguo" else "Nuevo"} Testamento", fontFamily = Inter, fontSize = 12.sp, color = Tokens.sageText)
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    FlowRow(Modifier.verticalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        for (c in 1..pb.chapters) {
                            val current = pb.num == s.bookNum && c == s.chapter
                            Box(
                                Modifier.size(46.dp).clip(RoundedCornerShape(10.dp)).background(if (current) Tokens.forest else Color(0x8CFFFFFF)).border(1.dp, if (current) Tokens.forest else Tokens.sageA(0.18f), RoundedCornerShape(10.dp))
                                    .pressable(scale = 0.95f) { picker = false; vm.open(pb.num, c) }.testTag("picker-chapter-$c"),
                                contentAlignment = Alignment.Center,
                            ) { Text(c.toString(), fontFamily = Fraunces, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = if (current) Tokens.cream else Tokens.forest) }
                        }
                    }
                }
            }
        }
    }

    // ======= drawer: Notas / Marcadores / Recientes =======
    if (drawer != null) {
        val sheet = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(onDismissRequest = { drawer = null }, sheetState = sheet, containerColor = Tokens.cream, modifier = Modifier.testTag("bible-drawer")) {
            Column(Modifier.padding(horizontal = 20.dp).padding(bottom = 28.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    for ((id, label) in listOf("notes" to "Notas", "bookmarks" to "Marcadores", "recent" to "Recientes")) {
                        val active = drawer == id
                        Box(Modifier.clip(CircleShape).background(if (active) Tokens.forest else Tokens.sageA(0.08f)).pressable(scale = 0.97f) { drawer = id }.padding(horizontal = 14.dp, vertical = 8.dp).testTag("panel-tab-$id")) {
                            Text(label + (if (id == "bookmarks" && s.bookmarks.isNotEmpty()) " ${s.bookmarks.size}" else ""), fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = if (active) Tokens.cream else Tokens.forest)
                        }
                    }
                }
                Spacer(Modifier.height(20.dp))
                when (drawer) {
                    "notes" -> {
                        EyebrowLine("— Notas de ${cur.name} ${s.chapter}", Tokens.sage, size = 10.5f)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            run { val saved = s.savedAt; when { saved != null -> "Guardado ${relativeTimeEs(saved)}"; s.notes.isNotEmpty() -> "Tus apuntes anteriores"; else -> "Empieza a escribir — guardamos automáticamente" } },
                            fontFamily = Inter, fontSize = 12.sp, color = Tokens.sageText,
                        )
                        Spacer(Modifier.height(12.dp))
                        val areaShape = RoundedCornerShape(12.dp)
                        Box(Modifier.fillMaxWidth().height(180.dp).clip(areaShape).background(Color(0xB3FFFFFF)).border(1.dp, Tokens.sageA(0.2f), areaShape).padding(14.dp)) {
                            if (s.notes.isEmpty()) Text("Escribe lo que aprendiste, citas favoritas, aplicaciones personales…", fontFamily = Inter, fontSize = 14.sp, color = Color(0xFF9AA792))
                            BasicTextField(s.notes, vm::notes, textStyle = TextStyle(fontFamily = Inter, fontSize = 14.5.sp, lineHeight = 22.sp, color = Tokens.ink), cursorBrush = SolidColor(Tokens.sage), modifier = Modifier.fillMaxSize().testTag("notes-area"))
                        }
                        if (s.notes.isNotEmpty()) Text("Borrar nota", fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, color = Color(0xFF8A3A2A), modifier = Modifier.padding(top = 8.dp).pressable(scale = 0.97f, onClick = vm::clearNotes))
                    }
                    "bookmarks" -> {
                        if (s.bookmarks.isEmpty()) Text("Aún no hay marcadores. Toca el marcador de la barra para guardar un capítulo.", fontFamily = Inter, fontSize = 14.sp, lineHeight = 21.sp, color = Tokens.sageText)
                        s.bookmarks.forEach { bm ->
                            Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Column(Modifier.weight(1f).pressable(scale = 0.98f) { drawer = null; vm.open(bm.bookNum, bm.chapter) }) {
                                    Text("${bm.bookName} ${bm.chapter}", fontFamily = Fraunces, fontWeight = FontWeight.Medium, fontSize = 16.sp, color = Tokens.ink)
                                    Text(relativeTimeEs(bm.addedAt), fontFamily = Inter, fontSize = 11.sp, color = Tokens.sageText)
                                }
                                Icon(painterResource(R.drawable.ic_nav_close), "Quitar marcador", tint = Tokens.sageText, modifier = Modifier.size(16.dp).pressable { vm.removeBookmark(bm.bookNum, bm.chapter) })
                            }
                            Hairline(0.12f)
                        }
                    }
                    "recent" -> {
                        if (s.recent.isEmpty()) Text("Todavía no has leído ningún capítulo.", fontFamily = Inter, fontSize = 14.sp, color = Tokens.sageText)
                        s.recent.forEach { r ->
                            Row(Modifier.fillMaxWidth().pressable(scale = 0.98f) { drawer = null; vm.open(r.bookNum, r.chapter) }.padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text("${r.bookName} ${r.chapter}", fontFamily = Fraunces, fontWeight = FontWeight.Medium, fontSize = 16.sp, color = Tokens.ink, modifier = Modifier.weight(1f))
                                Text(relativeTimeEs(r.viewedAt), fontFamily = Inter, fontSize = 11.sp, color = Tokens.sageText)
                            }
                            Hairline(0.12f)
                        }
                    }
                }
            }
        }
    }
}
