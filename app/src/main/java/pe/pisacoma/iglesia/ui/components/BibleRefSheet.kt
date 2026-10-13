package pe.pisacoma.iglesia.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pe.pisacoma.iglesia.data.BibleRepository
import pe.pisacoma.iglesia.ui.theme.Fraunces
import pe.pisacoma.iglesia.ui.theme.Inter
import pe.pisacoma.iglesia.ui.theme.Tokens
import pe.pisacoma.iglesia.util.BibleRefs
import pe.pisacoma.iglesia.util.LessonMarkdown
import pe.pisacoma.iglesia.util.RefSection

/**
 * The lesson page's verse popup (`.popup-card`): book + chapter:verses tag, the reference as
 * written, and the verses from the bundled Reina-Valera 1960, one section per `;` span.
 */
@Composable
fun BibleRefSheet(refKey: String, display: String, bible: BibleRepository, onClose: () -> Unit) {
    val (book, sections) = remember(refKey) { LessonMarkdown.decodeRef(refKey) }
    var loaded by remember(refKey) { mutableStateOf<List<Pair<RefSection, List<Pair<Int, String>>>>?>(null) }
    LaunchedEffect(refKey) {
        loaded = sections.map { s -> s to bible.verses(book, s.chapter, s.from, s.to) }
    }
    val state = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = onClose, sheetState = state, containerColor = Tokens.cream, modifier = Modifier.testTag("bible-sheet")) {
        Column(Modifier.padding(horizontal = 22.dp).padding(bottom = 24.dp).navigationBarsPadding().verticalScroll(rememberScrollState())) {
            EyebrowLine(
                BibleRefs.bookName(book) + " " + sections.joinToString("; ") { "${it.chapter}:${it.from}" + (if (it.to != it.from) "–${it.to}" else "") },
                Tokens.sage, size = 10.5f,
            )
            Spacer(Modifier.height(6.dp))
            Text(display, fontFamily = Fraunces, fontWeight = FontWeight.SemiBold, fontSize = 22.sp, color = Tokens.inkDeep)
            Spacer(Modifier.height(16.dp))
            Hairline(0.18f)
            Spacer(Modifier.height(16.dp))
            val data = loaded
            when {
                data == null -> Text("Cargando versículo…", fontFamily = Inter, fontSize = 14.sp, color = Tokens.sageText)
                data.all { it.second.isEmpty() } -> Text("Versículo no encontrado.", fontFamily = Inter, fontSize = 14.sp, color = Tokens.sageText)
                else -> data.forEach { (s, verses) ->
                    if (data.size > 1) {
                        EyebrowLine("Cap. ${s.chapter}:${s.from}" + (if (s.to != s.from) "–${s.to}" else ""), Tokens.sageText, size = 10f)
                        Spacer(Modifier.height(8.dp))
                    }
                    verses.forEach { (n, text) ->
                        Row(Modifier.fillMaxWidth().padding(bottom = 10.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Top) {
                            Text(n.toString(), fontFamily = Fraunces, fontStyle = FontStyle.Italic, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Ochre.label, modifier = Modifier.width(24.dp).padding(top = 3.dp))
                            Text(text, fontFamily = Fraunces, fontSize = 17.sp, lineHeight = 28.sp, color = Tokens.ink)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}
