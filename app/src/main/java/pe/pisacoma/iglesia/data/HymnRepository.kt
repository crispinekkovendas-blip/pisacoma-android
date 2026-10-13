package pe.pisacoma.iglesia.data

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import pe.pisacoma.iglesia.data.model.Hymn
import pe.pisacoma.iglesia.data.model.HymnVariant
import pe.pisacoma.iglesia.data.model.LyricBlock

/**
 * The three hymnals from the bundled JSON (the site's src/data files, copied verbatim).
 * Spanish and old-Portuguese hymns store `verses[]` where verseNumber 0 is the chorus;
 * the NHA stores `stanzas[]` with explicit stanza/chorus blocks. Both become [LyricBlock]s
 * in the order the site prints them (first stanza, chorus, remaining stanzas).
 */
class HymnRepository(private val assets: AssetSource) {
    private val cache = mutableMapOf<HymnVariant, List<Hymn>>()
    private val mutex = Mutex()
    private var featured: Map<String, List<Int>>? = null

    suspend fun all(variant: HymnVariant): List<Hymn> = mutex.withLock {
        cache.getOrPut(variant) { load(variant) }
    }

    suspend fun get(variant: HymnVariant, number: Int): Hymn? = all(variant).firstOrNull { it.number == number }

    /** The site's FEATURED_HYMNS picks for the hymnal index. */
    suspend fun featured(variant: HymnVariant): List<Int> {
        val map = featured ?: AppJson.decodeFromString<Map<String, List<Int>>>(assets.read("data/featured-hymns.json")).also { featured = it }
        return map[variant.slug] ?: emptyList()
    }

    private suspend fun load(variant: HymnVariant): List<Hymn> {
        val root = AppJson.parseToJsonElement(assets.read("data/${variant.file}.json"))
        val entries: JsonArray = if (root is JsonObject) root["hymns"]!!.jsonArray else root.jsonArray
        return entries.map { el -> parse(variant, el.jsonObject) }
    }

    private fun parse(variant: HymnVariant, o: JsonObject): Hymn {
        val number = o["number"]!!.jsonPrimitive.let { it.intOrNull ?: it.content.toInt() }
        val blocks: List<LyricBlock> = when {
            o["verses"] is JsonArray -> {
                val verses = o["verses"]!!.jsonArray.map { v ->
                    val vo = v.jsonObject
                    val n = vo["verseNumber"]!!.jsonPrimitive.int
                    val lines = vo["contents"]!!.jsonArray
                        .flatMap { c -> c.jsonPrimitive.content.split("\n") }
                        .filter { it.isNotBlank() }
                    n to lines
                }
                val chorus = verses.firstOrNull { it.first == 0 }
                val stanzas = verses.filter { it.first != 0 }
                val ordered = if (chorus != null && stanzas.isNotEmpty()) listOf(stanzas[0], chorus) + stanzas.drop(1) else verses
                ordered.map { (n, lines) -> LyricBlock(if (n == 0) "chorus" else "stanza", lines, if (n == 0) null else n) }
            }
            o["stanzas"] is JsonArray -> {
                var n = 0
                o["stanzas"]!!.jsonArray.map { s ->
                    val so = s.jsonObject
                    val type = so["type"]?.jsonPrimitive?.contentOrNull ?: "stanza"
                    val lines = so["lines"]!!.jsonArray.map { it.jsonPrimitive.content }
                    LyricBlock(type, lines, if (type == "chorus") null else ++n)
                }
            }
            else -> {
                val lyrics = o["lyrics"]?.jsonPrimitive?.contentOrNull ?: ""
                lyrics.split(Regex("\\n\\s*\\n")).mapIndexedNotNull { i, block ->
                    val lines = block.split("\n").filter { it.isNotBlank() }
                    if (lines.isEmpty()) null else LyricBlock("stanza", lines, i + 1)
                }
            }
        }
        return Hymn(
            variant = variant,
            number = number,
            title = o["title"]!!.jsonPrimitive.content,
            bibleReference = o["bibleReference"]?.jsonPrimitive?.contentOrNull?.takeIf { it.isNotBlank() },
            mp3Url = o["mp3Url"]?.jsonPrimitive?.contentOrNull?.takeIf { it.isNotBlank() },
            mp3UrlInstr = o["mp3UrlInstr"]?.jsonPrimitive?.contentOrNull?.takeIf { it.isNotBlank() },
            ytId = o["ytId"]?.jsonPrimitive?.contentOrNull?.takeIf { it.isNotBlank() },
            blocks = blocks,
        )
    }
}

private val JsonPrimitive.int: Int get() = intOrNull ?: content.toInt()
