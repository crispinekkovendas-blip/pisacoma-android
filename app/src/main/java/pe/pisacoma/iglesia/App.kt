package pe.pisacoma.iglesia

import android.app.Application
import pe.pisacoma.iglesia.data.AndroidAssetSource
import pe.pisacoma.iglesia.data.AssetSource
import pe.pisacoma.iglesia.data.BibleRepository
import pe.pisacoma.iglesia.data.ChurchRepository
import pe.pisacoma.iglesia.data.EgwRepository
import pe.pisacoma.iglesia.data.HymnRepository
import pe.pisacoma.iglesia.data.QuarterlyRepository
import pe.pisacoma.iglesia.data.QuizRepository
import pe.pisacoma.iglesia.data.SearchRepository
import pe.pisacoma.iglesia.data.prefs.UserState

/** The app's single set of repositories, created once over the APK's assets. */
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

    override fun onCreate() {
        super.onCreate()
        repos = Repos(AndroidAssetSource(this), UserState(this))
        instance = this
    }

    companion object {
        lateinit var instance: App
            private set
        val repos: Repos get() = instance.repos
    }
}
