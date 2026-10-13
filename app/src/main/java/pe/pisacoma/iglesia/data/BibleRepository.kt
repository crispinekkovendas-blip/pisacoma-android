package pe.pisacoma.iglesia.data

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable
import pe.pisacoma.iglesia.data.model.BibleBook

/** Reina-Valera 1960, one bundled JSON per book (`{"b": 1, "c": [[verse, …], …]}`), like the site. */
class BibleRepository(private val assets: AssetSource) {
    @Serializable private data class BookFile(val b: Int, val c: List<List<String>>)

    private var books: List<BibleBook>? = null
    private val cache = mutableMapOf<Int, BookFile>()
    private val mutex = Mutex()

    suspend fun books(): List<BibleBook> = mutex.withLock {
        books ?: AppJson.decodeFromString<List<BibleBook>>(assets.read("data/bible-books.json")).also { books = it }
    }

    suspend fun book(num: Int): BibleBook? = books().firstOrNull { it.num == num }

    /** Verses 1..n of a chapter (empty when the chapter does not exist). */
    suspend fun chapter(book: Int, chapter: Int): List<String> {
        val file = mutex.withLock {
            cache.getOrPut(book) { AppJson.decodeFromString<BookFile>(assets.read("data/biblia/$book.json")) }
        }
        return file.c.getOrNull(chapter - 1) ?: emptyList()
    }

    /** (verse number, text) for `from..to`, clamped to the chapter. */
    suspend fun verses(book: Int, chapter: Int, from: Int, to: Int): List<Pair<Int, String>> {
        val all = chapter(book, chapter)
        val last = maxOf(from, to)
        return all.mapIndexed { i, t -> (i + 1) to t }.filter { it.first in from..last }
    }

    /** Aymara Bible («Qullan Arunaca») on Bible.com, as the site links it. */
    fun aymaraUrl(book: Int, chapter: Int): String = "https://www.bible.com/bible/293/${USFM[book - 1]}.$chapter.AYOP"

    companion object {
        private val USFM = listOf(
            "GEN", "EXO", "LEV", "NUM", "DEU", "JOS", "JDG", "RUT", "1SA", "2SA", "1KI", "2KI", "1CH", "2CH", "EZR", "NEH",
            "EST", "JOB", "PSA", "PRO", "ECC", "SNG", "ISA", "JER", "LAM", "EZK", "DAN", "HOS", "JOL", "AMO", "OBA", "JON",
            "MIC", "NAM", "HAB", "ZEP", "HAG", "ZEC", "MAL", "MAT", "MRK", "LUK", "JHN", "ACT", "ROM", "1CO", "2CO", "GAL",
            "EPH", "PHP", "COL", "1TH", "2TH", "1TI", "2TI", "TIT", "PHM", "HEB", "JAS", "1PE", "2PE", "1JN", "2JN", "3JN",
            "JUD", "REV",
        )
    }
}
