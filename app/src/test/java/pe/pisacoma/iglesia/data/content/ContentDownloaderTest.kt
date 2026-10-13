package pe.pisacoma.iglesia.data.content

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import pe.pisacoma.iglesia.data.FileAssetSource
import java.io.File
import java.io.IOException

class ContentDownloaderTest {
    @get:Rule val tmp = TemporaryFolder()
    private val manifest = Manifest(
        files = listOf(
            ContentFile("data/himnario-es.json", 3, "himnario-es"),
            ContentFile("data/biblia/1.json", 4, "biblia"),
            ContentFile("data/egw/cc.json", 5, "egw"),
        ),
    )
    private val remote = mapOf("data/himnario-es.json" to "abc", "data/biblia/1.json" to "defg", "data/egw/cc.json" to "hijkl")
    private var failing = setOf<String>()
    private val fetched = mutableListOf<String>()

    private fun store() = ContentStore(FileAssetSource(tmp.newFolder("b")), File(tmp.root, "s"), manifest, { ContentMode.TODO }) { p ->
        fetched += p
        if (p in failing) throw IOException("x")
        remote[p]!!.toByteArray()
    }

    @Test fun downloadsEverythingForTodo() = runTest {
        val s = store()
        var completed: Boolean? = null
        val d = ContentDownloader(s, this) { completed = it }
        d.start(ContentMode.TODO)!!.join()
        assertEquals(DownloadState.Done(12), d.state.value)
        assertEquals(3, s.storedCount())
        assertEquals(true, completed)
    }

    @Test fun esencialSkipsEgw() = runTest {
        val s = store()
        val d = ContentDownloader(s, this)
        d.start(ContentMode.ESENCIAL)!!.join()
        assertFalse(s.isStored("data/egw/cc.json"))
        assertEquals(DownloadState.Done(7), d.state.value)
    }

    @Test fun skipsStoredFiles() = runTest {
        val s = store()
        s.download("data/biblia/1.json")
        fetched.clear()
        ContentDownloader(s, this).start(ContentMode.TODO)!!.join()
        assertFalse("data/biblia/1.json" in fetched)
    }

    @Test fun countsFailuresAndRetries() = runTest {
        failing = setOf("data/egw/cc.json")
        val s = store()
        var completed: Boolean? = null
        val d = ContentDownloader(s, this) { completed = it }
        d.start(ContentMode.TODO)!!.join()
        assertEquals(DownloadState.Error(1, 3), d.state.value)
        assertEquals(3, fetched.count { it == "data/egw/cc.json" })
        assertEquals(false, completed)
    }

    @Test fun nothingToDoReturnsNullAndDone() = runTest {
        val s = store()
        val d = ContentDownloader(s, this)
        assertNull(d.start(ContentMode.USO))
        assertEquals(DownloadState.Done(0), d.state.value)
    }

    @Test fun missingForListsOnlyAbsentFiles() = runTest {
        val s = store()
        s.download("data/himnario-es.json")
        assertEquals(listOf("data/biblia/1.json"), ContentDownloader(s, this).missingFor(ContentMode.ESENCIAL).map { it.path })
    }

    @Test fun secondStartWhileRunningIsIgnored() = runTest {
        val s = store()
        val d = ContentDownloader(s, this)
        val job = d.start(ContentMode.TODO)!!
        assertNull(d.start(ContentMode.TODO))
        job.join()
        assertEquals(3, fetched.size)
    }
}
