package pe.pisacoma.iglesia.data.net

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

/** Port of the site's utils/anuncios.ts: CSV → Anuncio, and the 21-day window selection. */
class AnunciosTest {
    private val csv = listOf(
        "titulo,fecha,hora,detalle,hasta",
        "Culto de jóvenes,2026-09-20,3:00 PM,\"En el templo, con \"\"invitados\"\"\",",
        "Campaña,15/10/2026,,Toda la semana,22/10/2026",
        "Sin fecha,,,,",
        "Vigilia,2026-10-30,8:00 PM,,",
    ).joinToString("\n")

    @Test fun parseCsvHandlesQuotesAndCrlf() {
        val rows = Anuncios.parseCsv("a,b\r\n\"x, y\",\"q\"\"q\"\n")
        assertEquals(listOf(listOf("a", "b"), listOf("x, y", "q\"q")), rows)
    }

    @Test fun parseDayAcceptsBothFormats() {
        assertEquals(Instant.parse("2026-09-20T00:00:00Z").toEpochMilli(), Anuncios.parseDay("2026-09-20"))
        assertEquals(Instant.parse("2026-10-15T00:00:00Z").toEpochMilli(), Anuncios.parseDay("15/10/2026"))
        assertEquals(null, Anuncios.parseDay(""))
        assertEquals(null, Anuncios.parseDay("ayer"))
    }

    @Test fun csvToAnunciosWithHeaderAliases() {
        val items = Anuncios.fromCsv(csv)
        assertEquals(3, items.size) // "Sin fecha" dropped
        assertEquals(Anuncio("Culto de jóvenes", "2026-09-20", "3:00 PM", "En el templo, con \"invitados\"", null), items[0])
        assertEquals("22/10/2026", items[1].until)
        assertTrue(Anuncios.fromCsv("nombre,cosa\na,b").isEmpty()) // no titulo/fecha columns
        assertTrue(Anuncios.fromCsv("").isEmpty())
    }

    @Test fun selectKeepsCurrentAndUpcomingWithinThreeWeeks() {
        val now = Instant.parse("2026-10-13T15:00:00Z").toEpochMilli()
        val views = Anuncios.select(Anuncios.fromCsv(csv), now)
        // Culto (September) is past; Campaña runs until 22/10; Vigilia 30/10 is within 21 days
        assertEquals(listOf("Campaña", "Vigilia"), views.map { it.title })
        assertEquals("Jueves 15 oct", views[0].dateLabel)
        assertEquals(false, views[0].isToday)
    }

    @Test fun selectMarksTodayAndCapsAtFive() {
        val now = Instant.parse("2026-10-13T15:00:00Z").toEpochMilli()
        val items = (1..7).map { Anuncio("A$it", "2026-10-13", null, null, null) }
        val views = Anuncios.select(items, now)
        assertEquals(5, views.size)
        assertTrue(views.all { it.isToday })
        assertEquals("Martes 13 oct", views[0].dateLabel)
    }

    @Test fun selectIgnoresBeyondHorizon() {
        val now = Instant.parse("2026-10-13T15:00:00Z").toEpochMilli()
        assertTrue(Anuncios.select(listOf(Anuncio("Lejos", "2026-12-01", null, null, null)), now).isEmpty())
    }
}
