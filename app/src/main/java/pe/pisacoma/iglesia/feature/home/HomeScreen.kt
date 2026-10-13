package pe.pisacoma.iglesia.feature.home

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import pe.pisacoma.iglesia.App
import pe.pisacoma.iglesia.R
import pe.pisacoma.iglesia.nav.Route
import pe.pisacoma.iglesia.ui.components.pressable
import pe.pisacoma.iglesia.ui.theme.DmSans
import pe.pisacoma.iglesia.ui.theme.Fraunces
import pe.pisacoma.iglesia.ui.theme.Tokens
import pe.pisacoma.iglesia.util.Zmanim

private val BASALT = Tokens.basalt
private val PARCHMENT = Tokens.parchment
private val OCHRE_LIGHT = Tokens.ochreLight
private val OLIVE_PALE = Tokens.olivePale
private val LIGHT_BG = Color(0xFFEFE6CC)

/** «Un día en el Altiplano»: eleven full-height scenes, each a looping Lottie over its poster with the section's text, as the site's home. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(modifier: Modifier, go: (Route) -> Unit) {
    val vm: HomeViewModel = viewModel { HomeViewModel(App.repos.assets, App.repos.quarterly, App.repos.church, App.repos.userState) }
    val s by vm.state.collectAsState()
    val conf = LocalConfiguration.current
    val portrait = conf.screenHeightDp >= conf.screenWidthDp
    // site: portrait sections are max(600px, 100svh − 66px); landscape clamp(560px, 88svh, 1000px)
    val sceneH: Dp = if (portrait) maxOf(600, conf.screenHeightDp - 66).dp else minOf(1000, maxOf(560, (conf.screenHeightDp * 0.88f).toInt())).dp
    val ctx = LocalContext.current

    Box(modifier.background(BASALT)) {
        LazyColumn(Modifier.fillMaxSize().testTag("home")) {
            item { Scene("inicio", sceneH, portrait, s.paused) {
                Title("Pisacoma", portada = true)
                Lede("Iglesia Adventista del Séptimo Día en el Altiplano de Puno, a 3.940 metros sobre el mar.")
                Cta("Visítanos un sábado", "inicio") { go(Route.Visitanos) }
            } }
            item { Scene("himnario", sceneH, portrait, s.paused) {
                Title("Himnario")
                Lede("1.823 himnos en español y portugués, con la letra completa y audio para cantar en casa o en el templo.")
                Cta("Abrir el himnario", "himnario") { go(Route.Himnario) }
            } }
            item { Scene("videos", sceneH, portrait, s.paused) {
                Title("Videos")
                Lede("Sermones y la lección de la semana de cuatro pastores, reunidos en un solo lugar.")
                Cta("Ver los videos", "videos") { go(Route.Videos) }
            } }
            item { Scene("escuela", sceneH, portrait, s.paused) {
                Title("Escuela Sabática")
                Lede(if (s.escEnded) "«${s.quarterTitle}». El trimestre terminó: puedes repasar sus ${s.weeksTotal} lecciones." else "«${s.quarterTitle}». Esta semana estudiamos la lección ${s.escWeek} de ${s.weeksTotal}.")
                Cta(if (s.escEnded) "Repasar el trimestre" else "Abrir la lección ${s.escWeek}", "escuela") { go(Route.Escuela) }
            } }
            item { Scene("biblia", sceneH, portrait, s.paused) {
                Title("La Biblia")
                Lede("La Reina-Valera 1960 completa, con tus notas y marcadores guardados en el teléfono.")
                Cta("Leer la Biblia", "biblia") { go(Route.Biblia()) }
            } }
            item { Scene("diccionario", sceneH, portrait, s.paused, light = true) {
                Title("Diccionario", light = true)
                Lede("¿Una palabra difícil en la lección o en un himno? Búscala y lee su significado.", light = true)
                Cta("Buscar una palabra", "diccionario", light = true) { go(Route.Diccionario) }
            } }
            item { Scene("egw", sceneH, portrait, s.paused) {
                Title("Elena G. de White")
                Lede("25 libros en español para leer por capítulo, desde El camino a Cristo hasta El conflicto de los siglos.")
                Cta("Entrar a la biblioteca", "egw") { go(Route.Egw) }
            } }
            item { Scene("quiz", sceneH, portrait, s.paused) {
                Title("Quiz de la lección")
                Lede("Responde las preguntas de la semana y guarda tu nivel, de Acán a Caleb.")
                Cta("Empezar el quiz", "quiz") { go(Route.Quiz) }
            } }
            item { Scene("quien", sceneH, portrait, s.paused) {
                Title("¿Quién?")
                Lede("Una ruleta para elegir quién ora, lee o canta, sin repetir a la misma persona.")
                Cta("Girar la ruleta", "quien") { go(Route.Quien) }
            } }
            if (s.anuncios.isNotEmpty()) item {
                Column(Modifier.fillMaxWidth().background(OLIVE_PALE).padding(horizontal = 20.dp, vertical = 48.dp).testTag("avisos")) {
                    Text("Esta semana en la iglesia", fontFamily = Fraunces, fontSize = 32.sp, lineHeight = 34.sp, color = BASALT)
                    Spacer(Modifier.height(24.dp))
                    s.anuncios.forEach { a ->
                        Box(Modifier.fillMaxWidth().height(1.dp).background(Color(0x2E2A2620)))
                        Column(Modifier.padding(vertical = 16.dp)) {
                            Text((if (a.isToday) "Hoy" else a.dateLabel) + (a.time?.let { ", $it" } ?: ""), fontFamily = DmSans, fontWeight = if (a.isToday) FontWeight.Bold else FontWeight.Medium, fontSize = 15.sp, color = if (a.isToday) Color(0xFF744612) else Color(0xFF4B5733))
                            Text(a.title, fontFamily = Fraunces, fontWeight = FontWeight.Medium, fontSize = 20.8.sp, lineHeight = 26.sp, color = BASALT)
                            if (a.detail != null) Text(a.detail, fontFamily = DmSans, fontSize = 15.sp, lineHeight = 22.sp, color = Color(0xFF3F3A30))
                        }
                    }
                }
            }
            item { Scene("sabado", sceneH, portrait, s.paused, strongVeil = true) {
                val sab = s.sabbath
                Title(if (sab?.isSabbath == true) "Feliz sábado" else "El sábado empieza al ponerse el sol")
                if (sab != null) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(28.dp), verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(bottom = 20.dp).testTag("sabado-times")) {
                        SolItem("${Zmanim.dayLabel(sab.friday)}, empieza", Zmanim.clock12(sab.start))
                        SolItem("${Zmanim.dayLabel(sab.saturday)}, termina", Zmanim.clock12(sab.end))
                    }
                }
                FlowRow(horizontalArrangement = Arrangement.spacedBy(28.dp), verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(bottom = 28.dp)) {
                    s.schedule.forEach { it ->
                        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                            Text("${it.day} ${it.time} ${it.suffix}", fontFamily = DmSans, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = PARCHMENT)
                            Text(it.name, fontFamily = DmSans, fontSize = 16.sp, color = PARCHMENT)
                        }
                    }
                }
                Cta("Añadir al calendario", "sabado") { go(Route.Visitanos) }
            } }
            item { Scene("noche", sceneH, portrait, s.paused) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.padding(bottom = 16.dp)) {
                    Icon(painterResource(R.drawable.sda_symbol), null, tint = OCHRE_LIGHT, modifier = Modifier.size(width = 60.dp, height = 54.dp))
                    Text("Iglesia Adventista del Séptimo Día de Pisacoma", fontFamily = Fraunces, fontSize = 25.6.sp, lineHeight = 28.sp, color = PARCHMENT)
                }
                Lede("Distrito de Pisacoma, Chucuito, Puno. Te esperamos el sábado.")
                Cta("Cómo llegar", "noche") { go(Route.Visitanos) }
                Spacer(Modifier.height(44.dp))
                // .creditos
                FlowRow(modifier = Modifier.testTag("creditos")) {
                    Text("Fotos animadas a partir de Wikimedia Commons: ", fontFamily = DmSans, fontSize = 12.8.sp, lineHeight = 20.sp, color = PARCHMENT.copy(alpha = 0.78f))
                    s.photos.forEachIndexed { i, p ->
                        Text(p.title, fontFamily = DmSans, fontSize = 12.8.sp, lineHeight = 20.sp, color = PARCHMENT.copy(alpha = 0.78f), textDecoration = TextDecoration.Underline, modifier = Modifier.pressable(scale = 0.98f) { ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(p.source))) })
                        Text(", de ${p.author} (", fontFamily = DmSans, fontSize = 12.8.sp, lineHeight = 20.sp, color = PARCHMENT.copy(alpha = 0.78f))
                        Text(p.license, fontFamily = DmSans, fontSize = 12.8.sp, lineHeight = 20.sp, color = PARCHMENT.copy(alpha = 0.78f), textDecoration = TextDecoration.Underline, modifier = Modifier.pressable(scale = 0.98f) { ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(p.licenseUrl))) })
                        Text(if (i < s.photos.lastIndex) "); " else "). ", fontFamily = DmSans, fontSize = 12.8.sp, lineHeight = 20.sp, color = PARCHMENT.copy(alpha = 0.78f))
                    }
                    Text("Los videos de esas secciones se comparten con la misma licencia.", fontFamily = DmSans, fontSize = 12.8.sp, lineHeight = 20.sp, color = PARCHMENT.copy(alpha = 0.78f))
                }
                Spacer(Modifier.height(24.dp))
            } }
        }
        // .escenas-pausa — fixed, bottom right; never tied to the system's reduce-motion setting
        Box(
            Modifier.align(Alignment.BottomEnd).navigationBarsPadding().padding(16.dp).size(44.dp).clip(CircleShape)
                .background(Color(0xB82A2620)).border(1.dp, PARCHMENT.copy(alpha = 0.3f), CircleShape)
                .pressable(scale = 0.95f, onClick = vm::togglePause)
                .testTag(if (s.paused) "home-pause-on" else "home-pause")
                .semantics { contentDescription = if (s.paused) "Reproducir las animaciones" else "Pausar las animaciones" },
            contentAlignment = Alignment.Center,
        ) {
            Icon(painterResource(if (s.paused) R.drawable.ic_play else R.drawable.ic_pause), null, tint = PARCHMENT, modifier = Modifier.size(18.dp))
        }
        if (s.paused) Box(Modifier.size(1.dp).testTag("home-pause-on-marker"))
    }
}

/** One section: poster → Lottie (portrait/landscape cut) → veil → text, bottom-aligned. */
@Composable
private fun Scene(id: String, height: Dp, portrait: Boolean, paused: Boolean, light: Boolean = false, strongVeil: Boolean = false, content: @Composable BoxScope.() -> Unit) {
    val cut = if (portrait) "portrait" else "landscape"
    Box(Modifier.fillMaxWidth().height(height).background(if (light) LIGHT_BG else BASALT).testTag("scene-$id")) {
        AsyncImage(model = "file:///android_asset/home/$id-$cut.jpg", contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        val composition by rememberLottieComposition(LottieCompositionSpec.Asset("home/lottie/$id-$cut.json"), imageAssetsFolder = "")
        val progress by animateLottieCompositionAsState(composition, iterations = LottieConstants.IterateForever, isPlaying = !paused)
        LottieAnimation(composition, { progress }, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        // veil: bottom (and left on landscape), stronger on #sabado, pale on the light scene
        val dark = Color(0xFF16120C)
        val pale = LIGHT_BG
        val bottom = when {
            light -> Brush.verticalGradient(0f to Color.Transparent, 0.4f to pale.copy(alpha = 0.7f), 1f to pale.copy(alpha = 0.96f))
            strongVeil -> Brush.verticalGradient(0f to dark.copy(alpha = 0.28f), 0.55f to dark.copy(alpha = 0.6f), 1f to dark.copy(alpha = 0.88f))
            else -> Brush.verticalGradient(0f to Color.Transparent, 0.4f to Color.Transparent, 0.68f to dark.copy(alpha = 0.55f), 1f to dark.copy(alpha = 0.86f))
        }
        Box(Modifier.fillMaxSize().background(bottom))
        if (!portrait) Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(0f to (if (light) pale else dark).copy(alpha = if (light) 0.85f else 0.62f), 0.36f to (if (light) pale else dark).copy(alpha = if (light) 0.3f else 0.3f), 0.58f to Color.Transparent)))
        Column(Modifier.align(Alignment.BottomStart).fillMaxWidth(if (portrait) 1f else 0.46f).padding(horizontal = 20.dp).padding(bottom = 44.dp)) {
            Box { content() }
        }
    }
}

@Composable
private fun Title(text: String, portada: Boolean = false, light: Boolean = false) {
    Text(
        text,
        fontFamily = Fraunces,
        fontWeight = if (portada) FontWeight.Light else FontWeight.Normal,
        fontSize = if (portada) 68.sp else 44.sp,
        lineHeight = if (portada) 62.sp else 43.sp,
        letterSpacing = if (portada) (-0.03 * 68).sp else (-0.015 * 44).sp,
        color = if (light) BASALT else PARCHMENT,
        modifier = Modifier.padding(bottom = 14.dp),
    )
}

@Composable
private fun Lede(text: String, light: Boolean = false) {
    Text(text, fontFamily = DmSans, fontSize = 16.8.sp, lineHeight = 26.sp, color = if (light) BASALT else PARCHMENT, modifier = Modifier.padding(bottom = 28.dp))
}

@Composable
private fun Cta(label: String, id: String, light: Boolean = false, onClick: () -> Unit) {
    Box(
        Modifier.height(48.dp).clip(CircleShape).background(if (light) BASALT else PARCHMENT).pressable(scale = 0.98f, onClick = onClick).padding(horizontal = 24.dp).testTag("scene-$id-action"),
        contentAlignment = Alignment.Center,
    ) { Text(label, fontFamily = DmSans, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = if (light) PARCHMENT else BASALT) }
}

@Composable
private fun SolItem(label: String, clock: Zmanim.Clock12) {
    Column {
        Text(label, fontFamily = DmSans, fontWeight = FontWeight.Medium, fontSize = 16.sp, color = PARCHMENT)
        Row(verticalAlignment = Alignment.Bottom) {
            Text(clock.time, fontFamily = Fraunces, fontSize = 44.sp, lineHeight = 44.sp, color = PARCHMENT)
            Text(clock.suffix, fontFamily = DmSans, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = PARCHMENT, modifier = Modifier.padding(start = 5.dp, bottom = 6.dp))
        }
    }
}
