package pe.pisacoma.iglesia.data.prefs

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive

/** A string key/value store: SharedPreferences on a phone, a map in tests. */
interface KeyValueStore {
    fun get(key: String): String?
    fun put(key: String, value: String?)
    fun all(): Map<String, String>
    /** Called with the key after every put. */
    fun addListener(listener: (String) -> Unit)
    fun removeListener(listener: (String) -> Unit)
}

class PrefsKeyValueStore(private val prefs: SharedPreferences) : KeyValueStore {
    private val wrappers = mutableMapOf<(String) -> Unit, SharedPreferences.OnSharedPreferenceChangeListener>()
    override fun get(key: String): String? = prefs.getString(key, null)
    override fun put(key: String, value: String?) {
        prefs.edit().apply { if (value == null) remove(key) else putString(key, value) }.apply()
    }
    override fun all(): Map<String, String> = prefs.all.mapNotNull { (k, v) -> (v as? String)?.let { k to it } }.toMap()
    override fun addListener(listener: (String) -> Unit) {
        val w = SharedPreferences.OnSharedPreferenceChangeListener { _, k -> if (k != null) listener(k) }
        wrappers[listener] = w
        prefs.registerOnSharedPreferenceChangeListener(w)
    }
    override fun removeListener(listener: (String) -> Unit) {
        wrappers.remove(listener)?.let { prefs.unregisterOnSharedPreferenceChangeListener(it) }
    }
}

/**
 * Everything the reader saves — notes, done marks, bookmarks, recents, quiz progress — under
 * the site's localStorage key names, so the two surfaces store the same thing the same way.
 * Booleans are "1"/"0" and lists are JSON arrays, as the site writes them.
 */
class UserState(private val store: KeyValueStore) {
    constructor(context: Context) : this(PrefsKeyValueStore(context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)))

    fun string(key: String): String? = store.get(key)
    fun set(key: String, value: String?) = store.put(key, value)

    fun bool(key: String): Boolean = store.get(key).let { it == "1" || it == "true" }
    fun set(key: String, value: Boolean) = store.put(key, if (value) "1" else "0")

    fun stringList(key: String): List<String> = runCatching {
        val raw = store.get(key) ?: return emptyList()
        Json.parseToJsonElement(raw).jsonArray.map { it.jsonPrimitive.content }
    }.getOrDefault(emptyList())

    fun set(key: String, value: List<String>) = store.put(key, JsonArray(value.map { JsonPrimitive(it) }).toString())

    /** Keys currently stored that start with `prefix` (e.g. every `egw:cc:ch` key). */
    fun keysWithPrefix(prefix: String): List<String> = store.all().keys.filter { it.startsWith(prefix) }

    /** Current value, then every change of that key. */
    fun flow(key: String): Flow<String?> = callbackFlow {
        trySend(store.get(key))
        val l: (String) -> Unit = { k -> if (k == key) trySend(store.get(key)) }
        store.addListener(l)
        awaitClose { store.removeListener(l) }
    }

    /** Every change to any key (the value is the key), for screens that aggregate many keys. */
    fun changes(): Flow<String> = callbackFlow {
        val l: (String) -> Unit = { k -> trySend(k) }
        store.addListener(l)
        awaitClose { store.removeListener(l) }
    }

    object Keys {
        fun bibleNotes(book: Int, chapter: Int) = "biblia:bk$book:ch$chapter:notes"
        const val bibleBookmarks = "biblia:bookmarks"
        const val bibleRecent = "biblia:recent"
        const val bibleHasNotesIndex = "biblia:hasNotesIndex"
        fun egwNotes(slug: String, chapter: Int) = "egw:$slug:ch$chapter:notes"
        fun egwDone(slug: String, chapter: Int) = "egw:$slug:ch$chapter:done"
        fun egwLast(slug: String) = "egw:$slug:last"
        const val egwBookmarks = "egw:bookmarks"
        fun lessonDone(qid: String, lesson: Int, day: Int) = "escuela:$qid:lecc$lesson:day$day:done"
        fun lessonNotes(qid: String, lesson: Int, day: Int) = "escuela:$qid:lecc$lesson:day$day:notes"
        fun lessonLastDay(qid: String, lesson: Int) = "escuela:$qid:lecc$lesson:lastDay"
        fun lessonBookmarks(qid: String) = "escuela:$qid:bookmarks"
        fun quiz(slug: String) = "quiz.$slug.v1"
        const val hymnRecent = "himnario.recent"
        const val homePaused = "inicio.videos.pausa"
        const val videosCache = "videos:cache"
        const val videosCacheAt = "videos:cacheAt"
        const val quienNames = "quien:names"
        /** «todo» | «esencial» | «uso» | «en-linea»; unset until the first open's choice. */
        const val contentMode = "contenido.modo"
        /** "1" while the chosen packs are not fully stored (the download resumes on the next open). */
        const val contentPending = "contenido.pendiente"
    }

    companion object {
        const val PREFS_NAME = "pisacoma"
    }
}
