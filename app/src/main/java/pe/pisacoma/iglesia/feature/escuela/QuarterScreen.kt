package pe.pisacoma.iglesia.feature.escuela

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
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import pe.pisacoma.iglesia.App
import pe.pisacoma.iglesia.R
import pe.pisacoma.iglesia.nav.Route
import pe.pisacoma.iglesia.ui.chrome.SiteFooter
import pe.pisacoma.iglesia.ui.components.EyebrowLine
import pe.pisacoma.iglesia.ui.components.Lede
import pe.pisacoma.iglesia.ui.components.Ochre
import pe.pisacoma.iglesia.ui.components.PageTitle
import pe.pisacoma.iglesia.ui.components.pressable
import pe.pisacoma.iglesia.ui.components.titleWithEm
import pe.pisacoma.iglesia.ui.theme.Fraunces
import pe.pisacoma.iglesia.ui.theme.Inter
import pe.pisacoma.iglesia.ui.theme.Tokens

/** The site's /escuela-sabatica: hero with meta + progress, «Nuevo trimestre» when ended, the week's lesson card, all 13 lessons. */
@Composable
fun QuarterScreen(modifier: Modifier, go: (Route) -> Unit) {
    val vm: QuarterViewModel = viewModel { QuarterViewModel(App.repos.quarterly, App.repos.userState) }
    val s by vm.state.collectAsState()
    LaunchedEffect(Unit) { vm.refresh() }
    val q = s.quarterly ?: return
    val cur = q.lessons[s.currentIdx]
    val accent = Color(LESSON_ACCENTS[s.currentIdx % LESSON_ACCENTS.size])
    val words = q.title.split(" ")

    LazyColumn(modifier.testTag("lesson-list")) {
        item {
            Column(Modifier.padding(horizontal = 20.dp)) {
                Spacer(Modifier.height(44.dp))
                EyebrowLine("— Escuela Sabática · ${q.humanDate}")
                Spacer(Modifier.height(18.dp))
                PageTitle(titleWithEm(words.take(2).joinToString(" "), words.drop(2).joinToString(" ")))
                Spacer(Modifier.height(22.dp))
                Lede(q.description.take(240) + "…")
                Spacer(Modifier.height(28.dp))
                // .meta
                Column {
                    DashedRule()
                    Row(Modifier.padding(vertical = 18.dp), horizontalArrangement = Arrangement.spacedBy(18.dp), verticalAlignment = Alignment.Bottom) {
                        MetaItem(q.lessons.size.toString(), "lecciones")
                        MetaSep()
                        MetaItem((q.lessons.size * 7).toString(), "días")
                        MetaSep()
                        MetaItem(q.startDate, "— ${q.endDate}", small = true)
                    }
                    DashedRule()
                }
                Spacer(Modifier.height(24.dp))
                // .progress
                Box(Modifier.fillMaxWidth().height(4.dp).clip(CircleShape).background(Tokens.sageA(0.14f))) {
                    Box(Modifier.fillMaxWidth((s.currentIdx + 1f) / q.lessons.size).height(4.dp).clip(CircleShape).background(accent))
                }
                Spacer(Modifier.height(12.dp))
                EyebrowLine(
                    if (s.ended) "Trimestre completado · ${q.lessons.size} lecciones" else "Vamos por la lección ${s.currentIdx + 1} de ${q.lessons.size}",
                    Color(0xFF57654A), size = 11.5f, tracking = 1.6f,
                )
                if (s.ended) {
                    Spacer(Modifier.height(34.dp))
                    val shape = RoundedCornerShape(16.dp)
                    Column(Modifier.fillMaxWidth().clip(shape).background(Color(0xB3FBF9EF)).border(1.dp, Color(0x80B8924F), shape).padding(24.dp, 22.dp)) {
                        EyebrowLine("— Nuevo trimestre", Color(0xFF8A6A32), size = 11f, tracking = 2.2f)
                        Spacer(Modifier.height(8.dp))
                        PageTitle(titleWithEm("Empieza el", s.nextStartLabel), size = 24.sp)
                        Spacer(Modifier.height(6.dp))
                        Text("Las lecciones aparecerán aquí en cuanto se publiquen. Mientras tanto, puedes repasar las ${q.lessons.size} lecciones de «${q.title}».", fontFamily = Inter, fontSize = 15.sp, lineHeight = 23.sp, color = Color(0xFF4D6044))
                    }
                }
                Spacer(Modifier.height(38.dp))
                // .current
                EyebrowLine(if (s.ended) "— Última lección del trimestre" else "— Lección de esta semana", Ochre.label)
                Spacer(Modifier.height(14.dp))
                val cardShape = RoundedCornerShape(24.dp)
                Column(
                    Modifier.fillMaxWidth().clip(cardShape)
                        .background(Brush.linearGradient(listOf(Color(0xFFFBF9EF), Color(0xFFECE9D6))))
                        .border(1.dp, Tokens.sageA(0.20f), cardShape)
                        .pressable(scale = 0.99f) { go(Route.Leccion(cur.number)) }
                        .padding(horizontal = 24.dp, vertical = 26.dp)
                        .testTag("current-lesson"),
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                ) {
                    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        EyebrowLine("Lec.", Tokens.sageText, size = 10f, tracking = 2f)
                        Text(cur.number.toString(), fontFamily = Fraunces, fontStyle = FontStyle.Italic, fontWeight = FontWeight.Bold, fontSize = 51.sp, lineHeight = 44.sp, letterSpacing = (-0.04 * 51).sp, color = accent)
                    }
                    DashedRule()
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(cur.title, fontFamily = Fraunces, fontWeight = FontWeight.SemiBold, fontSize = 24.sp, lineHeight = 26.sp, letterSpacing = (-0.022 * 24).sp, color = Tokens.inkDeep)
                        EyebrowLine("${cur.startDate} — ${cur.endDate} · 7 días de estudio", Tokens.sageText, size = 11f, tracking = 1.8f)
                        Row(Modifier.padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Empezar a estudiar", fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = accent)
                            Icon(painterResource(R.drawable.ic_nav_arrow), null, tint = accent, modifier = Modifier.size(16.dp))
                        }
                        Box(Modifier.width(150.dp).height(1.dp).background(accent))
                    }
                }
                Spacer(Modifier.height(50.dp))
                EyebrowLine("— Todas las lecciones del trimestre")
                Spacer(Modifier.height(18.dp))
            }
        }
        items(q.lessons.size, key = { q.lessons[it].number }) { i ->
            val l = q.lessons[i]
            val c = Color(LESSON_ACCENTS[i % LESSON_ACCENTS.size])
            val isCur = i == s.currentIdx
            val isPast = i < s.currentIdx
            val shape = RoundedCornerShape(14.dp)
            val done = s.progress[l.number] ?: 0
            Row(
                Modifier.padding(horizontal = 20.dp).padding(bottom = 12.dp).fillMaxWidth().clip(shape)
                    .background(Color(0x8CFFFFFF)).border(1.dp, Tokens.sageA(0.16f), shape)
                    .pressable(scale = 0.99f) { go(Route.Leccion(l.number)) }
                    .padding(horizontal = 18.dp, vertical = 16.dp)
                    .testTag("lesson-${l.number}"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Box(
                    Modifier.size(56.dp).clip(RoundedCornerShape(14.dp))
                        .background(if (isCur) c else if (isPast) Color(0x1F6B7D5B) else Tokens.sageA(0.10f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(l.number.toString().padStart(2, '0'), fontFamily = Fraunces, fontStyle = FontStyle.Italic, fontWeight = FontWeight.Bold, fontSize = 22.4.sp, letterSpacing = (-0.03 * 22.4).sp, color = if (isCur) Tokens.cream else if (isPast) Tokens.sageText else c)
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    EyebrowLine(l.startDate, Tokens.sageText, size = 10.5f, tracking = 1.8f)
                    Text(l.title, fontFamily = Fraunces, fontWeight = if (isCur) FontWeight.SemiBold else FontWeight.Medium, fontSize = 16.sp, lineHeight = 19.sp, color = if (isCur) Tokens.inkDeep else Tokens.ink)
                    // 7 progress dots (completed days), as the lesson page's progress tab counts them
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(top = 2.dp)) {
                        repeat(l.days.size) { d ->
                            Box(Modifier.size(6.dp).clip(CircleShape).background(if (d < done) c else Tokens.sageA(0.18f)).testTag(if (d < done) "lesson-${l.number}-dots-done-${d + 1}" else "lesson-${l.number}-dot-$d"))
                        }
                    }
                }
                if (isCur) Text("→", fontFamily = Inter, fontSize = 16.sp, color = c)
            }
        }
        item { Spacer(Modifier.height(40.dp)); SiteFooter() }
    }
}

@Composable
private fun MetaItem(num: String, label: String, small: Boolean = false) {
    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(num, fontFamily = Fraunces, fontStyle = if (small) FontStyle.Normal else FontStyle.Italic, fontWeight = FontWeight.Bold, fontSize = if (small) 16.sp else 25.6.sp, lineHeight = if (small) 18.sp else 26.sp, letterSpacing = (-0.3).sp, color = Tokens.inkDeep)
        EyebrowLine(label, Tokens.sageText, size = 11f, tracking = 1.6f)
    }
}

@Composable
private fun MetaSep() = Box(Modifier.width(1.dp).height(28.dp).background(Tokens.sageA(0.2f)))

/** `border: 1px dashed rgba(74,103,65,.22)` */
@Composable
fun DashedRule() {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        repeat(80) { Box(Modifier.weight(1f).height(1.dp).background(Tokens.sageA(0.22f))) }
    }
}
