package pe.pisacoma.iglesia.ui.chrome

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pe.pisacoma.iglesia.R
import pe.pisacoma.iglesia.nav.NAV_ITEMS
import pe.pisacoma.iglesia.nav.NavItem
import pe.pisacoma.iglesia.nav.Route
import pe.pisacoma.iglesia.nav.quarterLabel
import pe.pisacoma.iglesia.ui.components.pressable
import pe.pisacoma.iglesia.ui.theme.Fraunces
import pe.pisacoma.iglesia.ui.theme.Metrics
import pe.pisacoma.iglesia.ui.theme.Tokens

/**
 * The site's mobile menu (`.mm`): a scrim and a cream panel sliding in from the right with
 * the eleven destinations, numbered, with their icon tile and description, and the circle
 * symbol in the foot. Always cream, whatever the route's skin (as on the site).
 */
@Composable
fun FullMenu(open: Boolean, current: Route?, onNavigate: (Route) -> Unit, onClose: () -> Unit) {
    AnimatedVisibility(open, enter = fadeIn(tween(280)), exit = fadeOut(tween(280))) {
        Box(
            Modifier
                .fillMaxSize()
                .background(Color(0x800D1117)) // .mm-scrim rgba(13,17,23,.5)
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClose)
                .semantics { contentDescription = "Cerrar menú" },
        )
    }
    AnimatedVisibility(
        open,
        enter = slideInHorizontally(tween(320)) { it },
        exit = slideOutHorizontally(tween(320)) { it },
        modifier = Modifier.fillMaxSize(),
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.CenterEnd) {
            val screenW = LocalConfiguration.current.screenWidthDp
            val panelW = minOf(440, (screenW * 0.92f).toInt())
            Column(
                Modifier
                    .width(panelW.dp)
                    .fillMaxHeight()
                    .background(
                        Brush.verticalGradient(listOf(Tokens.menuTop, Tokens.menuBottom)),
                    )
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {}
                    .statusBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(start = Metrics.menuPaddingH.dp, end = Metrics.menuPaddingH.dp, top = 24.dp, bottom = 24.dp)
                    .testTag("menu-panel"),
            ) {
                // close X — .mm-close 40dp circle, top right
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Box(
                        Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Tokens.sageA(0.08f))
                            .border(1.dp, Tokens.sageA(0.20f), CircleShape)
                            .pressable(onClick = onClose)
                            .semantics { contentDescription = "Cerrar menú" },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(painterResource(R.drawable.ic_nav_close), null, tint = Tokens.forest, modifier = Modifier.size(18.dp))
                    }
                }
                Spacer(Modifier.height(12.dp))
                // .mm-head
                Eyebrow("— Navegación · ${NAV_ITEMS.size} destinos", Tokens.sage, size = 10.5f, tracking = 2.4f, weight = FontWeight.Bold)
                Spacer(Modifier.height(14.dp))
                Text(
                    buildAnnotatedString {
                        append("¿A dónde ")
                        withStyle(SpanStyle(fontStyle = FontStyle.Italic, color = Tokens.sage, fontWeight = FontWeight.Normal)) { append("vamos") }
                        append("?")
                    },
                    fontFamily = Fraunces,
                    fontWeight = FontWeight.Medium,
                    fontSize = 27.2.sp, // 1.7rem ≤480px
                    lineHeight = 28.sp,
                    letterSpacing = (-0.025 * 27.2).sp,
                    color = Tokens.inkDeep,
                )
                Spacer(Modifier.height(22.dp))
                // .mm-list
                val now = System.currentTimeMillis()
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    NAV_ITEMS.forEachIndexed { i, item ->
                        val active = when (val r = item.route) {
                            Route.Home -> current == Route.Home
                            else -> current != null && current.base.startsWith(r.base)
                        }
                        MenuRow(
                            index = i + 1,
                            item = item,
                            desc = if (item.route == Route.Escuela) quarterLabel(now) else item.desc,
                            active = active,
                            onClick = { onNavigate(item.route) },
                        )
                    }
                }
                Spacer(Modifier.height(22.dp))
                // .mm-foot
                Box(Modifier.fillMaxWidth().height(1.dp).background(Tokens.sageA(0.18f)))
                Spacer(Modifier.height(22.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Icon(painterResource(R.drawable.sda_symbol_circle), null, tint = Tokens.sage, modifier = Modifier.size(40.dp))
                    Column {
                        Text("Pisacoma · 3.940 m", fontFamily = Fraunces, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Tokens.forest)
                        Spacer(Modifier.height(3.dp))
                        Eyebrow("Chucuito · Puno · MLT", Tokens.sageText)
                    }
                }
            }
        }
    }
}

@Composable
private fun MenuRow(index: Int, item: NavItem, desc: String, active: Boolean, onClick: () -> Unit) {
    val rowShape = RoundedCornerShape(Metrics.menuRowRadius.dp)
    Row(
        Modifier
            .fillMaxWidth()
            .clip(rowShape)
            .background(
                if (active) Brush.horizontalGradient(listOf(Tokens.sageA(0.16f), Tokens.sageA(0.02f))) else Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent)),
            )
            .pressable(scale = 0.985f, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 11.dp)
            .testTag("menu-${item.label}"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            index.toString().padStart(2, '0'),
            fontFamily = Fraunces,
            fontStyle = FontStyle.Italic,
            fontWeight = FontWeight.Bold,
            fontSize = 15.2.sp,
            color = if (active) Tokens.forest else Tokens.sageSoft,
            modifier = Modifier.width(22.dp),
        )
        val tileShape = RoundedCornerShape(Metrics.menuIconRadius.dp)
        Box(
            Modifier
                .size(Metrics.menuIcon.dp)
                .clip(tileShape)
                .background(if (active) Tokens.forest else Tokens.leafA(0.10f))
                .border(1.dp, if (active) Tokens.forest else Tokens.sageA(0.18f), tileShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(painterResource(item.iconRes), null, tint = if (active) Tokens.cream else Tokens.sage, modifier = Modifier.size(20.dp))
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                item.label,
                fontFamily = Fraunces,
                fontWeight = if (active) FontWeight.Normal else FontWeight.Medium,
                fontStyle = if (active) FontStyle.Italic else FontStyle.Normal,
                fontSize = 18.9.sp, // 1.18rem ≤480px
                lineHeight = 20.sp,
                letterSpacing = (-0.022 * 18.9).sp,
                color = if (active) Tokens.forest else Tokens.ink,
            )
            if (desc.isNotEmpty()) Eyebrow(desc, Tokens.sageText, size = 10f)
        }
        if (active) Icon(painterResource(R.drawable.ic_nav_arrow), null, tint = Tokens.sage, modifier = Modifier.size(20.dp))
    }
}
