package pe.pisacoma.iglesia.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pe.pisacoma.iglesia.ui.theme.Inter
import pe.pisacoma.iglesia.ui.theme.LocalChrome

/**
 * The one notice for content that is neither stored nor reachable (the site's offline-save
 * voice): shown by the scaffold over any screen while ContentStore.missing is set.
 */
@Composable
fun MissingContentNotice(onRetry: () -> Unit, onDownloads: () -> Unit, modifier: Modifier = Modifier) {
    val c = LocalChrome.current
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier
            .fillMaxWidth()
            .padding(16.dp)
            .navigationBarsPadding()
            .clip(shape)
            .background(c.background)
            .border(1.dp, c.footerBorder, shape)
            .padding(18.dp)
            .testTag("sin-conexion"),
    ) {
        EyebrowLine("Sin conexión", color = c.bodyText.copy(alpha = 0.7f))
        Spacer(Modifier.height(8.dp))
        Text(
            "Este contenido no está guardado en el teléfono. Conéctate a internet o guárdalo desde Descargas.",
            fontFamily = Inter,
            fontSize = 14.sp,
            lineHeight = 21.sp,
            color = c.bodyText,
        )
        Spacer(Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            NoticeButton("Reintentar", filled = true, onClick = onRetry, modifier = Modifier.testTag("sin-conexion-reintentar"))
            NoticeButton("Descargas", filled = false, onClick = onDownloads, modifier = Modifier.testTag("sin-conexion-descargas"))
        }
    }
}

@Composable
private fun NoticeButton(label: String, filled: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val c = LocalChrome.current
    val shape = RoundedCornerShape(999.dp)
    Text(
        label,
        fontFamily = Inter,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        color = if (filled) c.background else c.bodyText,
        modifier = modifier
            .clip(shape)
            .background(if (filled) c.bodyText else c.background)
            .border(1.dp, c.bodyText.copy(alpha = if (filled) 1f else 0.35f), shape)
            .pressable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 9.dp),
    )
}
