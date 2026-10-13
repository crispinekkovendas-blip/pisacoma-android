package pe.pisacoma.iglesia.nav

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Site hrefs (as the bundled search index and the pages write them) map to routes one to one. */
class RoutesTest {
    @Test fun plainPaths() {
        assertEquals(Route.Home, Route.parse("/"))
        assertEquals(Route.Himnario, Route.parse("/himnario/"))
        assertEquals(Route.Hymn("es", 307), Route.parse("/himnario/es/307/"))
        assertEquals(Route.Video("abc_12"), Route.parse("/videos/abc_12"))
        assertEquals(Route.QuizPack("leccion-13-gracia-amor-comunion"), Route.parse("/quiz/leccion-13-gracia-amor-comunion"))
    }

    @Test fun queryStringsFromTheSite() {
        assertEquals(Route.Biblia(43, 3, 16), Route.parse("/biblia/?libro=43&cap=3&v=16"))
        assertEquals(Route.Biblia(1, 1, null), Route.parse("/biblia/?libro=1&cap=1"))
        assertEquals(Route.Biblia(), Route.parse("/biblia"))
        assertEquals(Route.EgwBook("cc", 2), Route.parse("/egw/cc/?ch=2"))
        assertEquals(Route.Leccion(4, 2), Route.parse("/escuela-sabatica/4/?dia=2"))
        assertEquals(Route.Leccion(4), Route.parse("/escuela-sabatica/4/"))
    }

    @Test fun pathsRoundTrip() {
        assertEquals("/biblia?libro=43&cap=3&v=16", Route.Biblia(43, 3, 16).path)
        assertEquals("/egw/cc?ch=2", Route.EgwBook("cc", 2).path)
        assertEquals("/escuela-sabatica/4?dia=2", Route.Leccion(4, 2).path)
        assertEquals("/escuela-sabatica/4", Route.Leccion(4, 2).base)
    }

    @Test fun unknownAndDeepLinkHost() {
        assertNull(Route.parse("/nada"))
        assertNull(Route.parse("/inicio-clasico"))
    }

    @Test fun quarterLabelInPeruTime() {
        assertEquals("Trimestre 2026·Q4", quarterLabel(1_791_000_000_000L)) // 2026-10-03
        assertEquals("Trimestre 2026·Q3", quarterLabel(1_790_000_000_000L)) // 2026-09-21
    }
}
