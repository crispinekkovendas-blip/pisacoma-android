package pe.pisacoma.iglesia.ui.components

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pe.pisacoma.iglesia.R
import pe.pisacoma.iglesia.audio.AudioUrls
import pe.pisacoma.iglesia.ui.theme.Inter
import pe.pisacoma.iglesia.ui.theme.Tokens

/**
 * «Compartir» (site components/share-button.tsx): the phone's share sheet with the message and
 * the site link, so WhatsApp, Messenger and the rest open exactly as from the website.
 */
@Composable
fun ShareButton(title: String, text: String, path: String, label: String = "Compartir", modifier: Modifier = Modifier) {
    val ctx = LocalContext.current
    ToolPill(iconRes = R.drawable.ic_share, label = label, modifier = modifier.testTag("share"), testTag = "share") {
        val url = AudioUrls.SITE + path
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_TEXT, "$text\n$url")
        }
        ctx.startActivity(Intent.createChooser(send, label))
    }
}

/** `.open-btn` / `.share-pill`: a cream pill with a 16 dp icon and Inter 500 label. */
@Composable
fun ToolPill(iconRes: Int, label: String, modifier: Modifier = Modifier, testTag: String = "", onClick: () -> Unit) {
    Row(
        modifier
            .clip(CircleShape)
            .background(Color(0xCCFBF9EF))
            .border(1.dp, Color(0x472C4128), CircleShape)
            .pressable(scale = 0.97f, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 9.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(painterResource(iconRes), null, tint = Tokens.forest, modifier = Modifier.size(16.dp))
        Text(label, fontFamily = Inter, fontWeight = FontWeight.Medium, fontSize = 14.4.sp, color = Tokens.forest)
    }
}
