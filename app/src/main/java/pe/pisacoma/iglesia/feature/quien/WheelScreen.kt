package pe.pisacoma.iglesia.feature.quien

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import pe.pisacoma.iglesia.nav.Route
import pe.pisacoma.iglesia.ui.chrome.SiteFooter
import pe.pisacoma.iglesia.ui.components.pressable
import pe.pisacoma.iglesia.ui.theme.Inter
import pe.pisacoma.iglesia.ui.theme.Playfair
import pe.pisacoma.iglesia.ui.theme.Tokens
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

// site quien/index.tsx constants
private val SEGMENT_COLORS = listOf(Color(0xFF1E3A5F), Color(0xFFC9963B), Color(0xFF2D5A8E), Color(0xFF0D1117), Color(0xFF5C2B1E), Color(0xFF1B5E3B))
private val DEFAULT_NAMES = listOf("Hermano 1", "Hermana 1", "Hermano 2", "Hermana 2", "Hermano 3", "Hermana 3", "Hermano 4", "Hermana 4")
private const val SPIN_MS = 4500
private const val TURNS = 6
private val BG = Tokens.bg
private val GOLD = Tokens.accent
private val GOLD_LIGHT = Tokens.accentLight

/** The site's /quien: name wheel («Ruleta») and the table pointer («Apuntador»), dark theme. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WheelScreen(modifier: Modifier, go: (Route) -> Unit) {
    val names = remember { mutableStateListOf<String>().apply { addAll(DEFAULT_NAMES) } }
    var input by rememberSaveable { mutableStateOf("") }
    var mode by rememberSaveable { mutableStateOf("wheel") }
    val angle = remember { Animatable(0f) }
    var spinning by remember { mutableStateOf(false) }
    var winner by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    fun addNames() {
        val parts = input.split(Regex("[,\\n]")).map { it.trim() }.filter { it.isNotEmpty() }
        if (parts.isNotEmpty()) { names.addAll(parts); input = "" }
    }

    fun spin() {
        val n = names.size
        if (n < 2 || spinning) return
        winner = null
        val seg = 360f / n
        val idx = Random.nextInt(n)
        val targetMod = (((-(idx * seg + seg / 2)) % 360f) + 360f) % 360f
        val currentMod = ((angle.value % 360f) + 360f) % 360f
        val delta = (targetMod - currentMod + 360f) % 360f
        spinning = true
        scope.launch {
            angle.animateTo(angle.value + TURNS * 360f + delta, tween(SPIN_MS, easing = CubicBezierEasing(0.17f, 0.67f, 0.32f, 1f)))
            spinning = false
            winner = names.getOrNull(idx)
        }
    }

    Column(modifier.background(BG).verticalScroll(rememberScrollState())) {
        Column(Modifier.padding(horizontal = 20.dp)) {
            Spacer(Modifier.height(40.dp))
            Text("DINÁMICA", fontFamily = Inter, fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 2.4.sp, color = GOLD)
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text("¿", fontFamily = Playfair, fontWeight = FontWeight.Bold, fontSize = 44.sp, color = Color.White)
                Text("Quién", fontFamily = Playfair, fontWeight = FontWeight.Bold, fontSize = 44.sp, color = GOLD_LIGHT)
                Text("?", fontFamily = Playfair, fontWeight = FontWeight.Bold, fontSize = 44.sp, color = Color.White)
            }
            Spacer(Modifier.height(12.dp))
            Text("Para Escuela Sabática, oraciones, lecturas y juegos. Elige al azar desde una lista o pon el celular en la mesa y deja que la flecha señale a alguien.", fontFamily = Inter, fontSize = 15.sp, lineHeight = 23.sp, color = Color(0xB3FFFFFF))
            Spacer(Modifier.height(22.dp))
            // tabs
            Row(Modifier.clip(RoundedCornerShape(12.dp)).background(Color(0xFF161B22)).border(1.dp, Tokens.border, RoundedCornerShape(12.dp)).padding(4.dp)) {
                for ((id, label) in listOf("wheel" to "Ruleta", "pointer" to "Apuntador")) {
                    val active = mode == id
                    Box(Modifier.weight(1f).clip(RoundedCornerShape(9.dp)).background(if (active) GOLD else Color.Transparent).pressable(scale = 0.98f) { mode = id }.padding(vertical = 10.dp).testTag("mode-$id"), contentAlignment = Alignment.Center) {
                        Text(label, fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = if (active) BG else Color(0xB3FFFFFF))
                    }
                }
            }
            Spacer(Modifier.height(26.dp))
            if (mode == "wheel") {
                // ───── wheel
                Box(Modifier.fillMaxWidth().aspectRatio(1f).testTag("wheel-segments-${names.size}"), contentAlignment = Alignment.TopCenter) {
                    Canvas(Modifier.fillMaxWidth().aspectRatio(1f).rotate(angle.value)) {
                        val c = center
                        val r = size.minDimension / 2 * 0.91f
                        drawCircle(BG, r + 5.dp.toPx(), c)
                        when {
                            names.isEmpty() -> drawCircle(Color(0xFF1F2630), r, c)
                            names.size == 1 -> drawCircle(SEGMENT_COLORS[0], r, c)
                            else -> {
                                val seg = 360f / names.size
                                names.forEachIndexed { i, _ ->
                                    val start = i * seg - 90f
                                    drawArc(SEGMENT_COLORS[i % SEGMENT_COLORS.size], start, seg, true, Offset(c.x - r, c.y - r), Size(2 * r, 2 * r))
                                    drawArc(Color(0xD9FFFFFF), start, seg, true, Offset(c.x - r, c.y - r), Size(2 * r, 2 * r), style = Stroke(1.5.dp.toPx()))
                                }
                            }
                        }
                        drawCircle(GOLD, 26.dp.toPx() * 0.6f, c)
                        drawCircle(BG, 8.dp.toPx() * 0.6f, c)
                        // labels (native text, rotated into each segment like the site)
                        val paint = android.graphics.Paint().apply { color = android.graphics.Color.WHITE; isAntiAlias = true; textAlign = android.graphics.Paint.Align.CENTER; isFakeBoldText = true }
                        val n = names.size
                        if (n == 1) {
                            paint.textSize = 22.sp.toPx() * 0.75f
                            drawContext.canvas.nativeCanvas.drawText(names[0], c.x, c.y + paint.textSize / 3, paint)
                        } else if (n >= 2) {
                            val seg = 360f / n
                            val fs = maxOf(9f, minOf(20f, 220f / n))
                            paint.textSize = fs.sp.toPx() * 0.75f
                            val maxChars = maxOf(6, (34 / Math.sqrt(n.toDouble())).toInt())
                            names.forEachIndexed { i, name ->
                                val mid = i * seg - 90f + seg / 2
                                val mr = mid * PI.toFloat() / 180f
                                val tx = c.x + r * 0.62f * cos(mr)
                                val ty = c.y + r * 0.62f * sin(mr)
                                val display = if (name.length > maxChars) name.take(maxChars - 1) + "…" else name
                                rotate(mid + 90f, Offset(tx, ty)) { drawContext.canvas.nativeCanvas.drawText(display, tx, ty + paint.textSize / 3, paint) }
                            }
                        } else {
                            paint.color = android.graphics.Color.argb(115, 255, 255, 255); paint.textSize = 20.sp.toPx() * 0.75f
                            drawContext.canvas.nativeCanvas.drawText("Agrega nombres", c.x, c.y + paint.textSize / 3, paint)
                        }
                    }
                    // external pointer at the top
                    Canvas(Modifier.size(44.dp, 52.dp)) {
                        val p = Path().apply { moveTo(size.width / 2, size.height); lineTo(4.dp.toPx(), 4.dp.toPx()); lineTo(size.width - 4.dp.toPx(), 4.dp.toPx()); close() }
                        drawPath(p, GOLD_LIGHT); drawPath(p, BG, style = Stroke(2.dp.toPx()))
                    }
                    if (names.isEmpty()) Text("Agrega nombres", color = Color.Transparent, fontSize = 1.sp, modifier = Modifier.testTag("wheel-empty"))
                    if (names.size == 1) Text(names[0], color = Color.Transparent, fontSize = 1.sp, modifier = Modifier.testTag("wheel-single"))
                }
                Spacer(Modifier.height(18.dp))
                Box(
                    Modifier.fillMaxWidth().alpha(if (spinning || names.size < 2) 0.5f else 1f).clip(RoundedCornerShape(12.dp)).background(Brush.linearGradient(listOf(GOLD, GOLD_LIGHT)))
                        .pressable(scale = 0.97f, enabled = !spinning && names.size >= 2) { spin() }.padding(vertical = 16.dp).testTag("wheel-spin"),
                    contentAlignment = Alignment.Center,
                ) { Text(if (spinning) "Girando..." else "GIRAR", fontFamily = Inter, fontWeight = FontWeight.Bold, fontSize = 17.sp, letterSpacing = 2.sp, color = BG) }
                val w = winner
                if (w != null && !spinning) {
                    Spacer(Modifier.height(18.dp))
                    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Color(0xFF161B22)).border(1.dp, GOLD, RoundedCornerShape(14.dp)).padding(18.dp).testTag("wheel-winner"), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("LE TOCA A", fontFamily = Inter, fontSize = 11.sp, letterSpacing = 2.sp, color = GOLD)
                        Text(w, fontFamily = Playfair, fontWeight = FontWeight.Bold, fontSize = 30.sp, color = Color.White, textAlign = TextAlign.Center)
                    }
                }
                Spacer(Modifier.height(30.dp))
                // side: participants
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("Participantes ", fontFamily = Playfair, fontWeight = FontWeight.SemiBold, fontSize = 20.sp, color = Color.White)
                    Text("(${names.size})", fontFamily = Inter, fontSize = 14.sp, color = Color(0x80FFFFFF), modifier = Modifier.weight(1f))
                    if (names.isNotEmpty()) Text("Limpiar", fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Color(0xFFE65B5B), modifier = Modifier.pressable(scale = 0.97f) { names.clear(); winner = null }.testTag("wheel-clear"))
                }
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.weight(1f).clip(RoundedCornerShape(10.dp)).background(Color(0xFF161B22)).border(1.dp, Tokens.border, RoundedCornerShape(10.dp)).padding(horizontal = 14.dp, vertical = 12.dp)) {
                        if (input.isEmpty()) Text("Nombre o lista (separados por coma)", fontFamily = Inter, fontSize = 14.sp, color = Color(0x66FFFFFF))
                        BasicTextField(input, { input = it }, textStyle = TextStyle(fontFamily = Inter, fontSize = 14.sp, color = Color.White), cursorBrush = SolidColor(GOLD), singleLine = true, keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done), keyboardActions = KeyboardActions(onDone = { addNames() }), modifier = Modifier.fillMaxWidth().testTag("wheel-input"))
                    }
                    Box(Modifier.size(46.dp).alpha(if (input.isBlank()) 0.5f else 1f).clip(RoundedCornerShape(10.dp)).background(GOLD).pressable(scale = 0.95f, enabled = input.isNotBlank()) { addNames() }.testTag("wheel-add"), contentAlignment = Alignment.Center) {
                        Text("+", fontFamily = Inter, fontWeight = FontWeight.Bold, fontSize = 22.sp, color = BG)
                    }
                }
                Spacer(Modifier.height(12.dp))
                if (names.isEmpty()) Text("Agrega al menos 2 nombres para girar la ruleta.", fontFamily = Inter, fontSize = 13.sp, color = Color(0x80FFFFFF))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    names.forEachIndexed { i, name ->
                        Row(Modifier.clip(CircleShape).background(Color(0xFF161B22)).border(1.dp, Tokens.border, CircleShape).padding(start = 10.dp, end = 6.dp, top = 6.dp, bottom = 6.dp).testTag("wheel-name-$name"), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(Modifier.size(8.dp).clip(CircleShape).background(SEGMENT_COLORS[i % SEGMENT_COLORS.size]))
                            Text(name, fontFamily = Inter, fontSize = 13.5.sp, color = Color.White)
                            Box(Modifier.size(22.dp).clip(CircleShape).pressable { names.removeAt(i) }.testTag("wheel-remove-$name"), contentAlignment = Alignment.Center) { Text("×", fontFamily = Inter, fontSize = 15.sp, color = Color(0x99FFFFFF)) }
                        }
                    }
                }
                Spacer(Modifier.height(14.dp))
                Text("Tip · pega una lista separada por comas o saltos de línea y presiona Enter.", fontFamily = Inter, fontSize = 12.5.sp, lineHeight = 19.sp, color = Color(0x80FFFFFF))
            } else {
                PointerMode()
            }
            Spacer(Modifier.height(40.dp))
        }
        SiteFooter()
    }
}

/** «Apuntador»: a needle that spins on the dial and stops pointing at someone around the table (site pointer mode, same friction model). */
@Composable
private fun PointerMode() {
    val angle = remember { Animatable(0f) }
    var spinning by remember { mutableStateOf(false) }
    var settled by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    fun spin() {
        if (spinning) return
        spinning = true; settled = false
        scope.launch {
            // ω0 1200–1700 °/s with friction e^(−0.42·t) until 18 °/s, as the site integrates it
            var omega = 1200f + Random.nextFloat() * 500f
            var a = angle.value
            val dt = 1f / 60f
            while (omega > 18f) { a += omega * dt; omega *= exp(-0.42f * dt) - 0.0035f * dt * 60f; angle.snapTo(a); kotlinx.coroutines.delay(16) }
            spinning = false; settled = true
        }
    }
    Box(Modifier.fillMaxWidth().aspectRatio(1f), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxWidth().aspectRatio(1f)) {
            val c = center; val r = size.minDimension / 2 * 0.91f
            drawCircle(BG, r + 8.dp.toPx(), c)
            drawCircle(GOLD.copy(alpha = 0.55f), r + 2.dp.toPx(), c, style = Stroke(1.5.dp.toPx()))
            drawCircle(Brush.radialGradient(listOf(Color(0xFF1C2530), Color(0xFF141A23), BG), c, r), r, c)
            drawCircle(Tokens.border, r, c, style = Stroke(1.dp.toPx()))
            for (i in 0 until 24) { val t = i * 15f * PI.toFloat() / 180f; drawCircle(GOLD.copy(alpha = 0.7f), 3.dp.toPx(), Offset(c.x + (r - 10.dp.toPx()) * cos(t), c.y + (r - 10.dp.toPx()) * sin(t))) }
            rotate(angle.value, c) {
                val shaft = Path().apply { moveTo(c.x - 9f, c.y - r * 0.55f); lineTo(c.x + 9f, c.y - r * 0.55f); lineTo(c.x + 10f, c.y - 8f); lineTo(c.x - 10f, c.y - 8f); close() }
                drawPath(shaft, Brush.horizontalGradient(listOf(Color(0xFF7A5818), Color(0xFFF0C870), Color(0xFF7A5818))))
                val head = Path().apply { moveTo(c.x - 13f, c.y - r * 0.55f); lineTo(c.x, c.y - r * 0.95f); lineTo(c.x + 13f, c.y - r * 0.55f); close() }
                drawPath(head, Brush.verticalGradient(listOf(Color(0xFFF4CD80), GOLD, Color(0xFF7A5818))))
                val tail = Path().apply { moveTo(c.x, c.y + 70f); lineTo(c.x - 10f, c.y + 35f); lineTo(c.x + 10f, c.y + 35f); close() }
                drawPath(tail, GOLD.copy(alpha = 0.85f))
            }
            drawCircle(GOLD, 14.dp.toPx(), c); drawCircle(BG, 5.dp.toPx(), c)
        }
    }
    Spacer(Modifier.height(18.dp))
    Box(Modifier.fillMaxWidth().alpha(if (spinning) 0.5f else 1f).clip(RoundedCornerShape(12.dp)).background(Brush.linearGradient(listOf(GOLD, GOLD_LIGHT))).pressable(scale = 0.97f, enabled = !spinning) { spin() }.padding(vertical = 16.dp).testTag("pointer-spin"), contentAlignment = Alignment.Center) {
        Text(if (spinning) "Girando..." else "GIRAR", fontFamily = Inter, fontWeight = FontWeight.Bold, fontSize = 17.sp, letterSpacing = 2.sp, color = BG)
    }
    Spacer(Modifier.height(12.dp))
    Text(if (spinning) "..." else if (settled) "🎯 Le toca a quien señale la flecha" else "Pon el celular plano en la mesa y toca GIRAR", fontFamily = Inter, fontSize = 14.sp, color = if (settled) GOLD_LIGHT else Color(0x99FFFFFF), textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
    Spacer(Modifier.height(30.dp))
    Text("Cómo usarla", fontFamily = Playfair, fontWeight = FontWeight.SemiBold, fontSize = 20.sp, color = Color.White)
    Spacer(Modifier.height(10.dp))
    for ((i, t) in listOf(
        "Pongan el celular plano en el centro de la mesa, con la pantalla hacia arriba.",
        "Siéntense alrededor — cada persona en su posición.",
        "Toquen GIRAR. La flecha gira y se detiene apuntando hacia afuera.",
        "A quien le señale la punta — ¡le toca!",
    ).withIndex()) {
        Row(Modifier.padding(bottom = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("${i + 1}.", fontFamily = Inter, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = GOLD_LIGHT)
            Text(t, fontFamily = Inter, fontSize = 14.sp, lineHeight = 21.sp, color = Color(0xB3FFFFFF))
        }
    }
    Spacer(Modifier.height(10.dp))
    Text("Modo Apuntador · este modo no necesita lista de nombres, solo personas físicas alrededor del celular.", fontFamily = Inter, fontSize = 12.5.sp, lineHeight = 19.sp, color = Color(0x80FFFFFF))
}
