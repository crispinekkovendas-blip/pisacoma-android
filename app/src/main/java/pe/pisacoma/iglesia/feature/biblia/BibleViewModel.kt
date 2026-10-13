package pe.pisacoma.iglesia.feature.biblia

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import pe.pisacoma.iglesia.data.AppJson
import pe.pisacoma.iglesia.data.BibleRepository
import pe.pisacoma.iglesia.data.model.BibleBook
import pe.pisacoma.iglesia.data.prefs.UserState

/** `biblia:bookmarks` entries, as the site writes them. */
@Serializable
data class BibleBookmark(val bookNum: Int, val chapter: Int, val bookName: String, val addedAt: Long)

/** `biblia:recent` entries (max 20), as the site writes them. */
@Serializable
data class RecentChapter(val bookNum: Int, val chapter: Int, val bookName: String, val viewedAt: Long)

data class BibleUi(
    val books: List<BibleBook> = emptyList(),
    val bookNum: Int = 43,
    val chapter: Int = 3,
    val verses: List<Pair<Int, String>> = emptyList(),
    val loading: Boolean = true,
    val targetVerse: Int = 0,
    val scale: Float = 1.05f,
    val bookmarks: List<BibleBookmark> = emptyList(),
    val recent: List<RecentChapter> = emptyList(),
    /** "book:chapter" keys that have notes (`biblia:hasNotesIndex`). */
    val hasNotes: List<String> = emptyList(),
    val notes: String = "",
    val savedAt: Long? = null,
) {
    val book: BibleBook? get() = books.firstOrNull { it.num == bookNum }
    val isBookmarked get() = bookmarks.any { it.bookNum == bookNum && it.chapter == chapter }
    val currentHasNotes get() = "$bookNum:$chapter" in hasNotes
    val isFirst get() = bookNum == 1 && chapter == 1
    val isLast get() = bookNum == 66 && chapter == (book?.chapters ?: 1)
}

/** The site's biblia/index.tsx state: book/chapter, deep-link verse, notes, bookmarks, recents. */
class BibleViewModel(private val bible: BibleRepository, private val userState: UserState, book: Int?, chapter: Int?, verse: Int?) : ViewModel() {
    private val _state = MutableStateFlow(BibleUi(targetVerse = verse ?: 0))
    val state: StateFlow<BibleUi> = _state

    init {
        viewModelScope.launch {
            val books = bible.books()
            val b = (book ?: 43).coerceIn(1, 66)
            val max = books.first { it.num == b }.chapters
            val c = (chapter ?: if (b == 43) 3 else 1).coerceIn(1, max)
            _state.update { it.copy(books = books, bookNum = b, chapter = c, bookmarks = readList(UserState.Keys.bibleBookmarks), recent = readList(UserState.Keys.bibleRecent), hasNotes = userState.stringList(UserState.Keys.bibleHasNotesIndex)) }
            open(b, c, keepTarget = true)
        }
    }

    private inline fun <reified T> readList(key: String): List<T> = runCatching {
        userState.string(key)?.let { AppJson.decodeFromString<List<T>>(it) } ?: emptyList()
    }.getOrDefault(emptyList())

    fun open(bookNum: Int, chapter: Int, keepTarget: Boolean = false) {
        viewModelScope.launch {
            _state.update { it.copy(bookNum = bookNum, chapter = chapter, loading = true, targetVerse = if (keepTarget) it.targetVerse else 0, savedAt = null) }
            val verses = bible.verses(bookNum, chapter, 1, 999)
            val name = _state.value.books.firstOrNull { it.num == bookNum }?.name ?: ""
            // recent (max 20, newest first) — site useVisibleTask on book/chapter
            val recent = (listOf(RecentChapter(bookNum, chapter, name, System.currentTimeMillis())) + _state.value.recent.filter { !(it.bookNum == bookNum && it.chapter == chapter) }).take(20)
            userState.set(UserState.Keys.bibleRecent, AppJson.encodeToString(recent))
            _state.update { it.copy(verses = verses, loading = false, recent = recent, notes = userState.string(UserState.Keys.bibleNotes(bookNum, chapter)) ?: "") }
        }
    }

    fun prev() {
        val s = _state.value
        if (s.chapter > 1) open(s.bookNum, s.chapter - 1)
        else if (s.bookNum > 1) open(s.bookNum - 1, s.books.first { it.num == s.bookNum - 1 }.chapters)
    }

    fun next() {
        val s = _state.value
        val max = s.book?.chapters ?: 1
        if (s.chapter < max) open(s.bookNum, s.chapter + 1)
        else if (s.bookNum < 66) open(s.bookNum + 1, 1)
    }

    /** Tapping a verse selects it (for sharing) or clears the selection. */
    fun tapVerse(v: Int) = _state.update { it.copy(targetVerse = if (it.targetVerse == v) 0 else v) }

    fun smaller() = _state.update { it.copy(scale = maxOf(0.9f, it.scale - 0.08f)) }
    fun larger() = _state.update { it.copy(scale = minOf(1.5f, it.scale + 0.08f)) }

    fun toggleBookmark() {
        val s = _state.value
        val next = if (s.isBookmarked) s.bookmarks.filter { !(it.bookNum == s.bookNum && it.chapter == s.chapter) }
        else listOf(BibleBookmark(s.bookNum, s.chapter, s.book?.name ?: "", System.currentTimeMillis())) + s.bookmarks
        userState.set(UserState.Keys.bibleBookmarks, AppJson.encodeToString(next))
        _state.update { it.copy(bookmarks = next) }
    }

    fun removeBookmark(bookNum: Int, chapter: Int) {
        val next = _state.value.bookmarks.filter { !(it.bookNum == bookNum && it.chapter == chapter) }
        userState.set(UserState.Keys.bibleBookmarks, AppJson.encodeToString(next))
        _state.update { it.copy(bookmarks = next) }
    }

    fun notes(value: String) {
        val s = _state.value
        val key = "${s.bookNum}:${s.chapter}"
        val idx = if (value.isBlank()) s.hasNotes.filter { it != key } else if (key in s.hasNotes) s.hasNotes else s.hasNotes + key
        userState.set(UserState.Keys.bibleNotes(s.bookNum, s.chapter), value.ifBlank { null })
        userState.set(UserState.Keys.bibleHasNotesIndex, idx)
        _state.update { it.copy(notes = value, hasNotes = idx, savedAt = if (value.isBlank()) null else System.currentTimeMillis()) }
    }

    fun clearNotes() = notes("")

    fun aymaraUrl(): String = bible.aymaraUrl(_state.value.bookNum, _state.value.chapter)
}
