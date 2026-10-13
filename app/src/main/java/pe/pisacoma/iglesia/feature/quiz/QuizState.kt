package pe.pisacoma.iglesia.feature.quiz

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import pe.pisacoma.iglesia.data.model.QuizLevel
import pe.pisacoma.iglesia.data.model.QuizPack

/**
 * The site's quiz-view.tsx state machine, as an immutable value: a pick opens the confirm modal
 * ([pending]); confirming locks the answer in [committed]; nothing can change afterwards;
 * «Ver resultados» sets [submitted]. Persisted under `quiz.<slug>.v1` as
 * `{"committed":{"1":"B"},"submitted":false}`.
 */
data class QuizState(
    val committed: Map<Int, String> = emptyMap(),
    val submitted: Boolean = false,
    val pending: Pending? = null,
) {
    data class Pending(val qN: Int, val letter: String)

    val committedCount: Int get() = committed.size

    fun requestCommit(qN: Int, letter: String): QuizState =
        if (committed.containsKey(qN) || submitted) this else copy(pending = Pending(qN, letter))

    fun cancel(): QuizState = copy(pending = null)

    fun confirm(): QuizState {
        val p = pending ?: return this
        return copy(committed = committed + (p.qN to p.letter), pending = null)
    }

    fun submit(): QuizState = copy(submitted = true, pending = null)

    fun reset(): QuizState = QuizState()

    fun score(pack: QuizPack): Int = pack.questions.count { committed[it.n] == it.correct }
    fun allCommitted(pack: QuizPack): Boolean = committedCount == pack.questions.size
    fun pct(pack: QuizPack): Int = if (pack.questions.isEmpty()) 0 else Math.round(committedCount * 100f / pack.questions.size)

    fun toJson(): String {
        val obj = JsonObject(
            mapOf(
                "committed" to JsonObject(committed.toSortedMap().map { (k, v) -> k.toString() to JsonPrimitive(v) }.toMap()),
                "submitted" to JsonPrimitive(submitted),
            ),
        )
        return obj.toString()
    }

    companion object {
        /** The site's DEFAULT_LEVELS, used when a pack has none. */
        val DEFAULT_LEVELS = listOf(
            QuizLevel(14, "Caleb", "¡Excelente! Conoces bien la lección de esta semana."),
            QuizLevel(11, "Daniel", "Muy buen dominio. Un repaso más y llegas al nivel Caleb."),
            QuizLevel(8, "Débora", "Vas bien. Vuelve a la lección diaria para afianzar."),
            QuizLevel(0, "Josías", "Sigue estudiando — cada semana es una nueva oportunidad."),
        )

        /** Highest minScore reached, like the site's results card. */
        fun tier(score: Int, levels: List<QuizLevel>): QuizLevel {
            val sorted = levels.sortedByDescending { it.minScore }
            return sorted.firstOrNull { score >= it.minScore } ?: sorted.last()
        }

        fun fromJson(raw: String?): QuizState {
            if (raw.isNullOrBlank()) return QuizState()
            return runCatching {
                val o = Json.parseToJsonElement(raw).jsonObject
                val committed = (o["committed"] as? JsonObject)?.mapNotNull { (k, v) ->
                    val n = k.toIntOrNull() ?: return@mapNotNull null
                    val letter = (v as? JsonPrimitive)?.content ?: return@mapNotNull null
                    if (letter.isEmpty()) null else n to letter
                }?.toMap() ?: emptyMap()
                val submitted = (o["submitted"] as? JsonPrimitive)?.let { runCatching { it.boolean }.getOrDefault(false) } ?: false
                QuizState(committed, submitted)
            }.getOrDefault(QuizState())
        }
    }
}
