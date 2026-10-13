package pe.pisacoma.iglesia.feature.videos

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import pe.pisacoma.iglesia.App
import pe.pisacoma.iglesia.R
import pe.pisacoma.iglesia.nav.Route
import pe.pisacoma.iglesia.ui.chrome.SiteFooter
import pe.pisacoma.iglesia.ui.components.pressable
import pe.pisacoma.iglesia.ui.theme.Inter
import pe.pisacoma.iglesia.ui.theme.Playfair
import pe.pisacoma.iglesia.ui.theme.Tokens

private val BG = Tokens.bg
private val GOLD = Tokens.accent
private val GOLD_LIGHT = Tokens.accentLight

/** The site's /videos: search bar + count, the hero, one rail per channel and category (Netflix-style), grid when searching. */
@Composable
fun VideosScreen(modifier: Modifier, go: (Route) -> Unit) {
    val vm: VideosViewModel = viewModel(key = "videos") { VideosViewModel(App.repos.userState) }
    val s by vm.state.collectAsState()
    val ctx = LocalContext.current

    LazyColumn(modifier.background(BG).testTag("videos-page")) {
        item {
            // .vid-topbar
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(Modifier.weight(1f).clip(CircleShape).background(Color(0x14FFFFFF)).border(1.dp, Color(0x1FFFFFFF), CircleShape).padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(painterResource(R.drawable.ic_nav_search), null, tint = Color(0x99FFFFFF), modifier = Modifier.size(16.dp))
                    Box(Modifier.weight(1f)) {
                        if (s.query.isEmpty()) Text("Buscar videos…", fontFamily = Inter, fontSize = 14.sp, color = Color(0x80FFFFFF))
                        BasicTextField(s.query, vm::query, singleLine = true, textStyle = TextStyle(fontFamily = Inter, fontSize = 14.sp, color = Color.White), cursorBrush = SolidColor(GOLD), modifier = Modifier.fillMaxWidth().testTag("videos-search"))
                    }
                    if (s.isSearching) Icon(painterResource(R.drawable.ic_nav_close), "Limpiar búsqueda", tint = Color(0xB3FFFFFF), modifier = Modifier.size(14.dp).pressable { vm.query("") })
                }
                Text("${s.videos.size} videos", fontFamily = Inter, fontSize = 12.sp, color = Color(0x80FFFFFF))
            }
        }
        when {
            s.loading -> item { Box(Modifier.fillMaxWidth().padding(60.dp), contentAlignment = Alignment.Center) { Text("Cargando el catálogo…", fontFamily = Inter, fontSize = 14.sp, color = Color(0x99FFFFFF)) } }
            s.offline -> item {
                Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 60.dp).testTag("videos-offline"), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Sin conexión", fontFamily = Playfair, fontWeight = FontWeight.SemiBold, fontSize = 26.sp, color = Color.White)
                    Spacer(Modifier.height(8.dp))
                    Text("El catálogo de videos necesita señal. Revisa tu conexión e inténtalo otra vez.", fontFamily = Inter, fontSize = 14.5.sp, lineHeight = 22.sp, color = Color(0x99FFFFFF), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    Spacer(Modifier.height(18.dp))
                    Box(Modifier.clip(RoundedCornerShape(10.dp)).background(Brush.linearGradient(listOf(GOLD, GOLD_LIGHT))).pressable(scale = 0.97f) { vm.load() }.padding(horizontal = 26.dp, vertical = 12.dp).testTag("videos-retry")) {
                        Text("Reintentar", fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = BG)
                    }
                }
            }
            s.isSearching -> {
                val res = s.results
                item {
                    Row(Modifier.padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("${res.size} resultado${if (res.size != 1) "s" else ""} para", fontFamily = Inter, fontSize = 13.sp, color = Color(0x99FFFFFF))
                        Text("\"${s.query}\"", fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Color.White)
                    }
                    if (res.isEmpty()) Text("No se encontraron videos.", fontFamily = Inter, fontSize = 14.sp, color = Color(0x80FFFFFF), modifier = Modifier.padding(16.dp))
                }
                items(res.size, key = { res[it].videoId }) { i -> GridCard(res[i]) { go(Route.Video(res[i].videoId)) } }
            }
            else -> {
                s.hero?.let { h -> item { Hero(h, onPlay = { go(Route.Video(h.videoId)) }, onYouTube = { ctx.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://www.youtube.com/watch?v=${h.videoId}"))) }) } }
                val rows = s.rows
                items(rows.size, key = { rows[it].key }) { i -> VideoRow(rows[i]) { go(Route.Video(it.videoId)) } }
            }
        }
        item { Spacer(Modifier.height(30.dp)); SiteFooter() }
    }
}

@Composable
private fun Hero(v: TaggedVideo, onPlay: () -> Unit, onYouTube: () -> Unit) {
    Box(Modifier.fillMaxWidth().testTag("videos-hero")) {
        AsyncImage(model = VideoCatalog.thumb(v.videoId, "maxresdefault"), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxWidth().aspectRatio(16f / 10f))
        Box(Modifier.fillMaxWidth().aspectRatio(16f / 10f).background(Brush.verticalGradient(0.2f to Color.Transparent, 1f to BG)))
        Column(Modifier.align(Alignment.BottomStart).padding(horizontal = 16.dp, vertical = 16.dp)) {
            Text("— MÁS RECIENTE", fontFamily = Inter, fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 2.sp, color = GOLD_LIGHT)
            Spacer(Modifier.height(8.dp))
            Text(v.title, fontFamily = Playfair, fontWeight = FontWeight.SemiBold, fontSize = 24.sp, lineHeight = 29.sp, color = Color.White, maxLines = 3, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(6.dp))
            Text(VideoCatalog.CHANNEL_NAME + (v.published?.let { " · ${VideoCatalog.relTimeEsIso(it)}" } ?: ""), fontFamily = Inter, fontSize = 12.5.sp, color = Color(0x99FFFFFF), maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(Modifier.clip(RoundedCornerShape(8.dp)).background(Color.White).pressable(scale = 0.97f, onClick = onPlay).padding(horizontal = 22.dp, vertical = 11.dp).testTag("hero-play"), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(painterResource(R.drawable.ic_play), null, tint = BG, modifier = Modifier.size(16.dp))
                    Text("Ver", fontFamily = Inter, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = BG)
                }
                Box(Modifier.clip(RoundedCornerShape(8.dp)).background(Color(0x33FFFFFF)).pressable(scale = 0.97f, onClick = onYouTube).padding(horizontal = 16.dp, vertical = 11.dp)) {
                    Text("Abrir en YouTube", fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun VideoRow(row: VideoRowUi, onPlay: (TaggedVideo) -> Unit) {
    Column(Modifier.padding(top = 22.dp).testTag("row-${row.key}")) {
        Row(Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("${row.num}.", fontFamily = Playfair, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = GOLD)
            Text(row.label, fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Color.White, modifier = Modifier.weight(1f))
            Text(row.videos.size.toString(), fontFamily = Inter, fontSize = 12.sp, color = Color(0x80FFFFFF))
        }
        LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(row.videos.size, key = { row.videos[it].videoId }) { i ->
                val v = row.videos[i]
                Column(Modifier.width(196.dp).pressable(scale = 0.97f) { onPlay(v) }.testTag("video-${v.videoId}")) {
                    Box(Modifier.fillMaxWidth().aspectRatio(16f / 9f).clip(RoundedCornerShape(8.dp)).background(Color(0xFF161B22))) {
                        AsyncImage(model = VideoCatalog.thumb(v.videoId), contentDescription = v.title, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f))
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(v.title, fontFamily = Inter, fontSize = 12.5.sp, lineHeight = 17.sp, color = Color(0xD9FFFFFF), maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}

@Composable
private fun GridCard(v: TaggedVideo, onPlay: () -> Unit) {
    Column(Modifier.fillMaxWidth().pressable(scale = 0.985f, onClick = onPlay).padding(horizontal = 16.dp, vertical = 8.dp).testTag("video-${v.videoId}")) {
        Box(Modifier.fillMaxWidth().aspectRatio(16f / 9f).clip(RoundedCornerShape(10.dp)).background(Color(0xFF161B22))) {
            AsyncImage(model = VideoCatalog.thumb(v.videoId), contentDescription = v.title, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f))
        }
        Row(Modifier.padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.size(36.dp).clip(CircleShape).background(Color(0xFF161B22)), contentAlignment = Alignment.Center) { Icon(painterResource(R.drawable.sda_symbol_circle), null, tint = GOLD_LIGHT, modifier = Modifier.size(22.dp)) }
            Column {
                Text(v.title, fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 19.sp, color = Color.White, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(VideoCatalog.CHANNEL_NAME + (v.published?.let { " · ${VideoCatalog.relTimeEsIso(it)}" } ?: ""), fontFamily = Inter, fontSize = 12.sp, color = Color(0x80FFFFFF), maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}
