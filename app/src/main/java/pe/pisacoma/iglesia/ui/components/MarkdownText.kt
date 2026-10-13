package pe.pisacoma.iglesia.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pe.pisacoma.iglesia.ui.theme.Fraunces
import pe.pisacoma.iglesia.ui.theme.Inter
import pe.pisacoma.iglesia.ui.theme.Tokens
import pe.pisacoma.iglesia.util.LessonMarkdown
import pe.pisacoma.iglesia.util.MdBlock

/**
 * Renders a lesson day (`.content` on the site): headings in Fraunces, paragraphs in Inter at
 * the chosen size, the memory-verse quote, the backtick prompt box, and Bible references as
 * sage dotted-underline spans that call [onRef] with the reference key.
 */
@Composable
fun MarkdownText(blocks: List<MdBlock>, scale: Float, onRef: (String) -> Unit, modifier: Modifier = Modifier) {
    val base = 16f * scale
    Column(modifier, verticalArrangement = Arrangement.spacedBy(0.dp)) {
        blocks.forEach { b ->
            when (b) {
                is MdBlock.Heading -> {
                    Spacer(Modifier.height(if (b.level <= 3) 26.dp else 22.dp))
                    Text(
                        b.text,
                        fontFamily = Fraunces,
                        fontWeight = if (b.level <= 3) FontWeight.SemiBold else FontWeight.Medium,
                        fontSize = (if (b.level <= 3) base * 1.3f else base * 1.12f).sp,
                        lineHeight = (if (b.level <= 3) base * 1.5f else base * 1.35f).sp,
                        letterSpacing = (-0.015 * base).sp,
                        color = Tokens.inkDeep,
                    )
                    Spacer(Modifier.height(10.dp))
                }
                is MdBlock.Paragraph -> {
                    RefText(b.text, base, onRef)
                    Spacer(Modifier.height((base * 0.9f).dp))
                }
                is MdBlock.Quote -> {
                    val shape = RoundedCornerShape(14.dp)
                    Column(
                        Modifier.fillMaxWidth().clip(shape).background(Color(0xB3FBF9EF)).border(1.dp, Color(0x59B8924F), shape)
                            .padding(horizontal = 18.dp, vertical = 16.dp),
                    ) {
                        if (b.label != null) {
                            EyebrowLine("— ${b.label}", Ochre.label, size = 10.5f)
                            Spacer(Modifier.height(8.dp))
                        }
                        Text(
                            b.text,
                            fontFamily = Fraunces, fontStyle = FontStyle.Italic, fontSize = (base * 1.05f).sp, lineHeight = (base * 1.6f).sp, color = Tokens.forest,
                        )
                    }
                    Spacer(Modifier.height((base * 1.1f).dp))
                }
                is MdBlock.Prompt -> {
                    Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min).padding(vertical = 4.dp)) {
                        Box(Modifier.width(3.dp).fillMaxHeight().background(Tokens.sage))
                        Column(Modifier.clip(RoundedCornerShape(0.dp, 12.dp, 12.dp, 0.dp)).background(Tokens.sageA(0.08f)).padding(horizontal = 16.dp, vertical = 14.dp).fillMaxWidth()) {
                            RefText(b.text, base, onRef, weight = FontWeight.Medium, color = Tokens.forest, family = Fraunces)
                        }
                    }
                    Spacer(Modifier.height((base * 1.1f).dp))
                }
                is MdBlock.Bullet -> {
                    Row(Modifier.padding(bottom = 6.dp)) {
                        Text("•", fontFamily = Inter, fontSize = base.sp, color = Tokens.sage, modifier = Modifier.width(18.dp))
                        RefText(b.text, base, onRef)
                    }
                }
                MdBlock.Rule -> {
                    Spacer(Modifier.height(8.dp))
                    Hairline(0.18f)
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

/** Body text with the reference spans styled and clickable (site `.es-ref` buttons). */
@Composable
private fun RefText(
    text: AnnotatedString,
    base: Float,
    onRef: (String) -> Unit,
    weight: FontWeight = FontWeight.Normal,
    color: Color = Tokens.ink,
    family: androidx.compose.ui.text.font.FontFamily = Inter,
) {
    val styled = remember(text) {
        buildAnnotatedString {
            append(text)
            text.getStringAnnotations(LessonMarkdown.REF_TAG, 0, text.length).forEach { a ->
                addStyle(SpanStyle(color = Tokens.sage, fontWeight = FontWeight.SemiBold, textDecoration = TextDecoration.Underline), a.start, a.end)
            }
        }
    }
    ClickableText(
        text = styled,
        style = TextStyle(fontFamily = family, fontWeight = weight, fontSize = base.sp, lineHeight = (base * 1.72f).sp, color = color),
        onClick = { offset ->
            styled.getStringAnnotations(LessonMarkdown.REF_TAG, offset, offset).firstOrNull()?.let { onRef(it.item) }
        },
        modifier = Modifier.testTag("md-text"),
    )
}
