package pe.pisacoma.iglesia.data

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.io.FileNotFoundException

class CompositeAssetSourceTest {
    @get:Rule val tmp = TemporaryFolder()

    private fun source(): CompositeAssetSource {
        val a = tmp.newFolder("a").also { File(it, "x.json").writeText("from a"); File(it, "only-a.json").writeText("a") }
        val b = tmp.newFolder("b").also { File(it, "x.json").writeText("from b"); File(it, "only-b.json").writeText("b") }
        return CompositeAssetSource(listOf(FileAssetSource(a), FileAssetSource(b)))
    }

    @Test fun firstSourceWins() = runBlocking { assertEquals("from a", source().read("x.json")) }
    @Test fun fallsThroughToLaterSources() = runBlocking { assertEquals("b", source().read("only-b.json")) }
    @Test fun existsIsTheUnion() = runBlocking { val s = source(); assertTrue(s.exists("only-a.json")); assertTrue(s.exists("only-b.json")); assertFalse(s.exists("nope.json")) }
    @Test fun missingEverywhereThrows() = runBlocking { try { source().read("nope.json"); fail() } catch (e: FileNotFoundException) { assertEquals("nope.json", e.message) } }
}
