package pe.pisacoma.iglesia

import androidx.test.platform.app.InstrumentationRegistry
import pe.pisacoma.iglesia.data.prefs.UserState

/** Test helpers over the app's persisted user state. */
object TestState {
    /** Wipes notes, done marks, bookmarks, recents and quiz progress so every test starts fresh. */
    fun clearUserState() {
        val ctx = InstrumentationRegistry.getInstrumentation().targetContext
        ctx.getSharedPreferences(UserState.PREFS_NAME, 0).edit().clear().commit()
    }
}
