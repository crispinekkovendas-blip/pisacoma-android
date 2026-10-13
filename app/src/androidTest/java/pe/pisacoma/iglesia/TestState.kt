package pe.pisacoma.iglesia

import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import pe.pisacoma.iglesia.data.prefs.UserState
import java.io.File

/** Test helpers over the app's persisted user state and its content store. */
object TestState {
    private val target get() = InstrumentationRegistry.getInstrumentation().targetContext
    private val prefs get() = target.getSharedPreferences(UserState.PREFS_NAME, 0)
    private val contentDir get() = File(target.filesDir, "content")

    /** Wipes notes, done marks, bookmarks, recents and quiz progress; keeps the content mode «todo». */
    fun clearUserState() {
        prefs.edit().clear().putString(UserState.Keys.contentMode, "todo").putString(UserState.Keys.contentPending, "0").commit()
    }

    fun setMode(mode: String?) {
        prefs.edit().apply { if (mode == null) remove(UserState.Keys.contentMode) else putString(UserState.Keys.contentMode, mode) }
            .putString(UserState.Keys.contentPending, "0").commit()
    }

    /** The state of a phone that has never opened the app: no mode chosen, nothing stored. */
    fun firstRun() {
        setMode(null)
        wipeContent()
    }

    fun mode(): String? = prefs.getString(UserState.Keys.contentMode, null)

    /** Copies every manifest file from the test APK's assets into the app's store (skips files already there). */
    fun seedContent() {
        val testAssets = InstrumentationRegistry.getInstrumentation().context.assets
        val manifest = testAssets.open("manifest.json").bufferedReader(Charsets.UTF_8).use { it.readText() }
        val files = Json.parseToJsonElement(manifest).jsonObject["files"]!!.jsonArray.map { it.jsonObject }
        for (f in files) {
            val path = f["path"]!!.jsonPrimitive.content
            val bytes = f["bytes"]!!.jsonPrimitive.long
            val out = File(contentDir, path)
            if (out.isFile && out.length() == bytes) continue
            out.parentFile?.mkdirs()
            testAssets.open(path).use { input -> out.outputStream().use { input.copyTo(it) } }
        }
    }

    fun wipeContent() {
        contentDir.deleteRecursively()
    }

    /** Back to the default test state: everything stored, mode «todo». */
    fun restore() {
        seedContent()
        setMode("todo")
    }
}
