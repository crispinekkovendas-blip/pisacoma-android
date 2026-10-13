package pe.pisacoma.iglesia.data.model

import kotlinx.serialization.Serializable

/** One row of the site's bible-books.json. Books 1–39 are the Old Testament. */
@Serializable
data class BibleBook(val num: Int, val name: String, val chapters: Int, val abbr: String) {
    val isOldTestament: Boolean get() = num <= 39
}
