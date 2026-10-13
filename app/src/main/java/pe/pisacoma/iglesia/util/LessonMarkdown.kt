package pe.pisacoma.iglesia.util

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight

/** One rendered block of a lesson day (the subset of Markdown the quarterly uses). */
sealed class MdBlock {
    data class Heading(val level: Int, val text: String) : MdBlock()
    data class Paragraph(val text: AnnotatedString) : MdBlock()
    /** A `> ` block; the site's quarterly starts it with `<p>Para memorizar</p>` as a label. */
    data class Quote(val label: String?, val text: AnnotatedString) : MdBlock()
    /** The study question the quarterly writes between backticks (site `.es-prompt`). */
    data class Prompt(val text: AnnotatedString) : MdBlock()
    data class Bullet(val text: AnnotatedString) : MdBlock()
    data object Rule : MdBlock()
}

/**
 * The site's lesson pipeline ([leccion]/index.tsx): front matter → day title, short backtick
 * spans → prompt boxes, marked (gfm) for the rest, Bible references wrapped as tappable spans.
 * References are annotated with [REF_TAG] and the key `book|ch:from-to[;ch:from-to]`.
 */
object LessonMarkdown {
    const val REF_TAG = "ref"

    data class Day(val title: String, val body: String)

    private val FRONT = Regex("^---\\s*\\n([\\s\\S]*?)\\n---\\s*\\n?([\\s\\S]*)$")
    private val TITLE = Regex("^title:\\s*(.+)$", RegexOption.MULTILINE)

    fun parseDay(raw: String): Day {
        val m = FRONT.find(raw) ?: return Day("", raw)
        val title = TITLE.find(m.groupValues[1])?.groupValues?.get(1)?.trim() ?: ""
        return Day(title, m.groupValues[2])
    }

    fun refKey(ref: ParsedRef): String = "${ref.bookNum}|" + ref.sections.joinToString(";") { "${it.chapter}:${it.from}-${it.to}" }

    fun decodeRef(key: String): Pair<Int, List<RefSection>> {
        val (book, rest) = key.split("|", limit = 2)
        val sections = rest.split(";").map { s ->
            val (ch, span) = s.split(":", limit = 2)
            val (from, to) = span.split("-", limit = 2)
            RefSection(ch.toInt(), from.toInt(), to.toInt())
        }
        return book.toInt() to sections
    }

    private val PROMPT = Regex("`([^`\\n][^`]*)`")
    private const val PROMPT_MARK = "\u0001PROMPT\u0001"

    fun parse(body: String): List<MdBlock> {
        // Short backtick spans become their own block, like the site's preprocess().
        val text = PROMPT.replace(body) { m ->
            val inner = m.groupValues[1]
            if (inner.length < 200) "\n\n$PROMPT_MARK${inner.trim()}$PROMPT_MARK\n\n" else m.value
        }
        val blocks = mutableListOf<MdBlock>()
        val lines = text.replace("\r\n", "\n").split("\n")
        var i = 0
        val para = StringBuilder()
        fun flushPara() {
            val p = para.toString().trim()
            para.clear()
            if (p.isEmpty()) return
            if (p.startsWith(PROMPT_MARK) && p.endsWith(PROMPT_MARK)) {
                blocks += MdBlock.Prompt(inline(p.removeSurrounding(PROMPT_MARK)))
            } else blocks += MdBlock.Paragraph(inline(p))
        }
        while (i < lines.size) {
            val line = lines[i]
            val t = line.trim()
            when {
                t.isEmpty() -> { flushPara(); i++ }
                t == "---" || t == "***" -> { flushPara(); blocks += MdBlock.Rule; i++ }
                t.startsWith("#") -> {
                    flushPara()
                    val level = t.takeWhile { it == '#' }.length.coerceIn(1, 6)
                    blocks += MdBlock.Heading(level, stripTags(t.drop(level).trim()))
                    i++
                }
                t.startsWith(">") -> {
                    flushPara()
                    val quoteLines = mutableListOf<String>()
                    while (i < lines.size && lines[i].trim().startsWith(">")) {
                        quoteLines += lines[i].trim().removePrefix(">").trim()
                        i++
                    }
                    var label: String? = null
                    val first = quoteLines.firstOrNull() ?: ""
                    val pm = Regex("^<p>(.*?)</p>$").find(first)
                    val bodyLines = if (pm != null) { label = pm.groupValues[1].trim(); quoteLines.drop(1) } else quoteLines
                    blocks += MdBlock.Quote(label, inline(bodyLines.joinToString(" ").trim()))
                }
                t.startsWith("- ") || t.startsWith("* ") -> {
                    flushPara()
                    blocks += MdBlock.Bullet(inline(t.drop(2).trim()))
                    i++
                }
                else -> { if (para.isNotEmpty()) para.append(' '); para.append(t); i++ }
            }
        }
        flushPara()
        return blocks
    }

    private fun stripTags(s: String) = s.replace(Regex("<[^>]+>"), "")

    private val BOLD = Regex("\\*\\*(.+?)\\*\\*")
    private val ITALIC = Regex("(?<![*\\w])\\*(?!\\*)(.+?)\\*(?!\\*)|(?<!\\w)_(.+?)_(?!\\w)")

    /** Inline markdown (bold, italic, `<br>`, tags stripped) plus Bible-reference annotations. */
    fun inline(src: String): AnnotatedString {
        // 1. resolve bold/italic into plain text + style ranges
        data class Span(val start: Int, val end: Int, val style: SpanStyle)
        val spans = mutableListOf<Span>()
        var s = src.replace("<br>", "\n").replace("<br/>", "\n").replace("<br />", "\n")
        s = stripTags(s).replace("&nbsp;", " ").replace("&amp;", "&")
        val out = StringBuilder()
        val matches = (BOLD.findAll(s).map { it to true } + ITALIC.findAll(s).map { it to false }).sortedBy { it.first.range.first }
        var last = 0
        for ((m, bold) in matches) {
            if (m.range.first < last) continue
            out.append(s, last, m.range.first)
            val inner = m.groupValues.drop(1).firstOrNull { it.isNotEmpty() } ?: ""
            val start = out.length
            out.append(inner)
            spans += Span(start, out.length, if (bold) SpanStyle(fontWeight = FontWeight.Bold) else SpanStyle(fontStyle = FontStyle.Italic))
            last = m.range.last + 1
        }
        out.append(s, last, s.length)
        val plain = out.toString()
        // 2. references over the plain text
        return buildAnnotatedString {
            append(plain)
            for (sp in spans) addStyle(sp.style, sp.start, sp.end)
            for (ref in BibleRefs.findRefs(plain)) {
                addStringAnnotation(REF_TAG, refKey(ref), ref.start, ref.end)
            }
        }
    }
}
