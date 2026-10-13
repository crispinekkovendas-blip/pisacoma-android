package pe.pisacoma.iglesia.data

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import pe.pisacoma.iglesia.data.model.HymnVariant
import java.io.File

/** The models parse the real bundled files (read from the source tree on the JVM). */
class ModelsParseTest {
    private val assets = FileAssetSource(File("src/main/assets"))

    @Test fun spanishHymnalHas613Hymns() = runBlocking {
        val hymns = HymnRepository(assets).all(HymnVariant.ES)
        assertEquals(613, hymns.size)
        val first = hymns.first()
        assertEquals(1, first.number)
        assertEquals("Cantad Alegres al señor", first.title)
        assertEquals("Salmos 100:1-5", first.bibleReference)
        assertTrue(first.mp3Url!!.startsWith("https://docs.google.com/"))
        assertNotNull(first.mp3UrlInstr)
        assertTrue(first.blocks.first().lines.first().startsWith("Cantad alegres al Señor"))
    }

    @Test fun oldPortugueseHymnalHas610() = runBlocking {
        val hymns = HymnRepository(assets).all(HymnVariant.PT)
        assertEquals(610, hymns.size)
        assertTrue(hymns.first().mp3Url!!.contains("Hina7"))
    }

    @Test fun novoHinarioHas601WithYouTubeIds() = runBlocking {
        val hymns = HymnRepository(assets).all(HymnVariant.PT_NOVO)
        assertEquals(601, hymns.size)
        assertEquals("FDpludwY46U", hymns.first().ytId)
        assertEquals("stanza", hymns.first().blocks.first().type)
    }

    @Test fun hymnByNumber() = runBlocking {
        assertEquals("Cantad Alegres al señor", HymnRepository(assets).get(HymnVariant.ES, 1)?.title)
        assertEquals(null, HymnRepository(assets).get(HymnVariant.ES, 9999))
    }

    @Test fun featuredHymns() {
        assertEquals(listOf(1, 61, 115, 164, 303, 307, 400, 516), runBlocking { HymnRepository(assets).featured(HymnVariant.ES) })
    }

    @Test fun bibleBooksAndGenesis() = runBlocking {
        val repo = BibleRepository(assets)
        assertEquals(66, repo.books().size)
        assertEquals("Génesis", repo.books().first().name)
        assertEquals(50, repo.books().first().chapters)
        val ch1 = repo.chapter(1, 1)
        assertEquals(31, ch1.size)
        assertTrue(ch1.first().startsWith("En el principio"))
        val range = repo.verses(43, 3, 16, 17)
        assertEquals(2, range.size)
        assertEquals(16, range.first().first)
        assertTrue(range.first().second.startsWith("Porque de tal manera"))
    }

    @Test fun egwCatalogueAndBook() = runBlocking {
        val repo = EgwRepository(assets)
        assertEquals(25, repo.catalogue().size)
        assertEquals(4, repo.rails().size)
        assertEquals("cc", repo.catalogue().first().slug)
        val cc = repo.book("cc")!!
        assertEquals("El camino a Cristo", cc.chapters[0].title)
        assertEquals("Capítulo 1— Amor supremo", cc.chapters[1].title)
        assertTrue(cc.chapters.size > 10)
        assertEquals(null, repo.book("nope"))
    }

    @Test fun quarterlyHas13LessonsOf7Days() = runBlocking {
        val q = QuarterlyRepository(assets).quarterly()
        assertEquals(13, q.lessons.size)
        assertTrue(q.lessons.all { it.days.size == 7 })
        assertEquals("2026-03", q.id)
        assertEquals(1, q.lessons.first().number)
    }

    @Test fun quizzesParse() = runBlocking {
        val packs = QuizRepository(assets).packs()
        assertTrue(packs.size >= 5)
        assertTrue(packs.all { it.questions.size >= 5 })
        assertEquals(13, packs.first().lessonNumber)
        assertNotNull(QuizRepository(assets).pack(packs.first().slug))
    }

    @Test fun churchFacts() = runBlocking {
        val c = ChurchRepository(assets).church()
        assertEquals(3, c.schedule.size)
        assertEquals("Escuela Sabática", c.schedule.first().name)
        assertEquals(-16.908, c.location.lat, 1e-6)
        assertEquals(5, c.hierarchy.size)
        assertEquals(3, c.pioneers.size)
    }

    @Test fun searchIndexParses() = runBlocking {
        val idx = SearchRepository(assets).index()
        assertTrue(idx.v >= 1)
        assertTrue(idx.hymns.size > 1800)
        assertEquals(66, idx.bible.size)
        assertEquals(13, idx.lessons.size)
    }
}
