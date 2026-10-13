package pe.pisacoma.iglesia.feature.buscar

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import pe.pisacoma.iglesia.data.FileAssetSource
import pe.pisacoma.iglesia.data.SearchRepository
import java.io.File

/** Port of the site's utils/search.ts over the bundled index: same scores, same rows, same hrefs. */
class SearchTest {
    private val index = runBlocking { SearchRepository(pe.pisacoma.iglesia.data.TestAssets.source).index() }

    @Test fun normalizeFoldsAccentsAndPunctuation() {
        assertEquals("senor dame paz", Search.normalize("¡Señor, dame paz!"))
        assertEquals("santo santo santo", Search.normalize("«Santo, Santo, Santo»"))
    }

    @Test fun scoreTitleRules() {
        val q = Search.normalize("santo")
        val tokens = q.split(" ")
        assertEquals(100, Search.scoreTitle(q, tokens, "Santo"))
        assertEquals(60, Search.scoreTitle(q, tokens, "Santo, santo, santo"))
        assertEquals(0, Search.scoreTitle(q, tokens, "Cantad alegres"))
        // word hit 12 + short-title bonus (6 − floor(len/12)); "dios santo" is 10 chars → +6
        assertEquals(12 + 6, Search.scoreTitle(q, tokens, "Dios santo"))
        // 40 chars → floor(40/12) = 3 → +3 (a title that starts with the query scores 60 instead)
        assertEquals(12 + 3, Search.scoreTitle(q, tokens, "El Dios santo de los ejércitos de Israel"))
    }

    @Test fun explicitReferenceScores200() {
        val g = Search.search(index, "juan 3:16")
        assertEquals(1, g.bible.size)
        assertEquals("Juan 3:16", g.bible[0].title)
        assertEquals(200, g.bible[0].score)
        assertEquals("/biblia/?libro=43&cap=3&v=16", g.bible[0].href)
        assertEquals("Jn", g.bible[0].num)
    }

    @Test fun phoneStyleReferences() {
        assertEquals("Salmos 23", Search.search(index, "salmo 23").bible.first().title)
        assertEquals("1 Corintios 13:4", Search.search(index, "1 cor 13 4").bible.first().title)
        assertEquals("2 Corintios 10:4-5", Search.search(index, "2co 10:4-5").bible.first().title)
        assertEquals(listOf("1 Corintios 13", "2 Corintios 13"), Search.search(index, "corintios 13").bible.map { it.title })
    }

    @Test fun bookNameAloneListsTheBook() {
        val g = Search.search(index, "génesis")
        assertEquals("Génesis", g.bible.first().title)
        assertEquals("Biblia · 50 capítulos", g.bible.first().caption)
        assertEquals("/biblia/?libro=1&cap=1", g.bible.first().href)
    }

    @Test fun hymnNumberInEveryHymnal() {
        val g = Search.search(index, "307")
        assertEquals(3, g.hymns.size)
        assertTrue(g.hymns.all { it.num == "307" })
        assertEquals("es", g.hymns.first().href.split("/")[2]) // es wins the tie (+1)
        assertEquals("/himnario/es/307/", g.hymns.first().href)
        assertTrue(g.egw.isEmpty())
        assertEquals("/escuela-sabatica/307/", g.lessons.firstOrNull()?.href ?: "/escuela-sabatica/307/") // no lesson 307
    }

    @Test fun lessonNumberWord() {
        val g = Search.search(index, "lección 6")
        assertTrue(g.hymns.isEmpty())
        assertEquals("6", g.lessons.first().num)
        assertEquals("/escuela-sabatica/6/", g.lessons.first().href)
        assertEquals(110, g.lessons.first().score)
    }

    @Test fun hymnTitleAndLyricLine() {
        val g = Search.search(index, "santo santo santo")
        assertTrue(g.hymns.isNotEmpty())
        // the old Portuguese «Santo, santo, santo» is an exact title (100) and ranks first; ES 61 starts with it (60 + 1)
        assertEquals(100, g.hymns.first().score)
        assertEquals("Hinário Antigo · Português", g.hymns.first().caption)
        val es61 = g.hymns.first { it.href == "/himnario/es/61/" }
        assertEquals(61, es61.score)
        assertEquals("Himnario Adventista · Español", es61.caption)
    }

    @Test fun egwBookTitleGetsTheBonus() {
        val g = Search.search(index, "camino a cristo")
        val book = g.egw.first()
        assertEquals("§", book.num)
        assertEquals("El Camino a Cristo", book.title)
        assertEquals("/egw/cc/", book.href)
        assertTrue(g.egw.any { it.href == "/egw/cc/?ch=1" })
    }

    @Test fun lessonDayTitles() {
        val g = Search.search(index, "pablo")
        assertTrue(g.lessons.any { it.num == "1.1" && it.href == "/escuela-sabatica/1/?dia=1" })
    }

    @Test fun emptyQuery() {
        assertEquals(0, Search.search(index, "   ").total)
    }
}
