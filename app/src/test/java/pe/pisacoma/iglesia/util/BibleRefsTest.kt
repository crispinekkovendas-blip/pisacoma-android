package pe.pisacoma.iglesia.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Port of the site's bibleStore.ts findRefs: same regex, same book map, same ParsedRef shape. */
class BibleRefsTest {
    @Test fun simpleReference() {
        val refs = BibleRefs.findRefs("Lee Juan 3:16 hoy")
        assertEquals(1, refs.size)
        val r = refs[0]
        assertEquals(43, r.bookNum)
        assertEquals("Juan", r.bookName)
        assertEquals(listOf(RefSection(3, 16, 16)), r.sections)
        assertEquals("Juan 3:16", r.display)
        assertEquals(4, r.start)
        assertEquals(13, r.end)
    }

    @Test fun abbreviationWithDotAndRangeWithAl() {
        val r = BibleRefs.findRefs("1 Cor. 10:12 al 15").single()
        assertEquals(46, r.bookNum)
        assertEquals("1 Corintios", r.bookName)
        assertEquals(listOf(RefSection(10, 12, 15)), r.sections)
    }

    @Test fun semicolonContinuationAddsSections() {
        val r = BibleRefs.findRefs("Juan 3:16; 5:4-8 dice").single()
        assertEquals(listOf(RefSection(3, 16, 16), RefSection(5, 4, 8)), r.sections)
        assertEquals("Juan 3:16; 5:4-8", r.display)
    }

    @Test fun accentedAbbreviation() {
        assertEquals(1, BibleRefs.findRefs("Gén. 1:1").single().bookNum)
        assertEquals(48, BibleRefs.findRefs("Gálatas 1:1").single().bookNum)
    }

    @Test fun spaceAfterColonAsTheQuarterlyWrites() {
        val r = BibleRefs.findRefs("Hechos 17: 16-34").single()
        assertEquals(44, r.bookNum)
        assertEquals(listOf(RefSection(17, 16, 34)), r.sections)
    }

    @Test fun psalmsRange() {
        val r = BibleRefs.findRefs("Salmos 100:1-5").single()
        assertEquals(19, r.bookNum)
        assertEquals(listOf(RefSection(100, 1, 5)), r.sections)
    }

    @Test fun severalReferencesInOneLine() {
        val refs = BibleRefs.findRefs("1 Corintios 1: 1; Gálatas 1: 1; Hechos 17: 16-34; 1 Corintios 5: 9-11; 2 Corintios 2: 4.")
        assertEquals(listOf(46, 48, 44, 46, 47), refs.map { it.bookNum })
    }

    @Test fun unknownBookIsIgnored() {
        assertTrue(BibleRefs.findRefs("Capítulo 3:16 de un libro").isEmpty())
    }

    @Test fun bookNamesTable() {
        assertEquals("Apocalipsis", BibleRefs.bookName(66))
        assertEquals(62, BibleRefs.resolveBook("1 jn"))
        assertEquals(null, BibleRefs.resolveBook("nada"))
    }
}
