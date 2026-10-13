package pe.pisacoma.iglesia.feature.himnario

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
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
import androidx.lifecycle.viewmodel.compose.viewModel
import pe.pisacoma.iglesia.App
import pe.pisacoma.iglesia.R
import pe.pisacoma.iglesia.audio.AudioUrls
import pe.pisacoma.iglesia.audio.PlayerConnection
import pe.pisacoma.iglesia.data.model.Hymn
import pe.pisacoma.iglesia.nav.Route
import pe.pisacoma.iglesia.ui.chrome.SiteFooter
import pe.pisacoma.iglesia.ui.components.AudioCard
import pe.pisacoma.iglesia.ui.components.AudioTrack
import pe.pisacoma.iglesia.ui.components.Hairline
import pe.pisacoma.iglesia.ui.components.Ochre
import pe.pisacoma.iglesia.ui.components.ShareButton
import pe.pisacoma.iglesia.ui.components.ToolPill
import pe.pisacoma.iglesia.ui.components.YouTubeEmbed
import pe.pisacoma.iglesia.ui.components.pressable
import pe.pisacoma.iglesia.ui.theme.Fraunces
import pe.pisacoma.iglesia.ui.theme.Inter
import pe.pisacoma.iglesia.ui.theme.Tokens

/** The site's /himnario/<variant>/<n>: crumbs, numeral + title, tools, audio, the typeset lyrics, pager. */
@Composable
fun HymnScreen(variant: String, number: Int, modifier: Modifier, go: (Route) -> Unit) {
    val vm: HymnViewModel = viewModel(key = "$variant/$number") { HymnViewModel(App.repos.hymns, App.repos.userState, variant, number) }
    val ui by vm.state.collectAsState()
    when (val u = ui) {
        HymnUi.Loading -> Box(modifier)
        HymnUi.NotFound -> Column(modifier.padding(20.dp)) {
            Text("Himno no encontrado", fontFamily = Fraunces, fontSize = 24.sp, color = Tokens.inkDeep)
            Text("← Volver al himnario", fontFamily = Inter, color = Tokens.sage, modifier = Modifier.padding(top = 12.dp).pressable { go(Route.Himnario) })
        }
        is HymnUi.Ready -> HymnBody(u.hymn, u.slides, u.hasNext, modifier, go)
    }
}

@Composable
private fun HymnBody(h: Hymn, slides: List<ProjectorSlide>, hasNext: Boolean, modifier: Modifier, go: (Route) -> Unit) {
    val ctx = LocalContext.current
    val player = remember { PlayerConnection(ctx.applicationContext) }
    DisposableEffect(Unit) { player.connect(); onDispose { player.release() } }
    val ps by player.state.collectAsState()
    var projecting by remember { mutableStateOf(false) }

    Column(modifier.verticalScroll(rememberScrollState())) {
        Column(Modifier.padding(horizontal = 20.dp)) {
            // .crumbs
            Row(Modifier.padding(top = 36.dp, bottom = 22.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("HIMNARIO", fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, letterSpacing = 2.2.sp, color = Tokens.sage, modifier = Modifier.pressable(scale = 0.98f) { go(Route.Himnario) })
                Text("/", fontFamily = Inter, fontSize = 11.sp, color = Tokens.sageText.copy(alpha = 0.5f))
                Text(h.variant.label.uppercase(), fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, letterSpacing = 2.2.sp, color = Tokens.sageText)
            }
            // .head
            Row(Modifier.padding(top = 16.dp, bottom = 22.dp), horizontalArrangement = Arrangement.spacedBy(18.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    h.number.toString(),
                    fontFamily = Fraunces, fontStyle = FontStyle.Italic, fontWeight = FontWeight.ExtraBold, fontSize = 48.sp, lineHeight = 41.sp,
                    letterSpacing = (-0.04 * 48).sp, color = Ochre.numeral, modifier = Modifier.testTag("hymn-number"),
                )
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(h.title, fontFamily = Fraunces, fontWeight = FontWeight.SemiBold, fontSize = 24.sp, lineHeight = 26.sp, letterSpacing = (-0.022 * 24).sp, color = Tokens.inkDeep)
                    if (h.bibleReference != null) Text(h.bibleReference, fontFamily = Fraunces, fontStyle = FontStyle.Italic, fontSize = 15.2.sp, color = Tokens.sageText)
                }
            }
            Hairline(0.18f)
            Spacer(Modifier.height(16.dp))
            // .tools
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ToolPill(R.drawable.ic_project, "Proyectar", testTag = "hymn-project") { projecting = true }
                ShareButton(
                    title = "#${h.number} ${h.title}",
                    text = "Himno ${h.number} · ${h.title} — ${h.variant.label}",
                    path = "/himnario/${h.variant.slug}/${h.number}/",
                )
            }
            Spacer(Modifier.height(24.dp))
            // audio
            if (h.ytId != null) {
                YouTubeEmbed(h.ytId)
                Spacer(Modifier.height(30.dp))
            } else if (h.mp3Url != null) {
                val tracks = buildList {
                    add(AudioTrack(AudioUrls.mediaId(h, false), "Vocal", AudioUrls.title(h, false), AudioUrls.streamUrl(h, false)!!))
                    if (h.mp3UrlInstr != null) add(AudioTrack(AudioUrls.mediaId(h, true), "Instrumental", AudioUrls.title(h, true), AudioUrls.streamUrl(h, true)!!))
                }
                AudioCard(tracks, ps, onToggle = { t -> player.play(t.mediaId, t.title, t.url) }, onSeek = player::seekTo)
                Spacer(Modifier.height(32.dp))
            }
            // .lyrics — .verse rows: numeral column + lines; the chorus is italic with a 2px ochre rule
            Column(Modifier.testTag("hymn-lyrics")) {
                h.blocks.forEachIndexed { i, b ->
                    Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min).padding(bottom = 24.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        if (b.isChorus) {
                            Text("CORO", fontFamily = Inter, fontWeight = FontWeight.Bold, fontSize = 12.5.sp, letterSpacing = 2.4.sp, color = Ochre.label, textAlign = TextAlign.End, modifier = Modifier.width(28.dp).padding(top = 9.dp))
                            Box(Modifier.width(2.dp).fillMaxHeight().background(Color(0x8CB8924F)))
                        } else {
                            Text((b.stanzaNumber ?: (i + 1)).toString(), fontFamily = Fraunces, fontStyle = FontStyle.Italic, fontWeight = FontWeight.Bold, fontSize = 18.4.sp, color = Ochre.label, textAlign = TextAlign.End, modifier = Modifier.width(28.dp).padding(top = 4.dp))
                        }
                        Text(
                            b.lines.joinToString("\n"),
                            fontFamily = Fraunces,
                            fontStyle = if (b.isChorus) FontStyle.Italic else FontStyle.Normal,
                            fontSize = 17.3.sp,
                            lineHeight = 30.8.sp,
                            color = if (b.isChorus) Tokens.forest else Tokens.ink,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    if (i < h.blocks.lastIndex) {
                        Box(Modifier.fillMaxWidth().height(1.dp).background(Tokens.sageA(0.18f)))
                        Spacer(Modifier.height(28.dp))
                    }
                }
            }
            // .pager
            Spacer(Modifier.height(50.dp))
            Hairline(0.18f)
            Spacer(Modifier.height(26.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally)) {
                if (h.number > 1) PagerPill("← ${h.number - 1}", "hymn-prev") { go(Route.Hymn(h.variant.slug, h.number - 1)) }
                PagerPill("Índice", "hymn-index", ochre = true) { go(Route.Himnario) }
                if (hasNext) PagerPill("${h.number + 1} →", "hymn-next") { go(Route.Hymn(h.variant.slug, h.number + 1)) }
            }
            Spacer(Modifier.height(70.dp))
        }
        SiteFooter()
    }
    if (projecting) HymnProjector(h.number, h.title, h.bibleReference, slides) { projecting = false }
}

@Composable
private fun PagerPill(label: String, tag: String, ochre: Boolean = false, onClick: () -> Unit) {
    Box(
        Modifier
            .clip(CircleShape)
            .background(Color(0x80FFFFFF))
            .border(1.dp, if (ochre) Color(0x66B8924F) else Tokens.sageA(0.24f), CircleShape)
            .pressable(scale = 0.97f, onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 11.dp)
            .testTag(tag),
    ) {
        Text(label, fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = if (ochre) Ochre.label else Tokens.forest)
    }
}
