package pe.pisacoma.iglesia.feature.descargas

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import pe.pisacoma.iglesia.App
import pe.pisacoma.iglesia.data.content.ContentMode
import pe.pisacoma.iglesia.data.content.DownloadState
import pe.pisacoma.iglesia.data.content.formatMb
import pe.pisacoma.iglesia.ui.chrome.SiteFooter
import pe.pisacoma.iglesia.ui.components.EyebrowLine
import pe.pisacoma.iglesia.ui.components.Hairline
import pe.pisacoma.iglesia.ui.components.Lede
import pe.pisacoma.iglesia.ui.components.PageTitle
import pe.pisacoma.iglesia.ui.components.pressable
import pe.pisacoma.iglesia.ui.components.titleWithEm
import pe.pisacoma.iglesia.ui.theme.Fraunces
import pe.pisacoma.iglesia.ui.theme.Inter
import pe.pisacoma.iglesia.ui.theme.Tokens

/**
 * /descargas: what to keep on the phone. On the first open it is the only screen (`bare`, no
 * top bar) and «Continuar» leads on; later it is reached from the menu to change the choice,
 * complete a download or clear the store. Copy in the site's offline-save voice.
 */
@Composable
fun DescargasScreen(modifier: Modifier, bare: Boolean, onContinue: () -> Unit) {
    val vm: DescargasViewModel = viewModel { DescargasViewModel(App.content, App.downloader, App.repos.userState) }
    val s by vm.state.collectAsState()
    LaunchedEffect(Unit) { vm.refresh() }

    LazyColumn(modifier.testTag("descargas")) {
        item {
            Column(Modifier.padding(horizontal = 20.dp).let { if (bare) it.statusBarsPadding() else it }) {
                Spacer(Modifier.height(if (bare) 36.dp else 44.dp))
                EyebrowLine("— Guardar en el teléfono")
                Spacer(Modifier.height(18.dp))
                PageTitle(titleWithEm("¿Qué quieres guardar", "en este teléfono", tail = "?"))
                Spacer(Modifier.height(22.dp))
                Lede("Lo que guardes se lee sin señal. Puedes cambiarlo después desde el menú, en Descargas.")
                Spacer(Modifier.height(26.dp))
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    s.options.forEachIndexed { i, o -> OptionRow(i + 1, o, selected = o.mode == s.current) { vm.choose(o.mode) } }
                }
                if (s.download !is DownloadState.Idle) {
                    Spacer(Modifier.height(26.dp))
                    ProgressBlock(s.download, onRetry = vm::complete)
                }
                if (!bare && s.current != null) {
                    Spacer(Modifier.height(26.dp))
                    Hairline()
                    Spacer(Modifier.height(18.dp))
                    if (s.storedText.isNotEmpty()) {
                        Text(s.storedText, fontFamily = Inter, fontSize = 14.sp, color = Tokens.sageText, modifier = Modifier.testTag("descargas-guardado"))
                        Spacer(Modifier.height(14.dp))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        if (s.missingCount > 0 && s.download !is DownloadState.Running) {
                            Pill("Completar", filled = true, onClick = vm::complete, modifier = Modifier.testTag("descargas-completar"))
                        }
                        if (s.storedText.isNotEmpty()) {
                            Pill("Borrar lo descargado", filled = false, onClick = vm::clear, modifier = Modifier.testTag("descargas-borrar"))
                        }
                    }
                }
                if (bare) {
                    Spacer(Modifier.height(30.dp))
                    ContinueButton(enabled = s.canContinue, onClick = onContinue)
                }
                Spacer(Modifier.height(40.dp))
            }
        }
        item { SiteFooter() }
    }
}

@Composable
private fun OptionRow(index: Int, o: DescargasUi.Option, selected: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(14.dp)
    Column(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(if (selected) Tokens.sageA(0.12f) else Color.Transparent)
            .border(1.dp, Tokens.sageA(if (selected) 0.4f else 0.2f), shape)
            .pressable(scale = 0.985f, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .testTag("modo-${o.mode.value}"),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "0$index",
                fontFamily = Fraunces,
                fontSize = 13.sp,
                color = Tokens.sageSoft,
                modifier = Modifier.padding(end = 12.dp),
            )
            Text(
                o.mode.label,
                fontFamily = Fraunces,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                color = Tokens.inkDeep,
                modifier = Modifier.weight(1f),
            )
            if (o.size.isNotEmpty()) {
                Text(o.size, fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Tokens.sageText)
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(o.desc, fontFamily = Inter, fontSize = 13.5.sp, lineHeight = 20.sp, color = Tokens.sageText)
        if (selected) {
            Spacer(Modifier.height(8.dp))
            EyebrowLine("Actual", size = 10f)
        }
    }
}

@Composable
private fun ProgressBlock(d: DownloadState, onRetry: () -> Unit) {
    val message: String
    val progress: Float
    when (d) {
        is DownloadState.Running -> {
            val failed = if (d.failed > 0) " · ${d.failed} fallaron" else ""
            message = "Guardando ${d.doneFiles} de ${d.totalFiles} archivos · ${formatMb(d.doneBytes)} de ${formatMb(d.totalBytes)}$failed"
            progress = if (d.totalBytes > 0) d.doneBytes.toFloat() / d.totalBytes else 0f
        }
        is DownloadState.Done -> { message = "Guardado en este teléfono · ${formatMb(d.totalBytes)}"; progress = 1f }
        is DownloadState.Error -> { message = "No se pudo guardar todo. Revisa la señal e inténtalo otra vez."; progress = 0f }
        DownloadState.Idle -> { message = ""; progress = 0f }
    }
    Column(Modifier.fillMaxWidth().testTag("descarga-progreso")) {
        Text(message, fontFamily = Inter, fontSize = 14.sp, lineHeight = 21.sp, color = Tokens.forest)
        Spacer(Modifier.height(10.dp))
        LinearProgressIndicator(
            progress = { progress },
            color = Tokens.sage,
            trackColor = Tokens.sageA(0.15f),
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)).semantics { },
        )
        if (d is DownloadState.Error) {
            Spacer(Modifier.height(14.dp))
            Pill("Reintentar", filled = true, onClick = onRetry, modifier = Modifier.testTag("descarga-reintentar"))
        }
    }
}

@Composable
private fun ContinueButton(enabled: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(999.dp)
    Box(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(if (enabled) Tokens.forest else Tokens.sageA(0.18f))
            .pressable(enabled = enabled, onClick = onClick)
            .padding(vertical = 15.dp)
            .testTag("descargas-continuar"),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            "Continuar",
            fontFamily = Inter,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp,
            color = if (enabled) Tokens.cream else Tokens.sageText,
        )
    }
}

@Composable
private fun Pill(label: String, filled: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(999.dp)
    Text(
        label,
        fontFamily = Inter,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        color = if (filled) Tokens.cream else Tokens.forest,
        modifier = modifier
            .clip(shape)
            .background(if (filled) Tokens.forest else Color.Transparent)
            .border(1.dp, if (filled) Tokens.forest else Tokens.sageA(0.35f), shape)
            .pressable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 9.dp),
    )
}
