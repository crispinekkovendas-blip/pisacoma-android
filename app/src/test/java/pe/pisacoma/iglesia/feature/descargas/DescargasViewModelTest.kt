package pe.pisacoma.iglesia.feature.descargas

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import pe.pisacoma.iglesia.data.FileAssetSource
import pe.pisacoma.iglesia.data.MemoryKeyValueStore
import pe.pisacoma.iglesia.data.content.ContentDownloader
import pe.pisacoma.iglesia.data.content.ContentFile
import pe.pisacoma.iglesia.data.content.ContentMode
import pe.pisacoma.iglesia.data.content.ContentStore
import pe.pisacoma.iglesia.data.content.DownloadState
import pe.pisacoma.iglesia.data.content.Manifest
import pe.pisacoma.iglesia.data.prefs.UserState
import java.io.File
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class DescargasViewModelTest {
    @get:Rule val tmp = TemporaryFolder()
    private val manifest = Manifest(
        files = listOf(
            ContentFile("data/himnario-es.json", 1_100_000, "himnario-es"),
            ContentFile("data/biblia/1.json", 4_000_000, "biblia"),
            ContentFile("data/egw/cc.json", 20_000_000, "egw"),
        ),
    )
    private var offline = false
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private lateinit var userState: UserState
    private lateinit var store: ContentStore
    private lateinit var downloader: ContentDownloader

    @Before fun setUp() {
        Dispatchers.setMain(Dispatchers.Unconfined)
        userState = UserState(MemoryKeyValueStore())
        store = ContentStore(FileAssetSource(tmp.newFolder("b")), File(tmp.root, "s"), manifest, { ContentMode.of(userState.string(UserState.Keys.contentMode)) }) { p ->
            if (offline) throw IOException("offline")
            ByteArray(manifest.byPath(p)!!.bytes.toInt())
        }
        downloader = ContentDownloader(store, scope) { ok -> userState.set(UserState.Keys.contentPending, !ok) }
    }

    @After fun tearDown() = Dispatchers.resetMain()

    private fun vm() = DescargasViewModel(store, downloader, userState)

    private fun awaitDownload() = runBlocking {
        withTimeout(10_000) { downloader.state.first { it is DownloadState.Done || it is DownloadState.Error } }
    }

    @Test fun firstRunHasNoCurrentAndCannotContinue() {
        val s = vm().state.value
        assertNull(s.current)
        assertFalse(s.canContinue)
        assertEquals("", s.storedText)
    }

    @Test fun optionSizesComeFromTheManifest() {
        val o = vm().state.value.options
        assertEquals(listOf(ContentMode.TODO, ContentMode.ESENCIAL, ContentMode.USO, ContentMode.EN_LINEA), o.map { it.mode })
        assertEquals("25 MB", o[0].size)
        assertEquals("5,1 MB", o[1].size)
        assertEquals("", o[2].size)
        assertEquals("", o[3].size)
    }

    @Test fun chooseUsoStoresTheModeWithoutDownloading() {
        val vm = vm()
        vm.choose(ContentMode.USO)
        assertEquals("uso", userState.string(UserState.Keys.contentMode))
        assertFalse(userState.bool(UserState.Keys.contentPending))
        assertTrue(vm.state.value.canContinue)
        assertEquals(0, store.storedCount())
    }

    @Test fun chooseEsencialDownloadsAndClearsPending() {
        val vm = vm()
        vm.choose(ContentMode.ESENCIAL)
        assertEquals("esencial", userState.string(UserState.Keys.contentMode))
        awaitDownload()
        assertEquals(DownloadState.Done(5_100_000), downloader.state.value)
        assertFalse(userState.bool(UserState.Keys.contentPending))
        assertEquals(2, store.storedCount())
        assertFalse(store.isStored("data/egw/cc.json"))
        vm.refresh()
        assertEquals("Guardado en este teléfono · 5,1 MB", vm.state.value.storedText)
        assertEquals(0, vm.state.value.missingCount)
    }

    @Test fun offlineChoiceStaysPendingWithAnError() {
        offline = true
        val vm = vm()
        vm.choose(ContentMode.TODO)
        awaitDownload()
        assertTrue(downloader.state.value is DownloadState.Error)
        assertTrue(userState.bool(UserState.Keys.contentPending))
        vm.refresh()
        assertEquals(3, vm.state.value.missingCount)
    }

    @Test fun clearEmptiesTheStoreAndKeepsTheMode() {
        val vm = vm()
        vm.choose(ContentMode.ESENCIAL)
        awaitDownload()
        vm.clear()
        assertEquals(0, store.storedCount())
        assertEquals("esencial", userState.string(UserState.Keys.contentMode))
        assertTrue(userState.bool(UserState.Keys.contentPending))
        assertEquals(2, vm.state.value.missingCount)
    }
}
