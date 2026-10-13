package pe.pisacoma.iglesia.feature.visitanos

import android.content.Intent
import android.net.Uri
import android.provider.CalendarContract
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pe.pisacoma.iglesia.App
import pe.pisacoma.iglesia.data.model.Church
import pe.pisacoma.iglesia.nav.Route
import pe.pisacoma.iglesia.ui.chrome.SiteFooter
import pe.pisacoma.iglesia.ui.components.EyebrowLine
import pe.pisacoma.iglesia.ui.components.Hairline
import pe.pisacoma.iglesia.ui.components.Lede
import pe.pisacoma.iglesia.ui.components.Ochre
import pe.pisacoma.iglesia.ui.components.OrnamentRule
import pe.pisacoma.iglesia.ui.components.PageTitle
import pe.pisacoma.iglesia.ui.components.pressable
import pe.pisacoma.iglesia.ui.components.titleWithEm
import pe.pisacoma.iglesia.ui.theme.Fraunces
import pe.pisacoma.iglesia.ui.theme.Inter
import pe.pisacoma.iglesia.ui.theme.Tokens
import pe.pisacoma.iglesia.util.Zmanim
import kotlin.math.abs

private const val SITE = "https://quiz-qwik.vercel.app"

// site visitanos/index.tsx TIPS
private val TIPS = listOf(
    "Llega unos minutos antes" to "La Escuela Sabática empieza a las 9:30. Con 10 o 15 minutos de margen hay tiempo para saludar y encontrar sitio.",
    "Abrígate bien" to "A 3.940 metros las mañanas son frías todo el año, y en invierno la temperatura puede bajar de cero.",
    "Ven como eres" to "No hace falta ser miembro ni conocer a nadie para visitarnos: todos son bienvenidos.",
)

/** The site's /visitanos: horarios + sunset + calendar, dónde estamos, tu primer sábado, nuestra historia, a qué pertenecemos. */
@Composable
fun VisitanosScreen(modifier: Modifier, go: (Route) -> Unit) {
    var church by remember { mutableStateOf<Church?>(null) }
    LaunchedEffect(Unit) { church = App.repos.church.church() }
    val c = church ?: return
    val ctx = LocalContext.current
    val now = System.currentTimeMillis()
    val sab = remember(now / 60_000) { Zmanim.sabbathTimes(now) }
    val start = Zmanim.clock12(sab.start)
    val end = Zmanim.clock12(sab.end)
    val mapsUrl = "https://www.google.com/maps/search/?api=1&query=${c.location.lat},${c.location.lng}"
    val osmUrl = "https://www.openstreetmap.org/?mlat=${c.location.lat}&mlon=${c.location.lng}#map=14/${c.location.lat}/${c.location.lng}"
    fun openUrl(u: String) = ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(u)))

    Column(modifier.verticalScroll(rememberScrollState())) {
        Column(Modifier.padding(horizontal = 20.dp)) {
            Spacer(Modifier.height(44.dp))
            EyebrowLine("— Visítanos · Pisacoma, Chucuito · Puno")
            Spacer(Modifier.height(18.dp))
            PageTitle(titleWithEm("Te esperamos", "este sábado."))
            Spacer(Modifier.height(22.dp))
            Lede("Una iglesia adventista a 3.940 metros, en el Altiplano. Aquí tienes los horarios, dónde estamos y lo que conviene saber antes de tu primera visita.")
            Spacer(Modifier.height(36.dp))

            // ── Horarios
            BlockHead("— Horarios")
            c.schedule.forEachIndexed { i, s ->
                RowItem(first = i == 0, num = { Row(verticalAlignment = Alignment.Bottom) { Numeral(s.time); Text(s.suffix, fontFamily = Fraunces, fontStyle = FontStyle.Italic, fontSize = 12.sp, color = Ochre.label, modifier = Modifier.padding(start = 3.dp, bottom = 2.dp)) } }, title = s.name, caption = s.day)
            }
            Spacer(Modifier.height(18.dp))
            val solShape = RoundedCornerShape(14.dp)
            Column(Modifier.fillMaxWidth().clip(solShape).background(Color(0xB3FBF9EF)).border(1.dp, Color(0x59B8924F), solShape).padding(18.dp).testTag("sol")) {
                EyebrowLine("Puesta del sol esta semana · Pisacoma", Ochre.label, size = 10.5f)
                Spacer(Modifier.height(10.dp))
                SolLine(start.time, start.suffix, "${Zmanim.dayLabel(sab.friday)} · empieza el sábado")
                Spacer(Modifier.height(6.dp))
                SolLine(end.time, end.suffix, "${Zmanim.dayLabel(sab.saturday)} · termina el sábado")
            }
            Spacer(Modifier.height(18.dp))
            Text("Recibe los horarios y la puesta del sol de cada sábado en el calendario de tu teléfono, con un aviso antes de cada reunión. Se actualiza solo.", fontFamily = Inter, fontSize = 14.5.sp, lineHeight = 22.sp, color = Color(0xFF4D6044))
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Pill("Añadir al calendario", testTag = "add-calendar") {
                    val next = sab.saturday
                    val cal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("America/Lima")).apply { timeInMillis = next; set(java.util.Calendar.HOUR_OF_DAY, 9); set(java.util.Calendar.MINUTE, 30) }
                    ctx.startActivity(
                        Intent(Intent.ACTION_INSERT).setData(CalendarContract.Events.CONTENT_URI)
                            .putExtra(CalendarContract.Events.TITLE, "Escuela Sabática · Iglesia Adventista de Pisacoma")
                            .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, cal.timeInMillis)
                            .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, cal.timeInMillis + 3 * 3_600_000L)
                            .putExtra(CalendarContract.Events.EVENT_LOCATION, "Pisacoma, Chucuito, Puno")
                            .putExtra(CalendarContract.Events.RRULE, "FREQ=WEEKLY;BYDAY=SA"),
                    )
                }
                Pill("Suscribirse (.ics)", ghost = true) { openUrl("$SITE/calendario.ics") }
            }
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("¿Quieres llegar preparado? Lee", fontFamily = Inter, fontSize = 14.sp, color = Color(0xFF4D6044))
                Text("la lección de esta semana", fontFamily = Inter, fontSize = 14.sp, color = Tokens.sage, fontWeight = FontWeight.SemiBold, modifier = Modifier.pressable(scale = 0.98f) { go(Route.Escuela) })
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("o busca los himnos en", fontFamily = Inter, fontSize = 14.sp, color = Color(0xFF4D6044))
                Text("el himnario.", fontFamily = Inter, fontSize = 14.sp, color = Tokens.sage, fontWeight = FontWeight.SemiBold, modifier = Modifier.pressable(scale = 0.98f) { go(Route.Himnario) })
            }
            Spacer(Modifier.height(40.dp))

            // ── Dónde estamos
            BlockHead("— Dónde estamos")
            Text("Distrito de ${c.location.district}, provincia de ${c.location.province}, región ${c.location.region}, junto a la frontera con Bolivia.", fontFamily = Fraunces, fontSize = 18.sp, lineHeight = 27.sp, color = Tokens.inkDeep)
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(28.dp)) {
                Fact("Altitud", c.location.altitude)
                Fact("Coordenadas", "${"%.4f".format(abs(c.location.lat))}° S · ${"%.4f".format(abs(c.location.lng))}° O")
            }
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Pill("Abrir en Google Maps ↗", testTag = "open-maps") { openUrl(mapsUrl) }
                Pill("OpenStreetMap ↗", ghost = true) { openUrl(osmUrl) }
            }
            Spacer(Modifier.height(10.dp))
            Text("Las coordenadas marcan el centro de Pisacoma.", fontFamily = Fraunces, fontStyle = FontStyle.Italic, fontSize = 13.sp, color = Tokens.sageText)
            Spacer(Modifier.height(18.dp))
            // compass figure
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.width(180.dp).height(180.dp)) {
                    androidx.compose.foundation.Canvas(Modifier.fillMaxWidth().height(180.dp)) {
                        val r = size.minDimension / 2 - 14.dp.toPx()
                        drawCircle(Tokens.sageA(0.25f), r, center, style = androidx.compose.ui.graphics.drawscope.Stroke(1.dp.toPx()))
                        drawCircle(Tokens.sageA(0.12f), r * 0.6f, center, style = androidx.compose.ui.graphics.drawscope.Stroke(1.dp.toPx()))
                        drawLine(Tokens.sageA(0.3f), androidx.compose.ui.geometry.Offset(center.x, center.y - r), androidx.compose.ui.geometry.Offset(center.x, center.y + r), 1.dp.toPx())
                        drawLine(Tokens.sageA(0.3f), androidx.compose.ui.geometry.Offset(center.x - r, center.y), androidx.compose.ui.geometry.Offset(center.x + r, center.y), 1.dp.toPx())
                        drawCircle(Ochre.numeral, 6.dp.toPx(), center)
                    }
                    Text("N", fontFamily = Inter, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Tokens.sageText, modifier = Modifier.align(Alignment.TopCenter))
                    Text("S", fontFamily = Inter, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Tokens.sageText, modifier = Modifier.align(Alignment.BottomCenter))
                    Text("E", fontFamily = Inter, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Tokens.sageText, modifier = Modifier.align(Alignment.CenterEnd))
                    Text("O", fontFamily = Inter, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Tokens.sageText, modifier = Modifier.align(Alignment.CenterStart))
                }
                Text("Pisacoma · ${c.location.altitude}", fontFamily = Fraunces, fontStyle = FontStyle.Italic, fontSize = 12.5.sp, color = Tokens.sageText)
            }
            Spacer(Modifier.height(40.dp))

            // ── Tu primer sábado
            BlockHead("— Tu primer sábado")
            TIPS.forEachIndexed { i, (t, text) -> RowItem(first = i == 0, num = { Numeral((i + 1).toString().padStart(2, '0')) }, title = t, caption = text, prose = true) }
            Spacer(Modifier.height(40.dp))

            // ── Nuestra historia
            BlockHead("— Nuestra historia")
            Text("Más de un siglo de fe adventista en el Altiplano. Pisacoma está en el origen de esa historia.", fontFamily = Inter, fontSize = 15.sp, lineHeight = 23.sp, color = Color(0xFF4D6044))
            Spacer(Modifier.height(14.dp))
            c.timeline.forEachIndexed { i, t -> RowItem(first = i == 0, num = { Numeral(t.year, small = t.year.length > 4) }, title = null, caption = t.text, prose = true, dark = true) }
            Spacer(Modifier.height(22.dp))
            c.pioneers.forEach { p ->
                val shape = RoundedCornerShape(14.dp)
                Column(Modifier.fillMaxWidth().padding(bottom = 12.dp).clip(shape).background(Color(0x8CFFFFFF)).border(1.dp, Tokens.sageA(0.16f), shape).padding(18.dp)) {
                    EyebrowLine(p.role, Ochre.label, size = 10.5f)
                    Spacer(Modifier.height(6.dp))
                    Text(p.name, fontFamily = Fraunces, fontWeight = FontWeight.SemiBold, fontSize = 19.sp, color = Tokens.inkDeep)
                    Spacer(Modifier.height(6.dp))
                    Text(p.bio, fontFamily = Inter, fontSize = 14.5.sp, lineHeight = 22.sp, color = Color(0xFF4D6044))
                }
            }
            Spacer(Modifier.height(28.dp))

            // ── A qué pertenecemos
            BlockHead("— A qué pertenecemos")
            c.hierarchy.forEachIndexed { i, h ->
                Row(Modifier.padding(bottom = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.width(10.dp).height(10.dp).clip(CircleShape).background(if (i == 0) Tokens.sage else Tokens.sageA(0.3f)))
                    Column {
                        Text(h.name, fontFamily = Fraunces, fontWeight = if (i == 0) FontWeight.SemiBold else FontWeight.Medium, fontSize = 16.sp, color = if (i == 0) Tokens.inkDeep else Tokens.ink)
                        if (h.place.isNotEmpty()) EyebrowLine(h.place, Tokens.sageText, size = 10f, tracking = 1.4f)
                    }
                }
            }
            Spacer(Modifier.height(14.dp))
            Text("${c.mission.name} · ${c.mission.address}, ${c.mission.city}", fontFamily = Fraunces, fontStyle = FontStyle.Italic, fontSize = 13.sp, lineHeight = 20.sp, color = Tokens.sageText)
            Spacer(Modifier.height(60.dp))
        }
        SiteFooter()
    }
}

@Composable
private fun BlockHead(label: String) {
    Row(Modifier.fillMaxWidth().padding(bottom = 16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        EyebrowLine(label, Tokens.sage, size = 11f)
        Box(Modifier.weight(1f)) { OrnamentRule() }
    }
}

@Composable
private fun Numeral(text: String, small: Boolean = false) {
    Text(text, fontFamily = Fraunces, fontStyle = FontStyle.Italic, fontWeight = FontWeight.Bold, fontSize = if (small) 15.sp else 22.sp, letterSpacing = (-0.03 * 22).sp, color = Ochre.numeral, textAlign = TextAlign.End)
}

@Composable
private fun RowItem(first: Boolean, num: @Composable () -> Unit, title: String?, caption: String, prose: Boolean = false, dark: Boolean = false) {
    if (!first) Hairline(0.10f)
    Row(Modifier.fillMaxWidth().padding(vertical = 13.dp), verticalAlignment = if (prose) Alignment.Top else Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Box(Modifier.width(62.dp), contentAlignment = Alignment.CenterEnd) { num() }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            if (title != null) Text(title, fontFamily = Fraunces, fontWeight = FontWeight.Medium, fontSize = 16.5.sp, lineHeight = 20.sp, color = Tokens.ink)
            Text(caption, fontFamily = if (prose) Inter else Fraunces, fontStyle = if (prose) FontStyle.Normal else FontStyle.Italic, fontSize = if (prose) 14.sp else 12.sp, lineHeight = if (prose) 21.sp else 16.sp, color = if (dark) Tokens.ink else Color(0xFF657158))
        }
    }
}

@Composable
private fun SolLine(time: String, suffix: String, label: String) {
    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(time, fontFamily = Fraunces, fontStyle = FontStyle.Italic, fontWeight = FontWeight.Bold, fontSize = 22.sp, color = Tokens.inkDeep)
            Text(suffix, fontFamily = Fraunces, fontSize = 12.sp, color = Ochre.label, modifier = Modifier.padding(start = 3.dp, bottom = 3.dp))
        }
        Text(label, fontFamily = Inter, fontSize = 13.sp, color = Tokens.sageText, modifier = Modifier.padding(bottom = 3.dp))
    }
}

@Composable
private fun Fact(label: String, value: String) {
    Column {
        EyebrowLine(label, Tokens.sageText, size = 10f, tracking = 1.6f)
        Text(value, fontFamily = Fraunces, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Tokens.inkDeep)
    }
}

@Composable
private fun Pill(label: String, ghost: Boolean = false, testTag: String = "", onClick: () -> Unit) {
    Box(
        Modifier.clip(CircleShape).background(if (ghost) Color.Transparent else Tokens.forest).border(1.dp, if (ghost) Tokens.sageA(0.3f) else Tokens.forest, CircleShape)
            .pressable(scale = 0.97f, onClick = onClick).padding(horizontal = 16.dp, vertical = 10.dp).testTag(testTag),
    ) { Text(label, fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = if (ghost) Tokens.forest else Tokens.cream) }
}
