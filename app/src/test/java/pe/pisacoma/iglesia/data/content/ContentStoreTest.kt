package pe.pisacoma.iglesia.data.content

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import pe.pisacoma.iglesia.data.FileAssetSource
import java.io.File
import java.io.IOException

class ContentStoreTest {
    @get:Rule val tmp = TemporaryFolder()
    private val remote = mutableMapOf("data/x.json" to "{\"x\":1}", "data/big.json" to "0123456789")
    private var offline = false
    private var fetched = 0
    private val manifest = Manifest(files = listOf(ContentFile("data/x.json", 7, "biblia"), ContentFile("data/big.json", 10, "egw")))
    private var mode: ContentMode? = ContentMode.USO

    private fun store(): ContentStore {
        val bundledDir = tmp.newFolder("bundled").also { File(it, "data/bundled.json").apply { parentFile.mkdirs(); writeText("{\"b\":1}") } }
        return ContentStore(FileAssetSource(bundledDir), File(tmp.root, "store"), manifest, { mode }) { path ->
            fetched++
            if (offline) throw IOException("offline")
            remote[path]?.toByteArray() ?: throw IOException("404")
        }
    }

    @Test fun readsBundledFirst() = runBlocking { assertEquals("{\"b\":1}", store().read("data/bundled.json")); assertEquals(0, fetched) }

    @Test fun fetchesAndStoresOnUse() = runBlocking {
        val s = store()
        assertEquals("{\"x\":1}", s.read("data/x.json"))
        assertTrue(s.isStored("data/x.json"))
        assertEquals("{\"x\":1}", s.read("data/x.json"))
        assertEquals(1, fetched)
    }

    @Test fun onlineModeNeverWrites() = runBlocking {
        mode = ContentMode.EN_LINEA
        val s = store()
        s.read("data/x.json")
        assertFalse(s.isStored("data/x.json"))
        assertEquals(0L, s.storedBytes())
    }

    @Test fun offlineMissingThrowsAndReports() = runBlocking {
        offline = true
        val s = store()
        try { s.read("data/x.json"); fail("expected ContentMissing") } catch (e: ContentMissing) { assertEquals("data/x.json", e.path) }
        assertEquals("data/x.json", s.missing.value)
    }

    @Test fun backgroundDownloadFailureDoesNotRaiseTheNotice() = runBlocking {
        offline = true
        val s = store()
        try { s.download("data/x.json"); fail("expected IOException") } catch (e: IOException) {}
        assertEquals(null, s.missing.value)
    }

    @Test fun staleFileIsRefetched() = runBlocking {
        // A new APK ships a new manifest: a stored file whose size no longer matches is downloaded again.
        val s = store()
        File(s.dir, "data/x.json").apply { parentFile.mkdirs(); writeText("old") }
        assertFalse(s.isStored("data/x.json"))
        assertEquals("{\"x\":1}", s.read("data/x.json"))
        assertEquals(1, fetched)
        assertTrue(s.isStored("data/x.json"))
    }

    @Test fun offlineStoredStillReads() = runBlocking {
        val s = store()
        s.read("data/x.json")
        offline = true
        assertEquals("{\"x\":1}", s.read("data/x.json"))
    }

    @Test fun existsUsesManifestNotNetwork() = runBlocking {
        offline = true
        val s = store()
        assertTrue(s.exists("data/x.json"))
        assertTrue(s.exists("data/bundled.json"))
        assertFalse(s.exists("data/nope.json"))
        assertEquals(0, fetched)
    }

    @Test fun rejectsShortBody() = runBlocking {
        remote["data/big.json"] = "0123"
        val s = store()
        try { s.read("data/big.json"); fail("expected IOException") } catch (e: IOException) { assertFalse(e is ContentMissing) }
        assertFalse(s.isStored("data/big.json"))
    }

    @Test fun downloadIsIdempotent() = runBlocking {
        val s = store()
        s.download("data/x.json")
        s.download("data/x.json")
        assertEquals(1, fetched)
        assertEquals(7L, s.storedBytes())
        assertEquals(1, s.storedCount())
    }

    @Test fun clearEmptiesTheStore() = runBlocking {
        val s = store()
        s.download("data/x.json")
        s.clear()
        assertEquals(0, s.storedCount())
        assertFalse(s.isStored("data/x.json"))
    }

    @Test fun nullModeBehavesLikeUso() = runBlocking {
        mode = null
        val s = store()
        s.read("data/x.json")
        assertTrue(s.isStored("data/x.json"))
    }
}
