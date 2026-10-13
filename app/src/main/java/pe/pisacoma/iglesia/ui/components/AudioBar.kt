package pe.pisacoma.iglesia.ui.components

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pe.pisacoma.iglesia.R
import pe.pisacoma.iglesia.audio.PlayerState
import pe.pisacoma.iglesia.ui.chrome.Eyebrow
import pe.pisacoma.iglesia.ui.theme.Inter
import pe.pisacoma.iglesia.ui.theme.Tokens

/** m:ss for a duration in milliseconds. */
fun clockMs(ms: Long): String {
    val s = (ms / 1000).coerceAtLeast(0)
    return "${s / 60}:${(s % 60).toString().padStart(2, '0')}"
}

/**
 * The hymn page's `.audio` card: a parchment card with one row per track («Vocal»,
 * «Instrumental»), each a play/pause button, a scrub bar and the time.
 */
@Composable
fun AudioCard(tracks: List<AudioTrack>, state: PlayerState, onToggle: (AudioTrack) -> Unit, onSeek: (Long) -> Unit, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(18.dp)
    Column(
        modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Brush.verticalGradient(listOf(Color(0xFFFBF9EF), Color(0xFFECE9D6))))
            .border(1.dp, Tokens.sageA(0.20f), shape)
            .padding(horizontal = 24.dp, vertical = 18.dp)
            .testTag("audio-card"),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        tracks.forEach { t ->
            val current = state.mediaId == t.mediaId
            Eyebrow(t.label, Tokens.sage, size = 10.5f, tracking = 2f, weight = FontWeight.Bold)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(bottom = 6.dp)) {
                Box(
                    Modifier.size(44.dp).clip(CircleShape).background(Tokens.forest).pressable { onToggle(t) }.testTag("audio-toggle-${t.mediaId}"),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(painterResource(if (current && state.isPlaying) R.drawable.ic_pause else R.drawable.ic_play), if (current && state.isPlaying) "Pausar" else "Reproducir", tint = Tokens.cream, modifier = Modifier.size(20.dp))
                }
                var dragging by remember { mutableFloatStateOf(-1f) }
                val dur = if (current) state.durationMs else 0L
                val pos = if (current) state.positionMs else 0L
                Slider(
                    value = if (dragging >= 0) dragging else if (dur > 0) pos.toFloat() / dur else 0f,
                    onValueChange = { dragging = it },
                    onValueChangeFinished = { if (dur > 0) onSeek((dragging * dur).toLong()); dragging = -1f },
                    enabled = current && dur > 0,
                    colors = SliderDefaults.colors(thumbColor = Tokens.sage, activeTrackColor = Tokens.sage, inactiveTrackColor = Tokens.sageA(0.18f), disabledThumbColor = Tokens.sageA(0.3f), disabledActiveTrackColor = Tokens.sageA(0.18f), disabledInactiveTrackColor = Tokens.sageA(0.12f)),
                    modifier = Modifier.weight(1f),
                )
                Text(
                    if (current && state.isBuffering && dur == 0L) "…" else "${clockMs(pos)} / ${clockMs(dur)}",
                    fontFamily = Inter, fontSize = 11.5.sp, color = Tokens.sageText, modifier = Modifier.width(74.dp),
                )
            }
        }
        if (state.error != null) {
            Spacer(Modifier.height(2.dp))
            Text(state.error, fontFamily = Inter, fontSize = 12.5.sp, color = Color(0xFF8A3A2A))
        }
    }
}

data class AudioTrack(val mediaId: String, val label: String, val title: String, val url: String)
