package pe.pisacoma.iglesia.ui.chrome

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pe.pisacoma.iglesia.R
import pe.pisacoma.iglesia.ui.components.pressable
import pe.pisacoma.iglesia.ui.theme.Fraunces
import pe.pisacoma.iglesia.ui.theme.Inter
import pe.pisacoma.iglesia.ui.theme.LocalChrome
import pe.pisacoma.iglesia.ui.theme.Metrics

/**
 * The site's `.site-nav`: brand (symbol + «Pisacoma» / «Iglesia Adventista») on the left,
 * search and hamburger (42 dp, 12 dp radius) on the right. 66 dp tall below the status bar.
 */
@Composable
fun TopBar(
    menuOpen: Boolean,
    onBrand: () -> Unit,
    onSearch: () -> Unit,
    onMenu: () -> Unit,
) {
    val c = LocalChrome.current
    Column(
        Modifier
            .fillMaxWidth()
            .background(c.navBackground)
            .statusBarsPadding(),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .height(Metrics.navHeight.dp)
                .padding(horizontal = Metrics.navPaddingH.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onBrand)
                    .semantics { contentDescription = "Pisacoma, inicio" },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(11.dp),
            ) {
                Icon(
                    painterResource(R.drawable.sda_symbol),
                    contentDescription = null,
                    tint = c.brandMark,
                    modifier = Modifier.size(width = Metrics.brandMarkW.dp, height = Metrics.brandMarkH.dp),
                )
                Column {
                    // .brand-text strong — Fraunces 1.1rem; the sub line is hidden ≤840px on the
                    // site, so the phone shows only the name.
                    Text(
                        "Pisacoma",
                        fontFamily = Fraunces,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.6.sp,
                        lineHeight = 19.sp,
                        color = c.brandStrong,
                    )
                }
            }
            Spacer(Modifier.weight(1f))
            NavButton(iconRes = R.drawable.ic_nav_search, label = "Buscar en todo el sitio", onClick = onSearch, testTag = "nav-search")
            Spacer(Modifier.width(8.dp))
            NavButton(
                iconRes = if (menuOpen) R.drawable.ic_nav_close else R.drawable.ic_nav_menu,
                label = if (menuOpen) "Cerrar menú" else "Abrir menú",
                onClick = onMenu,
                testTag = "nav-menu",
            )
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(c.navBorder))
    }
}

@Composable
private fun NavButton(iconRes: Int, label: String, onClick: () -> Unit, testTag: String) {
    val c = LocalChrome.current
    Box(
        Modifier
            .size(Metrics.navButton.dp)
            .clip(RoundedCornerShape(Metrics.navButtonRadius.dp))
            .background(c.navButtonBg)
            .border(1.dp, c.navButtonBorder, RoundedCornerShape(Metrics.navButtonRadius.dp))
            .pressable(onClick = onClick)
            .testTag(testTag)
            .semantics { contentDescription = label },
        contentAlignment = Alignment.Center,
    ) {
        Icon(painterResource(iconRes), contentDescription = null, tint = c.navButtonFg, modifier = Modifier.size(20.dp))
    }
}

/** Small uppercase Inter label used across the chrome (.brand-text span, .mm-desc, .mm-foot span). */
@Composable
fun Eyebrow(text: String, color: Color, size: Float = 10.5f, tracking: Float = 1.6f, weight: FontWeight = FontWeight.SemiBold) {
    Text(
        text.uppercase(),
        fontFamily = Inter,
        fontSize = size.sp,
        fontWeight = weight,
        letterSpacing = tracking.sp,
        lineHeight = (size + 2).sp,
        color = color,
    )
}
