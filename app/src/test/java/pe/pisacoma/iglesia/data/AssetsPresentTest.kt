package pe.pisacoma.iglesia.data

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/** What tools/sync-content.mjs generates: small indices + home + covers in the APK, the rest in content/. */
class AssetsPresentTest {
    private fun bundled(path: String) = File("src/main/assets/$path")
    private fun content(path: String) = File("../content/$path")

    private fun assertAtLeast(f: File, bytes: Long) {
        assertTrue("${f.path} missing — run tools/sync-content.mjs", f.isFile)
        assertTrue("${f.path} is only ${f.length()} bytes", f.length() >= bytes)
    }

    @Test fun spanishHymnal() = assertAtLeast(content("data/himnario-es.json"), 500_000)
    @Test fun portugueseHymnals() { assertAtLeast(content("data/himnario-pt.json"), 300_000); assertAtLeast(content("data/himnario-pt-novo.json"), 300_000) }
    @Test fun bibleFirstBook() = assertAtLeast(content("data/biblia/1.json"), 100_000)
    @Test fun bibleHasSixtySixBooks() = assertTrue(content("data/biblia").listFiles()!!.count { it.name.endsWith(".json") } == 66)
    @Test fun egwBook() = assertAtLeast(content("data/egw/cc.json"), 100_000)
    @Test fun egwCatalogueHas25Books() = assertTrue(content("data/egw").listFiles()!!.count { it.name.endsWith(".json") } == 25)
    @Test fun quarterly() = assertAtLeast(content("data/escuela-sabatica.json"), 300_000)
    @Test fun searchIndex() = assertAtLeast(content("data/search-index.json"), 100_000)
    @Test fun quizzes() = assertAtLeast(content("data/quizzes.json"), 10_000)
    @Test fun church() = assertAtLeast(bundled("data/church.json"), 1_000)
    @Test fun indicesAreBundled() {
        for (f in listOf("bible-books", "egw-index", "featured-hymns", "home-photos", "anuncios", "videos-channels")) assertAtLeast(bundled("data/$f.json"), 100)
    }
    @Test fun coversAreBundled() = assertTrue(bundled("covers").listFiles()!!.size == 25)
    @Test fun homeLottie() = assertAtLeast(bundled("home/lottie/inicio-portrait.json"), 2_000)
    @Test fun homePoster() = assertAtLeast(bundled("home/inicio-portrait.jpg"), 20_000)
    @Test fun manifestIsBundled() = assertAtLeast(bundled("manifest.json"), 1_000)
    @Test fun fonts() { for (f in listOf("fraunces", "fraunces_italic", "inter", "dm_sans")) assertTrue(File("src/main/res/font/$f.ttf").length() > 30_000) }
}
