package pe.pisacoma.iglesia

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import pe.pisacoma.iglesia.data.AndroidAssetSource
import pe.pisacoma.iglesia.data.AssetSource
import pe.pisacoma.iglesia.data.BibleRepository
import pe.pisacoma.iglesia.data.ChurchRepository
import pe.pisacoma.iglesia.data.EgwRepository
import pe.pisacoma.iglesia.data.HymnRepository
import pe.pisacoma.iglesia.data.QuarterlyRepository
import pe.pisacoma.iglesia.data.QuizRepository
import pe.pisacoma.iglesia.data.SearchRepository
import pe.pisacoma.iglesia.data.content.ContentDownloader
import pe.pisacoma.iglesia.data.content.ContentErrors
import pe.pisacoma.iglesia.data.content.ContentMode
import pe.pisacoma.iglesia.data.content.ContentStore
import pe.pisacoma.iglesia.data.content.Manifest
import pe.pisacoma.iglesia.data.net.Http
import pe.pisacoma.iglesia.data.prefs.UserState
import java.io.File

/** The app's single set of repositories, created once over the content store. */
class Repos(val assets: AssetSource, val userState: UserState) {
    val hymns = HymnRepository(assets)
    val bible = BibleRepository(assets)
    val egw = EgwRepository(assets)
    val quarterly = QuarterlyRepository(assets)
    val quizzes = QuizRepository(assets)
    val church = ChurchRepository(assets)
    val search = SearchRepository(assets)
}

class App : Application() {
    lateinit var repos: Repos
        private set
    lateinit var content: ContentStore
        private set
    lateinit var downloader: ContentDownloader
        private set

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        val userState = UserState(this)
        val manifest = Manifest.parse(assets.open("manifest.json").bufferedReader(Charsets.UTF_8).use { it.readText() })
        content = ContentStore(
            bundled = AndroidAssetSource(this),
            dir = File(filesDir, "content"),
            manifest = manifest,
            mode = { ContentMode.of(userState.string(UserState.Keys.contentMode)) },
            fetch = { path -> Http.bytes(BuildConfig.CONTENT_BASE_URL + path) },
        )
        downloader = ContentDownloader(content, appScope) { ok -> userState.set(UserState.Keys.contentPending, !ok) }
        ContentErrors.report = { content.missing.value = it.path }
        repos = Repos(content, userState)
        instance = this
        // A download interrupted by the process dying resumes on the next open.
        val mode = ContentMode.of(userState.string(UserState.Keys.contentMode))
        if (mode?.downloads == true && userState.bool(UserState.Keys.contentPending)) downloader.start(mode)
    }

    companion object {
        lateinit var instance: App
            private set
        val repos: Repos get() = instance.repos
        val content: ContentStore get() = instance.content
        val downloader: ContentDownloader get() = instance.downloader
    }
}
