package pe.pisacoma.iglesia.data.content

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ManifestTest {
    private val m = Manifest.parse(
        """{"generated":"2026-10-13","files":[
        {"path":"data/himnario-es.json","bytes":1000,"pack":"himnario-es"},
        {"path":"data/biblia/1.json","bytes":2000,"pack":"biblia"},
        {"path":"data/egw/cc.json","bytes":4000,"pack":"egw"}]}""",
    )

    @Test fun bytesOfPacks() = assertEquals(3000L, m.bytesOf(listOf("himnario-es", "biblia")))
    @Test fun totalBytes() = assertEquals(7000L, m.totalBytes)
    @Test fun filesForTodoIsEverything() = assertEquals(3, m.filesFor(ContentMode.TODO).size)
    @Test fun filesForEsencialSkipsEgw() =
        assertEquals(listOf("data/himnario-es.json", "data/biblia/1.json"), m.filesFor(ContentMode.ESENCIAL).map { it.path })
    @Test fun filesForUsoAndOnlineAreEmpty() {
        assertEquals(0, m.filesFor(ContentMode.USO).size)
        assertEquals(0, m.filesFor(ContentMode.EN_LINEA).size)
    }
    @Test fun byPath() { assertEquals(2000L, m.byPath("data/biblia/1.json")!!.bytes); assertNull(m.byPath("nope")) }
    @Test fun modeOf() {
        assertEquals(ContentMode.EN_LINEA, ContentMode.of("en-linea"))
        assertNull(ContentMode.of(null))
        assertNull(ContentMode.of("x"))
    }
    @Test fun modeFlags() {
        assertEquals(true, ContentMode.ESENCIAL.downloads)
        assertEquals(false, ContentMode.USO.downloads)
        assertEquals(false, ContentMode.EN_LINEA.persists)
        assertEquals(true, ContentMode.USO.persists)
    }
    @Test fun formatSizes() {
        assertEquals("5,9 MB", formatMb(5_900_000))
        assertEquals("29 MB", formatMb(29_400_000))
        assertEquals("0 MB", formatMb(0))
        assertEquals("0,3 MB", formatMb(331_000))
    }
    @Test fun packLabels() = assertEquals("25 libros de Ellen G. White", Packs.label("egw"))
    @Test fun essentialPacks() = assertEquals(setOf("himnario-es", "biblia", "escuela", "buscar"), Packs.ESSENTIAL)
}
