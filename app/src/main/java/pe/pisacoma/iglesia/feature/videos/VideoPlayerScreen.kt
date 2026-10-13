package pe.pisacoma.iglesia.feature.videos

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import kotlinx.coroutines.delay
import pe.pisacoma.iglesia.App
import pe.pisacoma.iglesia.R
import pe.pisacoma.iglesia.nav.Route
import pe.pisacoma.iglesia.ui.components.pressable
import pe.pisacoma.iglesia.ui.theme.Inter
import pe.pisacoma.iglesia.ui.theme.Playfair
import pe.pisacoma.iglesia.ui.theme.Tokens

private val SPEEDS = listOf(0.75f, 1f, 1.25f, 1.5f, 2f)

/**
 * The site's CustomPlayer: YouTube's IFrame player (required by YouTube) under the app's own
 * HBO-style controls — play/pause, ±10 s, scrub bar with time, speed, mute, fullscreen —
 * that hide after 3.2 s. The catalogue row of related videos follows below.
 */
@Composable
fun VideoPlayerScreen(videoId: String, modifier: Modifier, go: (Route) -> Unit) {
    val vm: VideosViewModel = viewModel(key = "videos") { VideosViewModel(App.repos.userState) }
    val s by vm.state.collectAsState()
    val video = s.videos.firstOrNull { it.videoId == videoId }
    val ctx = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    var player by remember { mutableStateOf<YouTubePlayer?>(null) }
    var playing by remember { mutableStateOf(true) }
    var current by remember { mutableFloatStateOf(0f) }
    var duration by remember { mutableFloatStateOf(0f) }
    var muted by remember { mutableStateOf(false) }
    var speed by remember { mutableFloatStateOf(1f) }
    var showSpeed by remember { mutableStateOf(false) }
    var showCtrl by remember { mutableStateOf(true) }
    var fullscreen by remember { mutableStateOf(false) }
    var lastTouch by remember { mutableStateOf(System.currentTimeMillis()) }
    var dragging by remember { mutableFloatStateOf(-1f) }
    LaunchedEffect(lastTouch) { delay(3200); if (playing) showCtrl = false }
    fun reveal() { showCtrl = true; lastTouch = System.currentTimeMillis() }
    DisposableEffect(fullscreen) {
        val a = ctx as? Activity
        a?.requestedOrientation = if (fullscreen) ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE else ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        onDispose { a?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED }
    }
    BackHandler(enabled = fullscreen) { fullscreen = false }

    Column(modifier.background(Color.Black).then(if (fullscreen) Modifier.fillMaxSize() else Modifier.verticalScroll(rememberScrollState()))) {
        Box(Modifier.fillMaxWidth().then(if (fullscreen) Modifier.fillMaxSize() else Modifier.aspectRatio(16f / 9f)).background(Color.Black).testTag("player")) {
            AndroidView(
                factory = { c ->
                    YouTubePlayerView(c).apply {
                        enableAutomaticInitialization = false
                        lifecycle.addObserver(this)
                        initialize(
                            object : AbstractYouTubePlayerListener() {
                                override fun onReady(youTubePlayer: YouTubePlayer) { player = youTubePlayer; youTubePlayer.loadVideo(videoId, 0f) }
                                override fun onStateChange(youTubePlayer: YouTubePlayer, state: PlayerConstants.PlayerState) { playing = state == PlayerConstants.PlayerState.PLAYING }
                                override fun onCurrentSecond(youTubePlayer: YouTubePlayer, second: Float) { if (dragging < 0) current = second }
                                override fun onVideoDuration(youTubePlayer: YouTubePlayer, d: Float) { duration = d }
                            },
                            // controls 0: the app draws its own, as the site does
                            IFramePlayerOptions.Builder(c).controls(0).rel(0).ivLoadPolicy(3).ccLoadPolicy(0).build(),
                        )
                    }
                },
                modifier = Modifier.fillMaxSize(),
            )
            // tap layer: reveal / hide controls, double-tap sides to skip ±10 s
            Box(Modifier.fillMaxSize().clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { if (showCtrl) showCtrl = false else reveal() })
            if (showCtrl) Box(Modifier.fillMaxSize()) {
                Box(Modifier.fillMaxSize().background(Brush.verticalGradient(0f to Color(0x99000000), 0.4f to Color.Transparent, 0.7f to Color.Transparent, 1f to Color(0xCC000000)))) {
                    // top
                    Row(Modifier.fillMaxWidth().padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        IconBtn(R.drawable.ic_nav_close, "Cerrar", "player-close") { if (fullscreen) fullscreen = false else go(Route.Videos) }
                        Spacer(Modifier.size(8.dp))
                        Text(video?.title ?: "", fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                    }
                    // centre: ±10 and play
                    Row(Modifier.align(Alignment.Center), horizontalArrangement = Arrangement.spacedBy(34.dp), verticalAlignment = Alignment.CenterVertically) {
                        IconBtn(R.drawable.ic_back, "Retroceder 10s", "player-back10", big = true) { player?.seekTo((current - 10f).coerceAtLeast(0f)); reveal() }
                        Box(Modifier.size(64.dp).clip(CircleShape).background(Color(0xE6FFFFFF)).pressable(scale = 0.95f) { if (playing) player?.pause() else player?.play(); reveal() }.testTag("player-toggle"), contentAlignment = Alignment.Center) {
                            Icon(painterResource(if (playing) R.drawable.ic_pause else R.drawable.ic_play), if (playing) "Pausar" else "Reproducir", tint = Color.Black, modifier = Modifier.size(28.dp))
                        }
                        IconBtn(R.drawable.ic_nav_arrow, "Avanzar 10s", "player-fwd10", big = true) { player?.seekTo((current + 10f).coerceAtMost(if (duration > 0) duration else current + 10f)); reveal() }
                    }
                    // bottom
                    Column(Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)) {
                        Slider(
                            value = if (dragging >= 0) dragging else if (duration > 0) (current / duration).coerceIn(0f, 1f) else 0f,
                            onValueChange = { dragging = it; reveal() },
                            onValueChangeFinished = { if (duration > 0) { player?.seekTo(dragging * duration); current = dragging * duration }; dragging = -1f },
                            colors = SliderDefaults.colors(thumbColor = Tokens.accentLight, activeTrackColor = Tokens.accent, inactiveTrackColor = Color(0x4DFFFFFF)),
                            modifier = Modifier.fillMaxWidth().height(28.dp).testTag("player-seek"),
                        )
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            IconBtn(if (playing) R.drawable.ic_pause else R.drawable.ic_play, if (playing) "Pausar" else "Reproducir", "player-toggle-small") { if (playing) player?.pause() else player?.play(); reveal() }
                            Text(VideoCatalog.fmtTime(current.toDouble()) + (if (duration > 0) " / " + VideoCatalog.fmtTime(duration.toDouble()) else ""), fontFamily = Inter, fontSize = 12.sp, color = Color.White, modifier = Modifier.testTag("player-time"))
                            Spacer(Modifier.weight(1f))
                            Box {
                                Text(if (speed == 1f) "1×" else "${speed}×", fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Color.White, modifier = Modifier.clip(RoundedCornerShape(6.dp)).pressable { showSpeed = !showSpeed; reveal() }.padding(horizontal = 8.dp, vertical = 6.dp).testTag("player-speed"))
                                if (showSpeed) Column(Modifier.align(Alignment.BottomEnd).padding(bottom = 34.dp).clip(RoundedCornerShape(10.dp)).background(Color(0xF2161B22)).padding(6.dp)) {
                                    SPEEDS.forEach { sp ->
                                        Text(
                                            if (sp == 1f) "Normal" else "${sp}×", fontFamily = Inter, fontSize = 13.sp, color = if (sp == speed) Tokens.accentLight else Color.White,
                                            modifier = Modifier.pressable { speed = sp; player?.setPlaybackRate(toRate(sp)); showSpeed = false; reveal() }.padding(horizontal = 14.dp, vertical = 8.dp),
                                        )
                                    }
                                }
                            }
                            IconBtn(if (muted) R.drawable.ic_minus else R.drawable.ic_plus, if (muted) "Activar sonido" else "Silenciar", "player-mute") { muted = !muted; if (muted) player?.mute() else player?.unMute(); reveal() }
                            IconBtn(R.drawable.ic_fullscreen, "Pantalla completa", "player-fullscreen") { fullscreen = !fullscreen; reveal() }
                        }
                    }
                }
            }
        }
        if (!fullscreen) {
            Column(Modifier.background(Tokens.bg).padding(16.dp)) {
                Text(video?.title ?: "", fontFamily = Playfair, fontWeight = FontWeight.SemiBold, fontSize = 20.sp, lineHeight = 25.sp, color = Color.White)
                Spacer(Modifier.height(6.dp))
                Text(VideoCatalog.CHANNEL_NAME + (video?.published?.let { " · ${VideoCatalog.relTimeEsIso(it)}" } ?: ""), fontFamily = Inter, fontSize = 12.5.sp, color = Color(0x99FFFFFF))
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(Modifier.clip(RoundedCornerShape(8.dp)).background(Color(0x33FFFFFF)).pressable(scale = 0.97f) { ctx.startActivity(Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://www.youtube.com/watch?v=$videoId"))) }.padding(horizontal = 14.dp, vertical = 10.dp)) {
                        Text("Abrir en YouTube", fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Color.White)
                    }
                    Box(Modifier.clip(RoundedCornerShape(8.dp)).background(Color(0x33FFFFFF)).pressable(scale = 0.97f) {
                        ctx.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, "${video?.title ?: ""}\nhttps://quiz-qwik.vercel.app/videos/$videoId") }, "Compartir"))
                    }.padding(horizontal = 14.dp, vertical = 10.dp)) {
                        Text("Compartir", fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Color.White)
                    }
                }
                Spacer(Modifier.height(22.dp))
                Text("Más videos", fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = Color.White)
                Spacer(Modifier.height(10.dp))
                s.videos.filter { it.videoId != videoId }.take(12).forEach { v ->
                    Row(Modifier.fillMaxWidth().pressable(scale = 0.98f) { go(Route.Video(v.videoId)) }.padding(vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(width = 128.dp, height = 72.dp).clip(RoundedCornerShape(6.dp)).background(Color(0xFF161B22))) {
                            coil.compose.AsyncImage(model = VideoCatalog.thumb(v.videoId), contentDescription = null, contentScale = androidx.compose.ui.layout.ContentScale.Crop, modifier = Modifier.fillMaxSize())
                        }
                        Text(v.title, fontFamily = Inter, fontSize = 13.sp, lineHeight = 18.sp, color = Color(0xD9FFFFFF), maxLines = 3, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
    }
}

private fun toRate(sp: Float): PlayerConstants.PlaybackRate = when (sp) {
    0.75f -> PlayerConstants.PlaybackRate.RATE_0_75
    1.25f -> PlayerConstants.PlaybackRate.RATE_1_25
    1.5f -> PlayerConstants.PlaybackRate.RATE_1_5
    2f -> PlayerConstants.PlaybackRate.RATE_2
    else -> PlayerConstants.PlaybackRate.RATE_1
}

@Composable
private fun IconBtn(iconRes: Int, label: String, tag: String, big: Boolean = false, onClick: () -> Unit) {
    Box(Modifier.size(if (big) 48.dp else 38.dp).clip(CircleShape).background(Color(0x33000000)).pressable(onClick = onClick).testTag(tag), contentAlignment = Alignment.Center) {
        Icon(painterResource(iconRes), label, tint = Color.White, modifier = Modifier.size(if (big) 22.dp else 18.dp))
    }
}
