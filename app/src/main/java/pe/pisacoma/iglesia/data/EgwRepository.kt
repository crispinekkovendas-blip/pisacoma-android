package pe.pisacoma.iglesia.data

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable
import pe.pisacoma.iglesia.data.model.EgwAisle
import pe.pisacoma.iglesia.data.model.EgwBook
import pe.pisacoma.iglesia.data.model.EgwBookMeta

/** The 25 Ellen G. White books: the site's catalogue (egw-index.json) and one JSON per book. */
class EgwRepository(private val assets: AssetSource) {
    @Serializable private data class IndexFile(val aisles: List<EgwAisle>, val books: List<EgwBookMeta>)

    private var index: IndexFile? = null
    private val cache = mutableMapOf<String, EgwBook?>()
    private val mutex = Mutex()

    private suspend fun index(): IndexFile = mutex.withLock {
        index ?: AppJson.decodeFromString<IndexFile>(assets.read("data/egw-index.json")).also { index = it }
    }

    suspend fun catalogue(): List<EgwBookMeta> = index().books
    suspend fun aisles(): List<EgwAisle> = index().aisles
    suspend fun meta(slug: String): EgwBookMeta? = catalogue().firstOrNull { it.slug == slug }

    /** The four «pasillos» in the site's order, each with its books. */
    suspend fun rails(): List<Pair<EgwAisle, List<EgwBookMeta>>> {
        val i = index()
        return i.aisles.map { a -> a to i.books.filter { it.aisle == a.roman } }
    }

    suspend fun book(slug: String): EgwBook? = mutex.withLock {
        cache.getOrPut(slug) {
            if (!assets.exists("data/egw/$slug.json")) null
            else AppJson.decodeFromString<EgwBook>(assets.read("data/egw/$slug.json"))
        }
    }
}
