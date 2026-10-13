package pe.pisacoma.iglesia.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The site's lesson pipeline ([leccion]/index.tsx): front matter → day title; a short
 * `backtick` span → a highlighted prompt box; the rest through marked (gfm); Bible references
 * wrapped as tappable buttons. The port keeps the same blocks.
 */
class LessonMarkdownTest {
    private val day1 = """
        |---
        |title:  El ministerio de Pablo en Corinto
        |date:   27/06/2026
        |---
        |
        |### Lee para el estudio de esta semana
        |1 Corintios 1: 1; Gálatas 1: 1; Hechos 17: 16-34.
        |
        |> <p>Para memorizar</p>
        |> «Entonces el Señor dijo a Pablo en una visión nocturna» (Hech. 18: 9-10).
        |
        |El gran misionero **William Carey** solía decir que reparaba zapatos (Hech. 18: 1-3).
        |
        |`Lee 1 Corintios 1: 1 y Romanos 1: 1. ¿Qué dos elementos destacan?`
        |
        |---
        |
        |#### Comentarios Elena G.W
        |
        |Aunque Pablo se sentó como alumno.
        |""".trimMargin()

    @Test fun frontMatterGivesTheDayTitle() {
        val parsed = LessonMarkdown.parseDay(day1)
        assertEquals("El ministerio de Pablo en Corinto", parsed.title)
        assertTrue(parsed.body.startsWith("### Lee para"))
    }

    @Test fun noFrontMatterKeepsTheBody() {
        val parsed = LessonMarkdown.parseDay("Hola")
        assertEquals("", parsed.title)
        assertEquals("Hola", parsed.body)
    }

    @Test fun blocksInOrder() {
        val blocks = LessonMarkdown.parse(LessonMarkdown.parseDay(day1).body)
        val kinds = blocks.map { it::class.simpleName }
        assertEquals(
            listOf("Heading", "Paragraph", "Quote", "Paragraph", "Prompt", "Rule", "Heading", "Paragraph"),
            kinds,
        )
        assertEquals(3, (blocks[0] as MdBlock.Heading).level)
        assertEquals("Lee para el estudio de esta semana", (blocks[0] as MdBlock.Heading).text)
        assertEquals(4, (blocks[6] as MdBlock.Heading).level)
    }

    @Test fun quoteKeepsItsLabelAndText() {
        val q = LessonMarkdown.parse(LessonMarkdown.parseDay(day1).body).filterIsInstance<MdBlock.Quote>().single()
        assertEquals("Para memorizar", q.label)
        assertTrue(q.text.text.startsWith("«Entonces el Señor"))
    }

    @Test fun promptIsTheBacktickQuestion() {
        val p = LessonMarkdown.parse(LessonMarkdown.parseDay(day1).body).filterIsInstance<MdBlock.Prompt>().single()
        assertEquals("Lee 1 Corintios 1: 1 y Romanos 1: 1. ¿Qué dos elementos destacan?", p.text.text)
    }

    @Test fun bibleReferencesAreAnnotated() {
        val blocks = LessonMarkdown.parse(LessonMarkdown.parseDay(day1).body)
        val para = blocks[1] as MdBlock.Paragraph
        val refs = para.text.getStringAnnotations(LessonMarkdown.REF_TAG, 0, para.text.length)
        assertEquals(3, refs.size)
        assertEquals("46|1:1-1", refs[0].item)   // 1 Corintios 1:1
        assertEquals("44|17:16-34", refs[2].item)
        val quote = blocks[2] as MdBlock.Quote
        assertEquals("44|18:9-10", quote.text.getStringAnnotations(LessonMarkdown.REF_TAG, 0, quote.text.length).single().item)
    }

    @Test fun boldIsStyled() {
        val para = LessonMarkdown.parse(LessonMarkdown.parseDay(day1).body)[3] as MdBlock.Paragraph
        assertTrue(para.text.text.contains("William Carey"))
        assertTrue(para.text.spanStyles.any { it.item.fontWeight == androidx.compose.ui.text.font.FontWeight.Bold })
        assertTrue(!para.text.text.contains("**"))
    }

    @Test fun refKeyRoundTrip() {
        val sections = LessonMarkdown.decodeRef("43|3:16-16;5:4-8")
        assertEquals(43, sections.first)
        assertEquals(listOf(RefSection(3, 16, 16), RefSection(5, 4, 8)), sections.second)
    }
}
