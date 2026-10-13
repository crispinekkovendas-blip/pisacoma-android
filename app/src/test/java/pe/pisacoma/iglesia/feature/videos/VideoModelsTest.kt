package pe.pisacoma.iglesia.feature.videos

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Port of the site's videos/data.ts helpers: CHANNELS, CATS regexes, tagVideo, fmtTime, thumbs. */
class VideoModelsTest {
    @Test fun channelsAreTheSitesFour() {
        assertEquals(listOf("bullon", "flores", "sumtv", "lecciones"), VideoCatalog.CHANNELS.map { it.key })
        assertEquals("UCzY9YOmZquSEERTbl_u-MSQ", VideoCatalog.CHANNELS[0].id)
        assertEquals("Pr. Alejandro Bullón", VideoCatalog.CHANNELS[0].label)
        assertEquals("Bullón", VideoCatalog.CHANNELS[0].shortLabel)
    }

    @Test fun categoriesInTheSitesOrder() {
        assertEquals(
            listOf("sermones", "escuela_sabatica", "musica", "profecia", "salud_familia", "en_vivo", "juveniles", "ninos", "egw", "pisacoma"),
            VideoCatalog.CATS.map { it.key },
        )
        assertEquals("Sermones · Predicación", VideoCatalog.CATS[0].label)
    }

    @Test fun tagVideoUsesTitleRegexesManualTagsAndChannel() {
        val v = VideoEntry(videoId = "x", title = "Sermón: la gracia de Daniel", channel = "bullon", tags = listOf("destacado"))
        val tagged = VideoCatalog.tagVideo(v)
        assertEquals(listOf("sermones", "profecia", "destacado", "channel:bullon"), tagged.tags)
    }

    @Test fun tagVideoIsCaseInsensitiveAndAccentTolerant() {
        assertTrue("escuela_sabatica" in VideoCatalog.tagVideo(VideoEntry("x", "LECCION 4 — Escuela Sabatica")).tags)
        assertTrue("juveniles" in VideoCatalog.tagVideo(VideoEntry("x", "Culto JA de jóvenes")).tags)
        assertTrue("pisacoma" in VideoCatalog.tagVideo(VideoEntry("x", "Altiplano aimara")).tags)
    }

    @Test fun fmtTime() {
        assertEquals("0:00", VideoCatalog.fmtTime(0.0))
        assertEquals("1:15", VideoCatalog.fmtTime(75.0))
        assertEquals("12:05", VideoCatalog.fmtTime(725.4))
        assertEquals("0:00", VideoCatalog.fmtTime(Double.NaN))
    }

    @Test fun thumbnailUrl() {
        assertEquals("https://i.ytimg.com/vi/abc/hqdefault.jpg", VideoCatalog.thumb("abc", "hqdefault"))
        assertEquals(listOf("maxresdefault", "hqdefault", "sddefault", "mqdefault", "default"), VideoCatalog.THUMB_SIZES)
    }

    @Test fun relativeTimeInSpanish() {
        val now = 1_791_000_000_000L
        // Intl.RelativeTimeFormat("es", { numeric: "auto" }) — "ayer"/"anteayer" for 1 and 2 days
        assertEquals("ayer", VideoCatalog.relTimeEs(now - 1 * 86_400_000L, now))
        assertEquals("hace 3 días", VideoCatalog.relTimeEs(now - 3 * 86_400_000L, now))
        assertEquals("hace 3 horas", VideoCatalog.relTimeEs(now - 3 * 3_600_000L, now))
        assertEquals("hace 5 minutos", VideoCatalog.relTimeEs(now - 5 * 60_000L, now))
        assertEquals("hace 2 meses", VideoCatalog.relTimeEs(now - 61 * 86_400_000L, now))
        assertEquals("el año pasado", VideoCatalog.relTimeEs(now - 400 * 86_400_000L, now))
        assertEquals("", VideoCatalog.relTimeEs(null, now))
    }

    @Test fun feedJsonParses() {
        val list = VideoCatalog.parseFeed("""{"videos":[{"videoId":"a1","title":"Sermón uno","published":"2026-10-01T10:00:00Z","channel":"flores"},{"videoId":"b2","title":"Lección 2","channel":"lecciones","hero":true}]}""")
        assertEquals(2, list.size)
        assertEquals("a1", list[0].videoId)
        assertEquals(true, list[1].hero)
        assertTrue(VideoCatalog.parseFeed("not json").isEmpty())
    }
}
