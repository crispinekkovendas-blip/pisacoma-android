package pe.pisacoma.iglesia.audio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import pe.pisacoma.iglesia.data.model.Hymn
import pe.pisacoma.iglesia.data.model.HymnVariant
import pe.pisacoma.iglesia.data.model.LyricBlock

/** Hymn audio goes through the site's proxy exactly as the hymn page builds the URL. */
class MediaItemsTest {
    @Test fun proxiedUrlMatchesEncodeURIComponent() {
        assertEquals(
            "https://quiz-qwik.vercel.app/api/audio?url=https%3A%2F%2Fdocs.google.com%2Fuc%3Fexport%3Ddownload%26id%3DX",
            AudioUrls.proxied("https://docs.google.com/uc?export=download&id=X"),
        )
        assertEquals(
            "https://quiz-qwik.vercel.app/api/audio?url=https%3A%2F%2Fgithub.com%2Fmoser-jose%2FHina7%2Freleases%2Fdownload%2FvHinos%2F001.O.Deus.de.Amor.mp3",
            AudioUrls.proxied("https://github.com/moser-jose/Hina7/releases/download/vHinos/001.O.Deus.de.Amor.mp3"),
        )
    }

    @Test fun mediaIdAndTitlePerHymn() {
        val hymn = Hymn(HymnVariant.ES, 1, "Cantad Alegres al señor", "Salmos 100:1-5", "https://docs.google.com/uc?id=a", "https://docs.google.com/uc?id=b", null, listOf(LyricBlock("stanza", listOf("x"))))
        assertEquals("es-1", AudioUrls.mediaId(hymn, instrumental = false))
        assertEquals("es-1-instr", AudioUrls.mediaId(hymn, instrumental = true))
        assertEquals("1 · Cantad Alegres al señor", AudioUrls.title(hymn, instrumental = false))
        assertEquals("1 · Cantad Alegres al señor (instrumental)", AudioUrls.title(hymn, instrumental = true))
        assertEquals("https://quiz-qwik.vercel.app/api/audio?url=https%3A%2F%2Fdocs.google.com%2Fuc%3Fid%3Db", AudioUrls.streamUrl(hymn, instrumental = true))
        assertNull(AudioUrls.streamUrl(hymn.copy(mp3UrlInstr = null), instrumental = true))
    }
}
