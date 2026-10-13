package pe.pisacoma.iglesia.feature.quiz

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import pe.pisacoma.iglesia.App
import pe.pisacoma.iglesia.nav.Route
import pe.pisacoma.iglesia.ui.chrome.SiteFooter
import pe.pisacoma.iglesia.ui.components.pressable
import pe.pisacoma.iglesia.ui.theme.Inter
import pe.pisacoma.iglesia.ui.theme.Playfair
import pe.pisacoma.iglesia.ui.theme.Tokens

// quiz-view.css / quiz index.tsx tokens
private val BG = Tokens.bg               // #0d1117
private val CARD = Tokens.bg2            // #161b22
private val BORDER = Tokens.border       // #21262d
private val GOLD = Tokens.accent         // #c9963b
private val GOLD_LIGHT = Tokens.accentLight // #e8b85a
private val TEXT = Color(0xFFE6E9EE)
private val GOLD_GRADIENT = Brush.linearGradient(listOf(GOLD, GOLD_LIGHT))

@Composable
private fun Crumbs(first: String, second: String, onFirst: (() -> Unit)? = null) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally), modifier = Modifier.fillMaxWidth()) {
        Text(first.uppercase(), fontFamily = Inter, fontSize = 12.5.sp, letterSpacing = 1.sp, color = GOLD, modifier = if (onFirst != null) Modifier.pressable(scale = 0.98f, onClick = onFirst) else Modifier)
        Text("/", fontFamily = Inter, fontSize = 12.5.sp, color = Color(0x40FFFFFF))
        Text(second.uppercase(), fontFamily = Inter, fontSize = 12.5.sp, letterSpacing = 1.sp, color = Color(0xB3FFFFFF))
    }
}

@Composable
private fun Badge(text: String) {
    Row(
        Modifier.clip(CircleShape).background(Color(0x0FC9963B)).border(1.dp, Color(0x73C9963B), CircleShape).padding(horizontal = 15.dp, vertical = 7.dp),
        horizontalArrangement = Arrangement.spacedBy(9.dp), verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(6.dp).clip(CircleShape).background(GOLD))
        Text(text.uppercase(), fontFamily = Inter, fontSize = 11.8.sp, letterSpacing = 1.1.sp, color = GOLD_LIGHT)
    }
}

/** `.hero` of the quiz pages: the gold glow over the dark base. */
private fun heroBrush() = Brush.verticalGradient(0f to Color(0xFF0F1923), 1f to BG)

/** The site's /quiz: the packs, newest first. */
@Composable
fun QuizIndexScreen(modifier: Modifier, go: (Route) -> Unit) {
    val vm: QuizIndexViewModel = viewModel { QuizIndexViewModel(App.repos.quizzes) }
    val packs by vm.packs.collectAsState()
    LazyColumn(modifier.background(BG)) {
        item {
            Column(Modifier.fillMaxWidth().background(heroBrush()).padding(horizontal = 24.dp, vertical = 48.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Crumbs("Escuela Sabática", "Quizzes")
                Spacer(Modifier.height(22.dp))
                Badge("Quizzes oficiales · Iglesia Adventista del Séptimo Día")
                Spacer(Modifier.height(22.dp))
                Text("Quizzes de la Escuela Sabática", fontFamily = Playfair, fontWeight = FontWeight.SemiBold, fontSize = 38.sp, lineHeight = 42.sp, color = Color.White, textAlign = TextAlign.Center)
                Spacer(Modifier.height(14.dp))
                Text("Pon a prueba lo que aprendiste en la lección de cada semana. Selecciona una pregunta a la vez, confírmala, y al final revisa tus respuestas.", fontFamily = Inter, fontSize = 16.sp, lineHeight = 25.sp, color = Color(0xB3FFFFFF), textAlign = TextAlign.Center)
            }
            Box(Modifier.fillMaxWidth().height(1.dp).background(BORDER))
            Spacer(Modifier.height(28.dp))
        }
        items(packs.size, key = { packs[it].slug }) { i ->
            val q = packs[i]
            val shape = RoundedCornerShape(16.dp)
            Column(
                Modifier.padding(horizontal = 20.dp).padding(bottom = 14.dp).fillMaxWidth().clip(shape).background(CARD).border(1.dp, BORDER, shape)
                    .pressable(scale = 0.985f) { go(Route.QuizPack(q.slug)) }.padding(22.dp).testTag("quiz-card-${q.slug}"),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.clip(RoundedCornerShape(8.dp)).background(Color(0x24C9963B)).padding(horizontal = 9.dp, vertical = 4.dp)) {
                        Text("L${q.lessonNumber}", fontFamily = Playfair, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = GOLD_LIGHT)
                    }
                    Text(q.badge.uppercase(), fontFamily = Inter, fontSize = 11.sp, letterSpacing = 1.sp, color = Color(0x8CFFFFFF))
                }
                Text(q.title, fontFamily = Playfair, fontWeight = FontWeight.SemiBold, fontSize = 21.sp, lineHeight = 26.sp, color = Color.White)
                Text("${q.questions.size} preguntas · respuestas confirmadas una a una", fontFamily = Inter, fontSize = 13.5.sp, color = Color(0x99FFFFFF))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Comenzar quiz", fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = GOLD_LIGHT)
                    Text("→", fontFamily = Inter, fontSize = 14.sp, color = GOLD_LIGHT)
                }
            }
        }
        item { Spacer(Modifier.height(30.dp)); SiteFooter() }
    }
}

/** The site's /quiz/<slug> (components/quiz-view.tsx): hero + memory verse, sticky progress, question cards, submit/results, confirm modal. */
@Composable
fun QuizScreen(slug: String, modifier: Modifier, go: (Route) -> Unit) {
    val vm: QuizViewModel = viewModel(key = "quiz-$slug") { QuizViewModel(App.repos.quizzes, App.repos.userState, slug) }
    val ui by vm.ui.collectAsState()
    if (ui.notFound) {
        Column(modifier.background(BG).padding(24.dp)) { Text("Quiz no encontrado", fontFamily = Playfair, fontSize = 24.sp, color = Color.White) }
        return
    }
    val pack = ui.pack ?: return
    val st = ui.state
    val total = pack.questions.size

    Column(modifier.background(BG)) {
        // sticky progress (.progress-bar)
        Column(Modifier.fillMaxWidth().background(Color(0xEB0D1117)).padding(horizontal = 20.dp, vertical = 10.dp)) {
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(st.committedCount.toString(), fontFamily = Inter, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = GOLD_LIGHT)
                Text("/ $total confirmadas", fontFamily = Inter, fontSize = 13.sp, color = Color(0x8CFFFFFF), modifier = Modifier.testTag("quiz-progress"))
            }
            Spacer(Modifier.height(6.dp))
            Box(Modifier.fillMaxWidth().height(5.dp).clip(CircleShape).background(Color(0x1AFFFFFF))) {
                Box(Modifier.fillMaxWidth(st.pct(pack) / 100f).height(5.dp).clip(CircleShape).background(GOLD_GRADIENT))
            }
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(BORDER))

        LazyColumn(Modifier.testTag("quiz-page")) {
            item {
                Column(Modifier.fillMaxWidth().background(heroBrush()).padding(horizontal = 24.dp, vertical = 40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Crumbs("Quizzes", "Lección ${pack.lessonNumber} · Quiz") { go(Route.Quiz) }
                    Spacer(Modifier.height(20.dp))
                    Badge(pack.badge)
                    Spacer(Modifier.height(22.dp))
                    Text(pack.title, fontFamily = Playfair, fontWeight = FontWeight.SemiBold, fontSize = 36.sp, lineHeight = 40.sp, color = Color.White, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(14.dp))
                    Text(pack.subtitle, fontFamily = Inter, fontSize = 16.sp, lineHeight = 25.sp, color = Color(0xB3FFFFFF), textAlign = TextAlign.Center)
                    Spacer(Modifier.height(30.dp))
                    val shape = RoundedCornerShape(14.dp)
                    Column(Modifier.fillMaxWidth().clip(shape).background(Color(0x08FFFFFF)).border(1.dp, BORDER, shape).padding(horizontal = 24.dp, vertical = 20.dp)) {
                        Text("TEXTO PARA MEMORIZAR", fontFamily = Inter, fontSize = 11.sp, letterSpacing = 1.4.sp, color = GOLD)
                        Spacer(Modifier.height(8.dp))
                        Text(pack.memorize.text, fontFamily = Playfair, fontWeight = FontWeight.SemiBold, fontSize = 17.sp, lineHeight = 26.sp, color = Color.White)
                        Spacer(Modifier.height(8.dp))
                        Text("— ${pack.memorize.ref}", fontFamily = Inter, fontSize = 13.sp, color = Color(0x99FFFFFF))
                    }
                }
                Box(Modifier.fillMaxWidth().height(1.dp).background(BORDER))
                Spacer(Modifier.height(24.dp))
            }
            items(pack.questions.size, key = { pack.questions[it].n }) { i ->
                val q = pack.questions[i]
                val locked = st.committed[q.n]
                val chosen = locked ?: st.pending?.takeIf { it.qN == q.n }?.letter
                val shape = RoundedCornerShape(16.dp)
                Column(Modifier.padding(horizontal = 20.dp).padding(bottom = 16.dp).fillMaxWidth().clip(shape).background(CARD).border(1.dp, BORDER, shape).padding(start = 20.dp, end = 20.dp, top = 22.dp, bottom = 20.dp)) {
                    Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        Box(Modifier.size(38.dp).clip(RoundedCornerShape(10.dp)).background(Color(0x24C9963B)), contentAlignment = Alignment.Center) {
                            Text(q.n.toString().padStart(2, '0'), fontFamily = Playfair, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = GOLD_LIGHT)
                        }
                        Column(Modifier.weight(1f)) {
                            if (q.ref != null) { Text(q.ref.uppercase(), fontFamily = Inter, fontSize = 11.sp, letterSpacing = 1.2.sp, color = GOLD); Spacer(Modifier.height(5.dp)) }
                            Text(q.text, fontFamily = Playfair, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, lineHeight = 25.sp, color = Color.White)
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    q.options.forEach { opt ->
                        val selected = chosen == opt.letter
                        val showRight = st.submitted && opt.letter == q.correct
                        val showWrong = st.submitted && selected && opt.letter != q.correct
                        val disabled = locked != null || st.submitted
                        val optShape = RoundedCornerShape(10.dp)
                        val bg = when { showRight -> Color(0x1A2EA043); showWrong -> Color(0x1ADA3633); selected -> Color(0x1AC9963B); else -> BG }
                        val bd = when { showRight -> Color(0xFF2EA043); showWrong -> Color(0xFFDA3633); selected -> GOLD; else -> BORDER }
                        val fg = when { showRight -> Color(0xFFC8E6CD); showWrong -> Color(0xFFF0C4C2); else -> TEXT }
                        Row(
                            Modifier.fillMaxWidth().padding(bottom = 8.dp).clip(optShape).background(bg).border(1.dp, bd, optShape)
                                .alpha(if (disabled && !selected && !showRight) 0.6f else 1f)
                                .pressable(scale = 0.99f, enabled = !disabled) { vm.pick(q.n, opt.letter) }
                                .padding(horizontal = 16.dp, vertical = 13.dp)
                                .testTag("q${q.n}-${opt.letter}"),
                            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(13.dp),
                        ) {
                            Box(Modifier.size(28.dp).clip(RoundedCornerShape(7.dp)).background(Color(0x0DFFFFFF)).border(1.dp, BORDER, RoundedCornerShape(7.dp)), contentAlignment = Alignment.Center) {
                                Text(opt.letter, fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 13.5.sp, color = Color(0xBFFFFFFF))
                            }
                            Text(opt.text, fontFamily = Inter, fontSize = 15.sp, lineHeight = 21.sp, color = fg, modifier = Modifier.weight(1f))
                            if (showRight) Text("✓", color = Color(0xFF2EA043), fontSize = 16.sp)
                            if (showWrong) Text("✕", color = Color(0xFFDA3633), fontSize = 16.sp)
                        }
                    }
                    if (locked != null && !st.submitted) {
                        Spacer(Modifier.height(6.dp))
                        Text("RESPUESTA CONFIRMADA", fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 12.5.sp, letterSpacing = 1.sp, color = Color(0xD9E8B85A), modifier = Modifier.testTag("q${q.n}-locked"))
                    }
                    if (st.submitted && q.explain != null) {
                        Spacer(Modifier.height(16.dp))
                        Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(0.dp, 8.dp, 8.dp, 0.dp)).background(Color(0x0FC9963B))) {
                            Box(Modifier.width(3.dp).height(1.dp).background(GOLD))
                            Text("Nota: ${q.explain}", fontFamily = Inter, fontSize = 14.7.sp, lineHeight = 22.sp, color = Color(0xC7FFFFFF), modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp))
                        }
                    }
                }
            }
            item {
                Column(Modifier.padding(horizontal = 20.dp).padding(top = 10.dp, bottom = 40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    if (!st.submitted) {
                        val all = st.allCommitted(pack)
                        Box(
                            Modifier.alpha(if (all) 1f else 0.45f).clip(RoundedCornerShape(10.dp)).background(GOLD_GRADIENT).pressable(scale = 0.97f, enabled = all, onClick = vm::submit)
                                .padding(horizontal = 32.dp, vertical = 15.dp).testTag("quiz-submit"),
                        ) { Text("Revisar mis respuestas", fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = BG) }
                        if (!all) {
                            Spacer(Modifier.height(14.dp))
                            Text("Te faltan ${total - st.committedCount} respuestas por confirmar.", fontFamily = Inter, fontSize = 13.8.sp, color = Color(0x80FFFFFF))
                        }
                    } else {
                        val score = st.score(pack)
                        val tier = QuizState.tier(score, ui.levels)
                        val shape = RoundedCornerShape(18.dp)
                        Column(Modifier.fillMaxWidth().clip(shape).background(Brush.verticalGradient(listOf(CARD, Color(0xFF11151C)))).border(1.dp, BORDER, shape).padding(horizontal = 24.dp, vertical = 38.dp).testTag("quiz-results"), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("RESULTADO", fontFamily = Inter, fontSize = 11.sp, letterSpacing = 1.6.sp, color = GOLD)
                            Spacer(Modifier.height(10.dp))
                            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(score.toString(), fontFamily = Playfair, fontWeight = FontWeight.SemiBold, fontSize = 72.sp, lineHeight = 72.sp, color = GOLD_LIGHT)
                                Text("/", fontFamily = Inter, fontSize = 24.sp, color = Color(0x66FFFFFF), modifier = Modifier.padding(bottom = 10.dp))
                                Text(total.toString(), fontFamily = Inter, fontSize = 24.sp, color = Color(0xB3FFFFFF), modifier = Modifier.padding(bottom = 10.dp))
                            }
                            Spacer(Modifier.height(16.dp))
                            Box(Modifier.fillMaxWidth().height(6.dp).clip(CircleShape).background(Color(0x1AFFFFFF))) { Box(Modifier.fillMaxWidth(if (total == 0) 0f else score / total.toFloat()).height(6.dp).clip(CircleShape).background(GOLD_GRADIENT)) }
                            Spacer(Modifier.height(22.dp))
                            Text("Nivel ${tier.name}", fontFamily = Playfair, fontWeight = FontWeight.SemiBold, fontSize = 30.sp, color = Color.White)
                            Spacer(Modifier.height(10.dp))
                            Text(tier.desc, fontFamily = Inter, fontSize = 15.sp, lineHeight = 23.sp, color = Color(0xB3FFFFFF), textAlign = TextAlign.Center)
                            Spacer(Modifier.height(24.dp))
                            Box(Modifier.clip(RoundedCornerShape(10.dp)).background(GOLD_GRADIENT).pressable(scale = 0.97f, onClick = vm::reset).padding(horizontal = 32.dp, vertical = 15.dp).testTag("quiz-reset")) {
                                Text("Intentar de nuevo", fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = BG)
                            }
                            Spacer(Modifier.height(10.dp))
                            Box(Modifier.clip(RoundedCornerShape(10.dp)).border(1.dp, GOLD, RoundedCornerShape(10.dp)).pressable(scale = 0.97f) { go(Route.Escuela) }.padding(horizontal = 27.dp, vertical = 15.dp)) {
                                Text("Volver a la lección", fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = GOLD_LIGHT)
                            }
                        }
                    }
                }
                SiteFooter()
            }
        }
    }

    // confirm modal
    val p = st.pending
    if (p != null) {
        val q = pack.questions.firstOrNull { it.n == p.qN }
        val opt = q?.options?.firstOrNull { it.letter == p.letter }
        if (q != null && opt != null) Dialog(onDismissRequest = vm::cancel) {
            val shape = RoundedCornerShape(16.dp)
            Column(Modifier.clip(shape).background(Brush.verticalGradient(listOf(CARD, Color(0xFF11151C)))).border(1.dp, Color(0xFF2D343D), shape).padding(horizontal = 24.dp, vertical = 26.dp).testTag("quiz-confirm")) {
                Text("PREGUNTA ${q.n}", fontFamily = Inter, fontSize = 11.sp, letterSpacing = 1.4.sp, color = GOLD)
                Spacer(Modifier.height(8.dp))
                Text("¿Confirmar esta respuesta?", fontFamily = Playfair, fontWeight = FontWeight.SemiBold, fontSize = 24.sp, lineHeight = 29.sp, color = Color.White)
                Spacer(Modifier.height(8.dp))
                Text("Una vez confirmada no podrás cambiarla, ni siquiera al recargar la página.", fontFamily = Inter, fontSize = 14.5.sp, lineHeight = 22.sp, color = Color(0xB3FFFFFF))
                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(Color(0x1AC9963B)).border(1.dp, GOLD, RoundedCornerShape(10.dp)).padding(14.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("${opt.letter}.", fontFamily = Inter, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = GOLD_LIGHT)
                    Text(opt.text, fontFamily = Inter, fontSize = 15.sp, lineHeight = 21.sp, color = TEXT)
                }
                Spacer(Modifier.height(18.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(Modifier.weight(1f).clip(RoundedCornerShape(10.dp)).border(1.dp, Color(0xFF2D343D), RoundedCornerShape(10.dp)).pressable(scale = 0.97f, onClick = vm::cancel).padding(vertical = 13.dp).testTag("confirm-no"), contentAlignment = Alignment.Center) {
                        Text("Cancelar", fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = Color(0xC7FFFFFF))
                    }
                    Box(Modifier.weight(1f).clip(RoundedCornerShape(10.dp)).background(GOLD_GRADIENT).pressable(scale = 0.97f, onClick = vm::confirm).padding(vertical = 13.dp).testTag("confirm-yes"), contentAlignment = Alignment.Center) {
                        Text("Sí, confirmar", fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = BG)
                    }
                }
            }
        }
    }
}
