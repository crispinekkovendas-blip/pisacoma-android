package pe.pisacoma.iglesia.data.content

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class ContentErrorsTest {
    private val reported = mutableListOf<String>()
    private var crashed: Throwable? = null
    private var previousDefault: Thread.UncaughtExceptionHandler? = null

    @Before fun swap() {
        ContentErrors.report = { reported += it.path }
        previousDefault = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { _, e -> crashed = e }
    }

    @After fun restore() {
        ContentErrors.report = {}
        Thread.setDefaultUncaughtExceptionHandler(previousDefault)
    }

    private fun run(block: suspend () -> Unit) = runBlocking {
        CoroutineScope(SupervisorJob() + ContentErrors).launch { block() }.join()
    }

    @Test fun missingContentIsReportedNotCrashed() {
        run { throw ContentMissing("data/biblia/43.json") }
        assertEquals(listOf("data/biblia/43.json"), reported)
        assertNull(crashed)
    }

    @Test fun wrappedMissingContentIsReported() {
        run { throw IllegalStateException("decode failed", ContentMissing("data/egw/cc.json")) }
        assertEquals(listOf("data/egw/cc.json"), reported)
        assertNull(crashed)
    }

    @Test fun otherExceptionsReachTheDefaultHandler() {
        run { throw IllegalStateException("boom") }
        assertEquals(0, reported.size)
        assertEquals("boom", crashed?.message)
    }
}
