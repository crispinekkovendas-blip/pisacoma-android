package pe.pisacoma.iglesia.data.net

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The site's diccionario route: GET es.wiktionary.org/api/rest_v1/page/definition/<word>,
 * take `es` | `Spanish` | the first language, 404 → «no encontramos», empty → «no tiene
 * definiciones registradas», network → «Error de red».
 */
class WiktionaryTest {
    private val body = """{"es":[{"partOfSpeech":"Sustantivo femenino","language":"Español","definitions":[{"definition":"Don o favor que se hace <b>sin merecimiento</b>.","examples":["Por la gracia de Dios."]},{"definition":"Perdón o indulto."}]},{"partOfSpeech":"Interjección","definitions":[{"definition":"Se usa para agradecer."}]}]}"""

    @Test fun urlEncodesTheWord() {
        assertEquals("https://es.wiktionary.org/api/rest_v1/page/definition/s%C3%A1bado", Wiktionary.url("sábado"))
    }

    @Test fun parsesSpanishSections() {
        val sections = Wiktionary.parse(body)
        assertEquals(2, sections.size)
        assertEquals("Sustantivo femenino", sections[0].partOfSpeech)
        assertEquals(2, sections[0].definitions.size)
        assertEquals("Don o favor que se hace sin merecimiento.", sections[0].definitions[0].definition) // HTML stripped
        assertEquals(listOf("Por la gracia de Dios."), sections[0].definitions[0].examples)
        assertTrue(sections[1].definitions[0].examples.isEmpty())
    }

    @Test fun fallsBackToFirstLanguage() {
        val s = Wiktionary.parse("""{"Spanish":[{"partOfSpeech":"Verbo","definitions":[{"definition":"x"}]}]}""")
        assertEquals("Verbo", s[0].partOfSpeech)
        val other = Wiktionary.parse("""{"la":[{"partOfSpeech":"Nomen","definitions":[{"definition":"y"}]}]}""")
        assertEquals("Nomen", other[0].partOfSpeech)
        assertTrue(Wiktionary.parse("{}").isEmpty())
        assertTrue(Wiktionary.parse("not json").isEmpty())
    }

    @Test fun outcomesMapToTheSitesMessages() {
        assertEquals("«fulano» — no encontramos esta palabra en el Wiccionario en español.", Wiktionary.notFoundMessage("fulano"))
        assertEquals("«x» no tiene definiciones registradas.", Wiktionary.emptyMessage("x"))
        assertEquals("Error de red. Intenta de nuevo.", Wiktionary.NETWORK_ERROR)
    }

    @Test fun suggestedWordsAreTheSites() {
        assertEquals(listOf("amor", "fe", "esperanza", "gracia"), Wiktionary.SUGGESTED.take(4))
        assertTrue("sábado" in Wiktionary.SUGGESTED)
        assertEquals("fiel", Wiktionary.SUGGESTED.last())
    }
}
