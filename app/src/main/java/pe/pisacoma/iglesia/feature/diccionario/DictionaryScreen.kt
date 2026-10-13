package pe.pisacoma.iglesia.feature.diccionario

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import pe.pisacoma.iglesia.data.net.DictResult
import pe.pisacoma.iglesia.data.net.DictSection
import pe.pisacoma.iglesia.data.net.Wiktionary
import pe.pisacoma.iglesia.nav.Route
import pe.pisacoma.iglesia.ui.chrome.SiteFooter
import pe.pisacoma.iglesia.ui.components.EyebrowLine
import pe.pisacoma.iglesia.ui.components.Hairline
import pe.pisacoma.iglesia.ui.components.Lede
import pe.pisacoma.iglesia.ui.components.Ochre
import pe.pisacoma.iglesia.ui.components.PageTitle
import pe.pisacoma.iglesia.ui.components.SearchField
import pe.pisacoma.iglesia.ui.components.pressable
import pe.pisacoma.iglesia.ui.components.titleWithEm
import pe.pisacoma.iglesia.ui.theme.Fraunces
import pe.pisacoma.iglesia.ui.theme.Inter
import pe.pisacoma.iglesia.ui.theme.Tokens

/** The site's /diccionario: a word → Wiktionary definitions, suggested words, a short history. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DictionaryScreen(modifier: Modifier, go: (Route) -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    var searched by rememberSaveable { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var sections by remember { mutableStateOf<List<DictSection>?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    val history = remember { mutableStateListOf<String>() }
    val scope = rememberCoroutineScope()

    fun search(term: String) {
        val word = term.trim().lowercase()
        if (word.isEmpty()) return
        searched = word; query = word; loading = true; error = null; sections = null
        scope.launch {
            when (val r = Wiktionary.lookup(word)) {
                is DictResult.Found -> { sections = r.sections; history.remove(word); history.add(0, word); while (history.size > 6) history.removeAt(history.lastIndex) }
                is DictResult.Error -> error = r.message
            }
            loading = false
        }
    }

    Column(modifier.verticalScroll(rememberScrollState())) {
        Column(Modifier.padding(horizontal = 20.dp)) {
            Spacer(Modifier.height(44.dp))
            EyebrowLine("— Diccionario · Wiccionario en español")
            Spacer(Modifier.height(18.dp))
            PageTitle(titleWithEm("Cada palabra\ntiene su", "raíz", tail = "."))
            Spacer(Modifier.height(22.dp))
            Lede("Busca cualquier palabra en español — bíblica, teológica o cotidiana. Las definiciones vienen del Wiccionario libre y se actualizan a diario.")
            Spacer(Modifier.height(26.dp))
            SearchField(query, { query = it }, "amor, gracia, sábado, redención…", testTag = "dict-input", onSearch = { search(query) }, onClear = { query = ""; sections = null; error = null; searched = "" })
            Spacer(Modifier.height(10.dp))
            Box(Modifier.clip(CircleShape).background(Tokens.forest).pressable(scale = 0.97f) { search(query) }.padding(horizontal = 18.dp, vertical = 10.dp).testTag("dict-search")) {
                Text("Buscar", fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Tokens.cream)
            }
            Spacer(Modifier.height(22.dp))
            if (history.isNotEmpty()) {
                EyebrowLine("— Recientes", Tokens.sageText, size = 10.5f)
                Spacer(Modifier.height(8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    history.forEach { w -> Chip(w) { search(w) } }
                }
                Spacer(Modifier.height(22.dp))
            }
            when {
                loading -> Text("Buscando «$searched»…", fontFamily = Fraunces, fontStyle = FontStyle.Italic, fontSize = 14.sp, color = Tokens.sageText, modifier = Modifier.testTag("dict-loading"))
                error != null -> Text(error!!, fontFamily = Fraunces, fontStyle = FontStyle.Italic, fontSize = 15.sp, lineHeight = 23.sp, color = Color(0xFF8A3A2A), modifier = Modifier.testTag("dict-error"))
                sections != null -> {
                    Text(searched, fontFamily = Fraunces, fontWeight = FontWeight.SemiBold, fontSize = 34.sp, color = Tokens.inkDeep, modifier = Modifier.testTag("dict-word"))
                    Spacer(Modifier.height(14.dp))
                    sections!!.forEach { sec ->
                        EyebrowLine(sec.partOfSpeech, Ochre.label, size = 10.5f)
                        Spacer(Modifier.height(10.dp))
                        sec.definitions.forEachIndexed { i, d ->
                            Row(Modifier.padding(bottom = 12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text((i + 1).toString(), fontFamily = Fraunces, fontStyle = FontStyle.Italic, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Ochre.numeral, modifier = Modifier.width(22.dp))
                                Column {
                                    Text(d.definition, fontFamily = Inter, fontSize = 15.5.sp, lineHeight = 24.sp, color = Tokens.ink)
                                    d.examples.forEach { ex -> Text("«$ex»", fontFamily = Fraunces, fontStyle = FontStyle.Italic, fontSize = 14.sp, lineHeight = 21.sp, color = Tokens.sageText, modifier = Modifier.padding(top = 4.dp)) }
                                }
                            }
                        }
                        Spacer(Modifier.height(10.dp)); Hairline(0.14f); Spacer(Modifier.height(18.dp))
                    }
                    Text("Definiciones del Wiccionario en español · CC BY-SA", fontFamily = Inter, fontSize = 11.5.sp, color = Tokens.sageText)
                }
                else -> {
                    EyebrowLine("— Palabras sugeridas", Tokens.sageText, size = 10.5f)
                    Spacer(Modifier.height(10.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Wiktionary.SUGGESTED.forEach { w -> Chip(w) { search(w) } }
                    }
                }
            }
            Spacer(Modifier.height(50.dp))
        }
        SiteFooter()
    }
}

@Composable
private fun Chip(label: String, onClick: () -> Unit) {
    Box(Modifier.clip(CircleShape).background(Color(0x99FFFFFF)).border(1.dp, Tokens.sageA(0.22f), CircleShape).pressable(scale = 0.97f, onClick = onClick).padding(horizontal = 12.dp, vertical = 7.dp).testTag("word-$label")) {
        Text(label, fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 12.5.sp, color = Tokens.forest)
    }
}
