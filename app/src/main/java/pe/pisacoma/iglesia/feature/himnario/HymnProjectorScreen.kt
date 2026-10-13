package pe.pisacoma.iglesia.feature.himnario

import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import pe.pisacoma.iglesia.R
import pe.pisacoma.iglesia.ui.components.pressable
import pe.pisacoma.iglesia.ui.theme.Fraunces
import pe.pisacoma.iglesia.ui.theme.Inter
import pe.pisacoma.iglesia.ui.theme.Tokens

private val GOLD = Color(0xFFD9B36C)
private val CREAM = Tokens.cream

/**
 * «Proyectar» (site components/hymn-projector.tsx): a full-screen dark view of the hymn for a
 * TV or a projector. Screen 0 is the cover; then one per stanza with the chorus repeated.
 * Tap the left third to go back, anywhere else to advance. Keeps the screen on.
 */
@Composable
fun HymnProjector(number: Int, title: String, bibleRef: String?, slides: List<ProjectorSlide>, onClose: () -> Unit) {
    var idx by remember { mutableIntStateOf(0) }
    val total = slides.size + 1
    val ctx = LocalContext.current
    DisposableEffect(Unit) {
        val w = (ctx as? android.app.Activity)?.window
        w?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose { w?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) }
    }
    Dialog(onDismissRequest = onClose, properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)) {
        BackHandler(onBack = onClose)
        Column(
            Modifier
                .fillMaxSize()
                .background(Brush.radialGradient(0f to Color(0xFF1D2A1F), 0.7f to Color(0xFF0C120D), center = androidx.compose.ui.geometry.Offset(0.5f, 0f), radius = 2000f))
                .statusBarsPadding()
                .navigationBarsPadding()
                .testTag("projector"),
        ) {
            // .proj-top
            Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(Modifier.weight(1f), verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(number.toString(), fontFamily = Fraunces, fontStyle = FontStyle.Italic, fontSize = 18.4.sp, color = GOLD)
                    Text("· $title", fontFamily = Inter, fontWeight = FontWeight.Medium, fontSize = 15.2.sp, color = CREAM.copy(alpha = 0.62f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                ProjButton(R.drawable.ic_nav_close, "Salir de la proyección", "projector-close", onClose)
            }
            // .stage
            BoxWithConstraints(
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .pointerInput(total) {
                        detectTapsLeftRight { left -> idx = (if (left) idx - 1 else idx + 1).coerceIn(0, total - 1) }
                    }
                    .testTag("projector-stage"),
                contentAlignment = Alignment.Center,
            ) {
                val slide = if (idx > 0) slides[idx - 1] else null
                if (slide == null) {
                    Column(Modifier.padding(horizontal = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(number.toString(), fontFamily = Fraunces, fontStyle = FontStyle.Italic, fontWeight = FontWeight.Bold, fontSize = 96.sp, lineHeight = 96.sp, color = GOLD)
                        Spacer(Modifier.height(14.dp))
                        Text(title, fontFamily = Fraunces, fontWeight = FontWeight.Medium, fontSize = 34.sp, lineHeight = 37.sp, color = CREAM, textAlign = TextAlign.Center)
                        if (bibleRef != null) {
                            Spacer(Modifier.height(12.dp))
                            Text(bibleRef, fontFamily = Fraunces, fontStyle = FontStyle.Italic, fontSize = 17.sp, color = CREAM.copy(alpha = 0.7f))
                        }
                        Spacer(Modifier.height(36.dp))
                        Text("→ o toca la pantalla para empezar", fontFamily = Inter, fontWeight = FontWeight.Medium, fontSize = 14.4.sp, color = CREAM.copy(alpha = 0.45f))
                    }
                } else {
                    // Fit the stanza to the stage: bounded by line count and by the longest line.
                    val longest = maxOf(12, slide.lines.maxOf { it.length })
                    val byHeight = (maxHeight.value - 40f) / (slide.lines.size * 1.34f)
                    val byWidth = maxWidth.value * 0.9f / (longest * 0.5f)
                    val size = minOf(byHeight, byWidth, 76.8f).coerceAtLeast(16f)
                    Column(Modifier.padding(horizontal = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(slide.label.uppercase(), fontFamily = Inter, fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 3.4.sp, color = GOLD)
                        Spacer(Modifier.height(16.dp))
                        Text(
                            slide.lines.joinToString("\n"),
                            fontFamily = Fraunces,
                            fontWeight = FontWeight.Medium,
                            fontStyle = if (slide.isChorus) FontStyle.Italic else FontStyle.Normal,
                            fontSize = size.sp,
                            lineHeight = (size * 1.34f).sp,
                            color = if (slide.isChorus) Color(0xFFF1E3C0) else CREAM,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
            // .proj-bottom
            Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                ProjNav("← Anterior", idx > 0, "projector-prev") { idx = (idx - 1).coerceAtLeast(0) }
                Row(horizontalArrangement = Arrangement.spacedBy(7.dp), verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f).padding(horizontal = 8.dp), ) {
                    Spacer(Modifier.weight(1f))
                    for (i in 0 until total) Box(Modifier.size(if (i == idx) 9.dp else 7.dp).clip(CircleShape).background(if (i == idx) GOLD else CREAM.copy(alpha = 0.22f)))
                    Spacer(Modifier.weight(1f))
                }
                ProjNav("Siguiente →", idx < total - 1, "projector-next") { idx = (idx + 1).coerceAtMost(total - 1) }
            }
        }
    }
}

private suspend fun PointerInputScope.detectTapsLeftRight(onTap: (left: Boolean) -> Unit) {
    detectTapGestures(onTap = { pos: Offset -> onTap(pos.x < size.width / 3f) })
}

@Composable
private fun ProjButton(iconRes: Int, label: String, tag: String, onClick: () -> Unit) {
    Box(
        Modifier.size(40.dp).clip(CircleShape).background(CREAM.copy(alpha = 0.06f)).border(1.dp, CREAM.copy(alpha = 0.2f), CircleShape)
            .pressable(onClick = onClick).testTag(tag),
        contentAlignment = Alignment.Center,
    ) { Icon(painterResource(iconRes), label, tint = CREAM, modifier = Modifier.size(18.dp)) }
}

@Composable
private fun ProjNav(label: String, enabled: Boolean, tag: String, onClick: () -> Unit) {
    Box(
        Modifier.alpha(if (enabled) 1f else 0.3f).clip(CircleShape).background(CREAM.copy(alpha = 0.06f)).border(1.dp, CREAM.copy(alpha = 0.2f), CircleShape)
            .pressable(enabled = enabled, onClick = onClick).padding(horizontal = 12.dp, vertical = 9.dp).testTag(tag),
    ) { Text(label, fontFamily = Inter, fontWeight = FontWeight.Medium, fontSize = 13.6.sp, color = CREAM) }
}
