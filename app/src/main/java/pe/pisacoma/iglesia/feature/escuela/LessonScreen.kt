package pe.pisacoma.iglesia.feature.escuela

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import pe.pisacoma.iglesia.nav.Route
import pe.pisacoma.iglesia.ui.chrome.SiteFooter
import pe.pisacoma.iglesia.ui.components.BibleRefSheet
import pe.pisacoma.iglesia.ui.components.EyebrowLine
import pe.pisacoma.iglesia.ui.components.Hairline
import pe.pisacoma.iglesia.ui.components.MarkdownText
import pe.pisacoma.iglesia.ui.components.ShareButton
import pe.pisacoma.iglesia.ui.components.TextSizeControls
import pe.pisacoma.iglesia.ui.components.ToolbarIcon
import pe.pisacoma.iglesia.ui.components.pressable
import pe.pisacoma.iglesia.ui.theme.Fraunces
import pe.pisacoma.iglesia.ui.theme.Inter
import pe.pisacoma.iglesia.ui.theme.Tokens
import pe.pisacoma.iglesia.util.LessonMarkdown

/** The site's /escuela-sabatica/<n>: sticky toolbar, crumbs + title, 7 day tabs, the day's text, done CTA, pager, drawer, verse popup. */
@Composable
fun LessonScreen(number: Int, day: Int?, modifier: Modifier, go: (Route) -> Unit) {
    val vm: LessonViewModel = viewModel(key = "lesson-$number") { LessonViewModel(App.repos.quarterly, App.repos.quizzes, App.repos.userState, number, day) }
    val s by vm.state.collectAsState()
    var drawer by remember { mutableStateOf<String?>(null) } // "notes" | "bookmarks" | "progress"
    var ref by remember { mutableStateOf<Pair<String, String>?>(null) } // key, display

    if (s.notFound) {
        Column(modifier.padding(20.dp)) {
            Text("Lección no encontrada", fontFamily = Fraunces, fontSize = 24.sp, color = Tokens.inkDeep)
            Text("← Volver", fontFamily = Inter, color = Tokens.sage, modifier = Modifier.padding(top = 12.dp).pressable { go(Route.Escuela) })
        }
        return
    }
    val q = s.quarterly ?: return
    val lesson = s.lesson ?: return
    val current = s.days.getOrNull(s.activeDay) ?: return

    Column(modifier.fillMaxSize()) {
        // ======= sticky toolbar (.toolbar) =======
        Row(
            Modifier.fillMaxWidth().background(Color(0xF2F7F4E6)).padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(Modifier.clip(CircleShape).pressable(scale = 0.97f) { go(Route.Escuela) }.padding(horizontal = 8.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(painterResource(R.drawable.ic_back), "Volver a las lecciones", tint = Tokens.sage, modifier = Modifier.size(14.dp))
                Text("Lecciones", fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Tokens.sage)
            }
            Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Lec. ${lesson.number}", fontFamily = Fraunces, fontStyle = FontStyle.Italic, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Tokens.forest)
                Text("·", color = Tokens.sageText)
                Text("Día ${s.activeDay + 1}", fontFamily = Inter, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.2.sp, color = Tokens.sageText)
            }
            // progress pill
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(Modifier.width(44.dp).height(4.dp).clip(CircleShape).background(Tokens.sageA(0.14f))) {
                    Box(Modifier.fillMaxWidth(s.pct / 100f).height(4.dp).clip(CircleShape).background(Tokens.sage))
                }
                Text("${s.pct}%", fontFamily = Inter, fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = Tokens.sageText)
            }
            ToolbarIcon(R.drawable.ic_check, if (s.isDone) "Marcar día como no leído" else "Marcar día como completado", active = s.isDone, testTag = "day-done", onClick = vm::toggleDone)
            ToolbarIcon(R.drawable.ic_bookmark, if (s.isBookmarked) "Quitar marcador" else "Agregar marcador", active = s.isBookmarked, testTag = "lesson-bookmark", onClick = vm::toggleBookmark)
            TextSizeControls(vm::smaller, vm::larger)
            ToolbarIcon(R.drawable.ic_note, "Notas, marcadores y progreso", active = drawer != null, testTag = "lesson-notes", onClick = { drawer = "notes" })
        }
        Hairline(0.12f)

        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            Column(Modifier.padding(horizontal = 20.dp)) {
                // .crumbs
                Row(Modifier.padding(top = 22.dp, bottom = 18.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("ESCUELA SABÁTICA", fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, letterSpacing = 2.2.sp, color = Tokens.sage, modifier = Modifier.pressable(scale = 0.98f) { go(Route.Escuela) })
                    Text("/", fontFamily = Inter, fontSize = 11.sp, color = Tokens.sageText.copy(alpha = 0.5f))
                    Text("LECCIÓN ${lesson.number}", fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, letterSpacing = 2.2.sp, color = Tokens.sageText)
                }
                // .head
                EyebrowLine("${lesson.startDate} — ${lesson.endDate}", Tokens.sageText, size = 11f, tracking = 1.8f)
                Spacer(Modifier.height(10.dp))
                Text("Lección ${lesson.number}: ${lesson.title}", fontFamily = Fraunces, fontWeight = FontWeight.SemiBold, fontSize = 26.sp, lineHeight = 29.sp, letterSpacing = (-0.022 * 26).sp, color = Tokens.inkDeep)
                Spacer(Modifier.height(6.dp))
                Text(q.title, fontFamily = Fraunces, fontStyle = FontStyle.Italic, fontSize = 15.sp, color = Tokens.sageText)
                Spacer(Modifier.height(22.dp))
            }
            // .tabs — 7 days, horizontally scrollable on a phone
            Row(Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                s.days.forEachIndexed { i, d ->
                    val active = i == s.activeDay
                    val done = i in s.doneDays
                    val shape = RoundedCornerShape(12.dp)
                    Column(
                        Modifier.clip(shape)
                            .background(if (active) Tokens.forest else if (done) Tokens.sageA(0.12f) else Color(0x8CFFFFFF))
                            .border(1.dp, if (active) Tokens.forest else Tokens.sageA(0.18f), shape)
                            .pressable(scale = 0.97f) { vm.selectDay(i) }
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                            .width(104.dp)
                            .testTag(if (active) "day-tab-$i-selected" else "day-tab-$i"),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(DAY_SHORT[i].uppercase(), fontFamily = Inter, fontWeight = FontWeight.Bold, fontSize = 10.sp, letterSpacing = 1.6.sp, color = if (active) Tokens.cream.copy(alpha = 0.8f) else Tokens.sage)
                            if (done) Icon(painterResource(R.drawable.ic_check), "Día completado", tint = if (active) Tokens.cream else Tokens.sage, modifier = Modifier.size(10.dp))
                            if (i in s.daysWithNotes) Icon(painterResource(R.drawable.ic_note), "Día con notas", tint = if (active) Tokens.cream else Tokens.sage, modifier = Modifier.size(10.dp))
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(d.title.ifEmpty { DAY_LABELS[i] }, fontFamily = Fraunces, fontWeight = FontWeight.Medium, fontSize = 12.5.sp, lineHeight = 15.sp, maxLines = 2, color = if (active) Tokens.cream else Tokens.ink)
                    }
                }
            }
            Spacer(Modifier.height(22.dp))
            Column(Modifier.padding(horizontal = 20.dp)) {
                // the day title (site: front matter title above the content)
                Text(current.title.ifEmpty { DAY_LABELS[s.activeDay] }, fontFamily = Fraunces, fontWeight = FontWeight.SemiBold, fontSize = 22.sp, lineHeight = 26.sp, color = Tokens.inkDeep)
                Spacer(Modifier.height(8.dp))
                EyebrowLine(DAY_LABELS[s.activeDay], Tokens.sageText, size = 10.5f)
                Spacer(Modifier.height(16.dp))
                MarkdownText(current.blocks, s.scale, onRef = { key -> ref = key to displayFor(key, current) }, modifier = Modifier.testTag("lesson-content"))
                Spacer(Modifier.height(30.dp))
                // .rd-mark-cta
                val markShape = RoundedCornerShape(14.dp)
                Box(
                    Modifier.fillMaxWidth().clip(markShape).background(if (s.isDone) Tokens.sageA(0.12f) else Tokens.forest)
                        .border(1.dp, if (s.isDone) Tokens.sageA(0.3f) else Tokens.forest, markShape)
                        .pressable(scale = 0.98f, onClick = vm::toggleDone).padding(vertical = 15.dp).testTag("day-mark-cta"),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        if (s.isDone) "Día completado · toca para deshacer" else "Marcar día como completado",
                        fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = if (s.isDone) Tokens.forest else Tokens.cream,
                    )
                }
                Spacer(Modifier.height(14.dp))
                when {
                    s.activeDay < s.totalDays - 1 -> Row(Modifier.pressable(scale = 0.98f) { vm.selectDay(s.activeDay + 1) }.padding(vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Siguiente:", fontFamily = Inter, fontSize = 14.sp, color = Tokens.sageText)
                        Text(DAY_LABELS[s.activeDay + 1], fontFamily = Fraunces, fontStyle = FontStyle.Italic, fontSize = 15.sp, color = Tokens.forest)
                        Text("→", fontFamily = Inter, fontSize = 14.sp, color = Tokens.sage)
                    }
                    lesson.number < q.lessons.size -> Row(Modifier.pressable(scale = 0.98f) { go(Route.Leccion(lesson.number + 1)) }.padding(vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("¡Felicidades! Has terminado la lección ·", fontFamily = Inter, fontSize = 14.sp, color = Tokens.sageText)
                        Text("Lección ${lesson.number + 1} →", fontFamily = Fraunces, fontStyle = FontStyle.Italic, fontSize = 15.sp, color = Tokens.forest)
                    }
                    else -> Text("¡Felicidades! Has terminado el trimestre.", fontFamily = Inter, fontSize = 14.sp, color = Tokens.sageText)
                }
                Spacer(Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ShareButton(
                        label = "Compartir este día",
                        title = "Lección ${lesson.number} · ${DAY_LABELS[s.activeDay]}",
                        text = "Escuela Sabática · Lección ${lesson.number}: ${lesson.title} · ${DAY_LABELS[s.activeDay]}" + (if (s.activeDay > 0 && current.title.isNotEmpty()) ": ${current.title}" else ""),
                        path = "/escuela-sabatica/${lesson.number}/?dia=${s.activeDay}",
                    )
                    s.quizSlug?.let { slug ->
                        Box(Modifier.clip(CircleShape).background(Tokens.forest).pressable(scale = 0.97f) { go(Route.QuizPack(slug)) }.padding(horizontal = 16.dp, vertical = 9.dp).testTag("lesson-quiz")) {
                            Text("Quiz de la lección", fontFamily = Inter, fontWeight = FontWeight.Medium, fontSize = 14.4.sp, color = Tokens.cream)
                        }
                    }
                }
                // .pager
                Spacer(Modifier.height(40.dp))
                Hairline(0.18f)
                Spacer(Modifier.height(22.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally)) {
                    if (lesson.number > 1) PagerPill("← Lección ${lesson.number - 1}") { go(Route.Leccion(lesson.number - 1)) }
                    PagerPill("Todas las lecciones", ochre = true) { go(Route.Escuela) }
                    if (lesson.number < q.lessons.size) PagerPill("Lección ${lesson.number + 1} →") { go(Route.Leccion(lesson.number + 1)) }
                }
                Spacer(Modifier.height(60.dp))
            }
            SiteFooter()
        }
    }

    // ======= drawer: Notas / Marcadores / Progreso =======
    if (drawer != null) {
        val sheet = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(onDismissRequest = { drawer = null }, sheetState = sheet, containerColor = Tokens.cream, modifier = Modifier.testTag("lesson-drawer")) {
            Column(Modifier.padding(horizontal = 20.dp).padding(bottom = 28.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    for ((id, label) in listOf("notes" to "Notas", "bookmarks" to "Marcadores", "progress" to "Progreso")) {
                        val active = drawer == id
                        Box(
                            Modifier.clip(CircleShape).background(if (active) Tokens.forest else Tokens.sageA(0.08f)).pressable(scale = 0.97f) { drawer = id }
                                .padding(horizontal = 14.dp, vertical = 8.dp).testTag("drawer-tab-$id"),
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(label, fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = if (active) Tokens.cream else Tokens.forest)
                                if (id == "bookmarks" && s.bookmarks.isNotEmpty()) Text(s.bookmarks.size.toString(), fontFamily = Inter, fontSize = 11.sp, color = if (active) Tokens.cream else Tokens.sage)
                                if (id == "notes" && s.activeDay in s.daysWithNotes) Box(Modifier.size(6.dp).clip(CircleShape).background(if (active) Tokens.cream else Tokens.sage))
                            }
                        }
                    }
                }
                Spacer(Modifier.height(20.dp))
                when (drawer) {
                    "notes" -> {
                        EyebrowLine("— Notas del día ${s.activeDay + 1}", Tokens.sage, size = 10.5f)
                        Spacer(Modifier.height(6.dp))
                        Text(current.title.ifEmpty { DAY_LABELS[s.activeDay] }, fontFamily = Fraunces, fontWeight = FontWeight.SemiBold, fontSize = 20.sp, color = Tokens.inkDeep)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            run { val saved = s.savedAt; when { saved != null -> "Guardado ${relativeTimeEs(saved)}"; s.notes.isNotEmpty() -> "Tus apuntes anteriores"; else -> "Empieza a escribir — guardamos automáticamente" } },
                            fontFamily = Inter, fontSize = 12.sp, color = Tokens.sageText,
                        )
                        Spacer(Modifier.height(12.dp))
                        val areaShape = RoundedCornerShape(12.dp)
                        Box(Modifier.fillMaxWidth().height(180.dp).clip(areaShape).background(Color(0xB3FFFFFF)).border(1.dp, Tokens.sageA(0.2f), areaShape).padding(14.dp)) {
                            if (s.notes.isEmpty()) Text("Escribe lo que aprendiste, citas favoritas, preguntas para el sábado, aplicaciones personales…", fontFamily = Inter, fontSize = 14.sp, color = Color(0xFF9AA792))
                            BasicTextField(s.notes, vm::notes, textStyle = TextStyle(fontFamily = Inter, fontSize = 14.5.sp, lineHeight = 22.sp, color = Tokens.ink), cursorBrush = SolidColor(Tokens.sage), modifier = Modifier.fillMaxSize().testTag("notes-area"))
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("${s.notes.length} caracteres · ${s.notes.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }.size} palabras", fontFamily = Inter, fontSize = 11.sp, color = Tokens.sageText)
                            if (s.notes.isNotEmpty()) Text("Borrar nota", fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, color = Color(0xFF8A3A2A), modifier = Modifier.pressable(scale = 0.97f, onClick = vm::clearNotes))
                        }
                    }
                    "bookmarks" -> {
                        if (s.bookmarks.isEmpty()) {
                            Text("Aún no hay marcadores", fontFamily = Fraunces, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, color = Tokens.inkDeep)
                            Text("Toca el botón de marcador en la barra superior para guardar lecciones importantes del trimestre.", fontFamily = Inter, fontSize = 14.sp, lineHeight = 21.sp, color = Tokens.sageText, modifier = Modifier.padding(top = 6.dp))
                        } else s.bookmarks.forEach { bm ->
                            Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("Lec. ${bm.leccionN}", fontFamily = Fraunces, fontStyle = FontStyle.Italic, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Tokens.forest, modifier = Modifier.width(52.dp))
                                Column(Modifier.weight(1f).pressable(scale = 0.98f) { drawer = null; go(Route.Leccion(bm.leccionN)) }) {
                                    Text(bm.title, fontFamily = Fraunces, fontWeight = FontWeight.Medium, fontSize = 15.sp, color = Tokens.ink)
                                    Text(relativeTimeEs(bm.addedAt), fontFamily = Inter, fontSize = 11.sp, color = Tokens.sageText)
                                }
                                Icon(painterResource(R.drawable.ic_nav_close), "Quitar marcador", tint = Tokens.sageText, modifier = Modifier.size(16.dp).pressable { vm.removeBookmark(bm.leccionN) })
                            }
                            Hairline(0.12f)
                        }
                    }
                    "progress" -> {
                        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("${s.pct}", fontFamily = Fraunces, fontStyle = FontStyle.Italic, fontWeight = FontWeight.Bold, fontSize = 40.sp, color = Tokens.forest)
                            Text("%", fontFamily = Fraunces, fontSize = 18.sp, color = Tokens.sage, modifier = Modifier.padding(bottom = 6.dp))
                            Spacer(Modifier.width(10.dp))
                            Text("${s.completedCount} de ${s.totalDays} días", fontFamily = Inter, fontSize = 13.sp, color = Tokens.sageText, modifier = Modifier.padding(bottom = 8.dp))
                        }
                        Spacer(Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(22.dp)) {
                            Stat(s.completedCount, "completados"); Stat(s.daysWithNotes.size, "con notas"); Stat(s.totalDays - s.completedCount, "restantes")
                        }
                        Spacer(Modifier.height(18.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            EyebrowLine("Lecciones del trimestre", Tokens.sageText, size = 10.5f)
                            Text(q.lessons.size.toString(), fontFamily = Inter, fontSize = 11.sp, color = Tokens.sageText)
                        }
                        Spacer(Modifier.height(8.dp))
                        q.lessons.forEach { l ->
                            val done = s.progress[l.number] ?: 0
                            val isCur = l.number == lesson.number
                            Row(Modifier.fillMaxWidth().pressable(scale = 0.98f) { drawer = null; go(Route.Leccion(l.number)) }.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text(l.number.toString().padStart(2, '0'), fontFamily = Fraunces, fontStyle = FontStyle.Italic, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = if (isCur) Tokens.forest else Tokens.sageText, modifier = Modifier.width(26.dp))
                                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                    Text(l.title, fontFamily = Fraunces, fontWeight = if (isCur) FontWeight.SemiBold else FontWeight.Medium, fontSize = 14.sp, color = Tokens.ink, maxLines = 1)
                                    Box(Modifier.fillMaxWidth().height(3.dp).clip(CircleShape).background(Tokens.sageA(0.14f))) {
                                        Box(Modifier.fillMaxWidth(done / l.days.size.toFloat()).height(3.dp).clip(CircleShape).background(Tokens.sage))
                                    }
                                }
                                Text("$done/${l.days.size}", fontFamily = Inter, fontSize = 11.sp, color = Tokens.sageText)
                            }
                        }
                    }
                }
            }
        }
    }

    ref?.let { (key, display) -> BibleRefSheet(key, display, App.repos.bible) { ref = null } }
}

/** The text the reference was written as (for the popup title), found back in the day's blocks. */
private fun displayFor(key: String, day: DayView): String {
    for (b in day.blocks) {
        val t = when (b) {
            is pe.pisacoma.iglesia.util.MdBlock.Paragraph -> b.text
            is pe.pisacoma.iglesia.util.MdBlock.Quote -> b.text
            is pe.pisacoma.iglesia.util.MdBlock.Prompt -> b.text
            is pe.pisacoma.iglesia.util.MdBlock.Bullet -> b.text
            else -> null
        } ?: continue
        t.getStringAnnotations(LessonMarkdown.REF_TAG, 0, t.length).firstOrNull { it.item == key }?.let { return t.text.substring(it.start, it.end) }
    }
    return key
}

@Composable
private fun Stat(n: Int, label: String) {
    Column {
        Text(n.toString(), fontFamily = Fraunces, fontStyle = FontStyle.Italic, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Tokens.inkDeep)
        EyebrowLine(label, Tokens.sageText, size = 10f, tracking = 1.4f)
    }
}

@Composable
internal fun PagerPill(label: String, ochre: Boolean = false, onClick: () -> Unit) {
    Box(
        Modifier.clip(CircleShape).background(Color(0x80FFFFFF))
            .border(1.dp, if (ochre) Color(0x66B8924F) else Tokens.sageA(0.24f), CircleShape)
            .pressable(scale = 0.97f, onClick = onClick).padding(horizontal = 18.dp, vertical = 11.dp),
    ) {
        Text(label, fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = if (ochre) pe.pisacoma.iglesia.ui.components.Ochre.label else Tokens.forest)
    }
}
