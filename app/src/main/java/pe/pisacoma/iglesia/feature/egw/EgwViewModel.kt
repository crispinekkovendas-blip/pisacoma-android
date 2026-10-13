package pe.pisacoma.iglesia.feature.egw

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import pe.pisacoma.iglesia.data.AppJson
import pe.pisacoma.iglesia.data.EgwRepository
import pe.pisacoma.iglesia.data.model.EgwAisle
import pe.pisacoma.iglesia.data.model.EgwBook
import pe.pisacoma.iglesia.data.model.EgwBookMeta
import pe.pisacoma.iglesia.data.prefs.UserState
import pe.pisacoma.iglesia.ui.components.TextScale

/** Site egw/index.tsx FEATURED: the four covers with a badge. */
val EGW_FEATURED = setOf("gc", "pvgm", "hc", "msv")

/** `egw:bookmarks` entries, as the site writes them. */
@Serializable
data class EgwBookmark(val slug: String, val bookId: String, val bookTitle: String, val chapter: Int, val chapterTitle: String, val addedAt: Long)

/** Reading progress of one book (site stats[slug]). */
data class BookStats(val done: Int, val lastCh: Int?) {
    val hasProgress get() = done > 0 || lastCh != null
}

data class CatalogUi(
    val rails: List<Pair<EgwAisle, List<EgwBookMeta>>> = emptyList(),
    val stats: Map<String, BookStats> = emptyMap(),
) {
    val books get() = rails.flatMap { it.second }
    val totalChapters get() = books.sumOf { it.chapters }
    val totalDone get() = stats.values.sumOf { it.done }
    val overallPct get() = if (totalChapters == 0) 0 else Math.round(totalDone * 100f / totalChapters)
}

class EgwCatalogViewModel(private val egw: EgwRepository, private val userState: UserState) : ViewModel() {
    private val _state = MutableStateFlow(CatalogUi())
    val state: StateFlow<CatalogUi> = _state

    init {
        refresh()
        viewModelScope.launch { userState.changes().collect { key -> if (key.startsWith("egw:")) refresh() } }
    }

    fun refresh() = viewModelScope.launch {
        val rails = egw.rails()
        val stats = rails.flatMap { it.second }.associate { b ->
            val done = (1..b.chapters).count { userState.string(UserState.Keys.egwDone(b.slug, it)) != null }
            val last = userState.string(UserState.Keys.egwLast(b.slug))?.toIntOrNull()
            b.slug to BookStats(done, last)
        }
        _state.value = CatalogUi(rails, stats)
    }
}

data class BookUi(
    val meta: EgwBookMeta? = null,
    val book: EgwBook? = null,
    val chapter: Int = 1,
    val done: Set<Int> = emptySet(),
    val hasNotes: Set<Int> = emptySet(),
    val bookmarks: List<EgwBookmark> = emptyList(),
    val notes: String = "",
    val savedAt: Long? = null,
    val scale: Float = TextScale.DEFAULT,
    val notFound: Boolean = false,
) {
    val total get() = book?.chapters?.size ?: 0
    val current get() = book?.chapters?.firstOrNull { it.number == chapter }
    val completedCount get() = done.size
    val pct get() = if (total == 0) 0 else Math.round(completedCount * 100f / total)
    val isDone get() = chapter in done
    val isBookmarked get() = bookmarks.any { it.slug == meta?.slug && it.chapter == chapter }
}

/** Site egw/[slug]/index.tsx: chapter from `?ch=`, else the last read; done/notes/bookmarks under the site's keys. */
class EgwBookViewModel(private val egw: EgwRepository, private val userState: UserState, private val slug: String, initialChapter: Int?) : ViewModel() {
    private val _state = MutableStateFlow(BookUi())
    val state: StateFlow<BookUi> = _state

    init {
        viewModelScope.launch {
            val meta = egw.meta(slug)
            val book = egw.book(slug)
            if (meta == null || book == null) { _state.update { it.copy(notFound = true) }; return@launch }
            val numbers = book.chapters.map { it.number }
            val ch = (initialChapter ?: userState.string(UserState.Keys.egwLast(slug))?.toIntOrNull())?.takeIf { it in numbers } ?: numbers.first()
            _state.update {
                it.copy(
                    meta = meta, book = book, chapter = ch,
                    done = numbers.filter { n -> userState.string(UserState.Keys.egwDone(slug, n)) != null }.toSet(),
                    hasNotes = numbers.filter { n -> !userState.string(UserState.Keys.egwNotes(slug, n)).isNullOrEmpty() }.toSet(),
                    bookmarks = readBookmarks(),
                    notes = userState.string(UserState.Keys.egwNotes(slug, ch)) ?: "",
                )
            }
            userState.set(UserState.Keys.egwLast(slug), ch.toString())
        }
    }

    private fun readBookmarks(): List<EgwBookmark> = runCatching {
        userState.string(UserState.Keys.egwBookmarks)?.let { AppJson.decodeFromString<List<EgwBookmark>>(it) } ?: emptyList()
    }.getOrDefault(emptyList())

    fun open(ch: Int) {
        val book = _state.value.book ?: return
        if (book.chapters.none { it.number == ch }) return
        _state.update { it.copy(chapter = ch, notes = userState.string(UserState.Keys.egwNotes(slug, ch)) ?: "", savedAt = null) }
        userState.set(UserState.Keys.egwLast(slug), ch.toString())
    }

    fun prev() = open(_state.value.chapter - 1)
    fun next() = open(_state.value.chapter + 1)

    fun toggleDone() {
        val ch = _state.value.chapter
        val key = UserState.Keys.egwDone(slug, ch)
        if (ch in _state.value.done) { userState.set(key, null); _state.update { it.copy(done = it.done - ch) } }
        else { userState.set(key, System.currentTimeMillis().toString()); _state.update { it.copy(done = it.done + ch) } }
    }

    fun toggleBookmark() {
        val s = _state.value
        val meta = s.meta ?: return
        val cur = s.current ?: return
        val next = if (s.isBookmarked) s.bookmarks.filter { !(it.slug == slug && it.chapter == s.chapter) }
        else listOf(EgwBookmark(slug, meta.id, meta.title, s.chapter, cur.title, System.currentTimeMillis())) + s.bookmarks
        userState.set(UserState.Keys.egwBookmarks, AppJson.encodeToString(next))
        _state.update { it.copy(bookmarks = next) }
    }

    fun removeBookmark(bmSlug: String, chapter: Int) {
        val next = _state.value.bookmarks.filter { !(it.slug == bmSlug && it.chapter == chapter) }
        userState.set(UserState.Keys.egwBookmarks, AppJson.encodeToString(next))
        _state.update { it.copy(bookmarks = next) }
    }

    fun notes(value: String) {
        val ch = _state.value.chapter
        userState.set(UserState.Keys.egwNotes(slug, ch), value.ifBlank { null })
        _state.update { it.copy(notes = value, hasNotes = if (value.isBlank()) it.hasNotes - ch else it.hasNotes + ch, savedAt = if (value.isBlank()) null else System.currentTimeMillis()) }
    }

    fun clearNotes() = notes("")
    fun smaller() = _state.update { it.copy(scale = TextScale.smaller(it.scale)) }
    fun larger() = _state.update { it.copy(scale = TextScale.larger(it.scale)) }

    /** The site's «Reiniciar progreso» in the drawer: clears every done mark of this book. */
    fun resetProgress() {
        val book = _state.value.book ?: return
        book.chapters.forEach { userState.set(UserState.Keys.egwDone(slug, it.number), null) }
        _state.update { it.copy(done = emptySet()) }
    }
}
