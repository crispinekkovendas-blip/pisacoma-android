package pe.pisacoma.iglesia.data

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import pe.pisacoma.iglesia.data.model.SearchIndex

/** The site's search-index.json (built by its scripts/gen-search-index.mjs), parsed into typed rows. */
class SearchRepository(private val assets: AssetSource) {
    private var index: SearchIndex? = null
    private val mutex = Mutex()

    suspend fun index(): SearchIndex = mutex.withLock {
        index ?: parse(assets.read("data/search-index.json")).also { index = it }
    }

    @Serializable private data class EgwRow(val s: String, val t: String, val c: List<JsonElement>)
    @Serializable private data class LessonRow(val n: Int, val t: String, val d: List<String>)
    @Serializable private data class Quarter(val id: String, val title: String)
    @Serializable private data class File(val v: Int, val quarter: Quarter, val hymns: List<JsonElement>, val egw: List<EgwRow>, val lessons: List<LessonRow>, val bible: List<JsonElement>)

    private fun parse(text: String): SearchIndex {
        val f = AppJson.decodeFromString<File>(text)
        return SearchIndex(
            v = f.v,
            quarter = SearchIndex.Quarter(f.quarter.id, f.quarter.title),
            hymns = f.hymns.map { e ->
                val a = e.jsonArray
                SearchIndex.HymnRow(
                    variant = a[0].jsonPrimitive.content,
                    number = a[1].jsonPrimitive.intOrNull ?: a[1].jsonPrimitive.content.toInt(),
                    title = a[2].jsonPrimitive.content,
                    lines = (a.getOrNull(3) as? JsonArray)?.map { it.jsonPrimitive.content } ?: emptyList(),
                )
            },
            egw = f.egw.map { r ->
                SearchIndex.EgwRow(r.s, r.t, r.c.map { c -> val ca = c.jsonArray; ca[0].jsonPrimitive.intOrNull!! to ca[1].jsonPrimitive.content })
            },
            lessons = f.lessons.map { SearchIndex.LessonRow(it.n, it.t, it.d) },
            bible = f.bible.map { e ->
                val a = e.jsonArray
                SearchIndex.BibleRow(a[0].jsonPrimitive.intOrNull!!, a[1].jsonPrimitive.content, a[2].jsonPrimitive.intOrNull!!, a[3].jsonPrimitive.contentOrNull ?: "")
            },
        )
    }
}
