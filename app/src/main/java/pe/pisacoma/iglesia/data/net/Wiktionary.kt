package pe.pisacoma.iglesia.data.net

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.net.URLEncoder

data class Definition(val definition: String, val examples: List<String>)
data class DictSection(val partOfSpeech: String, val language: String?, val definitions: List<Definition>)

sealed class DictResult {
    data class Found(val sections: List<DictSection>) : DictResult()
    data class Error(val message: String) : DictResult()
}

/** The site's diccionario route: es.wiktionary.org REST definitions, same section choice and messages. */
object Wiktionary {
    const val NETWORK_ERROR = "Error de red. Intenta de nuevo."
    fun notFoundMessage(word: String) = "«$word» — no encontramos esta palabra en el Wiccionario en español."
    fun emptyMessage(word: String) = "«$word» no tiene definiciones registradas."

    /** Site POPULAR, in order. */
    val SUGGESTED = listOf(
        "amor", "fe", "esperanza", "gracia", "salvación", "redención", "justicia", "misericordia", "pacto", "sábado",
        "iglesia", "profeta", "apóstol", "evangelio", "reino", "oración", "arrepentimiento", "perdón", "santificación",
        "bautismo", "alma", "espíritu", "pecado", "verdad", "luz", "alabanza", "adoración", "gloria", "santo", "fiel",
    )

    fun url(word: String): String = "https://es.wiktionary.org/api/rest_v1/page/definition/" + URLEncoder.encode(word, "UTF-8").replace("+", "%20")

    private val TAGS = Regex("<[^>]+>")
    private fun strip(s: String) = TAGS.replace(s, "").replace("&nbsp;", " ").replace("&amp;", "&").replace("&quot;", "\"").trim()

    /** `es` | `Spanish` | the first language; empty on anything unexpected. */
    fun parse(body: String): List<DictSection> = runCatching {
        val root = Json.parseToJsonElement(body).jsonObject
        val arr: JsonArray = (root["es"] ?: root["Spanish"] ?: root.values.firstOrNull()) as? JsonArray ?: return emptyList()
        arr.mapNotNull { el ->
            val o = el as? JsonObject ?: return@mapNotNull null
            DictSection(
                partOfSpeech = o["partOfSpeech"]?.jsonPrimitive?.contentOrNull ?: "",
                language = o["language"]?.jsonPrimitive?.contentOrNull,
                definitions = (o["definitions"] as? JsonArray)?.mapNotNull { d ->
                    val dobj = d as? JsonObject ?: return@mapNotNull null
                    Definition(
                        strip(dobj["definition"]?.jsonPrimitive?.contentOrNull ?: ""),
                        (dobj["examples"] as? JsonArray)?.map { strip(it.jsonPrimitive.content) } ?: emptyList(),
                    )
                } ?: emptyList(),
            )
        }
    }.getOrDefault(emptyList())

    suspend fun lookup(word: String): DictResult {
        val w = word.trim().lowercase()
        return try {
            val sections = parse(Http.get(url(w)))
            if (sections.isEmpty()) DictResult.Error(emptyMessage(w)) else DictResult.Found(sections)
        } catch (e: Http.HttpStatus) {
            if (e.code == 404) DictResult.Error(notFoundMessage(w)) else DictResult.Error(NETWORK_ERROR)
        } catch (e: Exception) {
            DictResult.Error(NETWORK_ERROR)
        }
    }
}
