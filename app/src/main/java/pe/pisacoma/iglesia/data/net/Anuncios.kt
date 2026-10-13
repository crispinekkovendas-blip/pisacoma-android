package pe.pisacoma.iglesia.data.net

import kotlinx.serialization.Serializable
import pe.pisacoma.iglesia.util.Zmanim
import java.text.Normalizer
import java.util.Calendar
import java.util.TimeZone

/** One announcement (site utils/anuncios.ts Anuncio). Dates are "YYYY-MM-DD" or "DD/MM/YYYY". */
@Serializable
data class Anuncio(val title: String, val date: String, val time: String? = null, val detail: String? = null, val until: String? = null)

/** What the home band shows (site AnuncioView). */
data class AnuncioView(val title: String, val time: String?, val detail: String?, val dateLabel: String, val isToday: Boolean)

/** Port of the site's utils/anuncios.ts: CSV from a published Google Sheet + local entries, the 21-day window. */
object Anuncios {
    private const val LOOKAHEAD_DAYS = 21
    private const val MAX_ITEMS = 5
    private const val DAY = 86_400_000L

    private val ISO = Regex("^(\\d{4})-(\\d{1,2})-(\\d{1,2})$")
    private val DMY = Regex("^(\\d{1,2})/(\\d{1,2})/(\\d{4})$")

    /** 00:00 UTC of a civil date, or null. */
    fun parseDay(s: String?): Long? {
        val v = (s ?: "").trim()
        ISO.find(v)?.let { m -> return utc(m.groupValues[1].toInt(), m.groupValues[2].toInt(), m.groupValues[3].toInt()) }
        DMY.find(v)?.let { m -> return utc(m.groupValues[3].toInt(), m.groupValues[2].toInt(), m.groupValues[1].toInt()) }
        return null
    }

    private fun utc(y: Int, m: Int, d: Int): Long = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { clear(); set(y, m - 1, d, 0, 0, 0) }.timeInMillis

    /** Minimal RFC 4180 parser (quoted fields, "" escapes, CRLF); blank rows dropped. */
    fun parseCsv(text: String): List<List<String>> {
        val rows = mutableListOf<List<String>>()
        var row = mutableListOf<String>()
        val field = StringBuilder()
        var quoted = false
        var i = 0
        while (i < text.length) {
            val c = text[i]
            when {
                quoted -> when {
                    c == '"' && i + 1 < text.length && text[i + 1] == '"' -> { field.append('"'); i++ }
                    c == '"' -> quoted = false
                    else -> field.append(c)
                }
                c == '"' -> quoted = true
                c == ',' -> { row.add(field.toString()); field.clear() }
                c == '\n' || c == '\r' -> {
                    if (c == '\r' && i + 1 < text.length && text[i + 1] == '\n') i++
                    row.add(field.toString()); field.clear(); rows.add(row); row = mutableListOf()
                }
                else -> field.append(c)
            }
            i++
        }
        if (field.isNotEmpty() || row.isNotEmpty()) { row.add(field.toString()); rows.add(row) }
        return rows.filter { r -> r.any { it.isNotBlank() } }
    }

    private val HEADER = mapOf(
        "titulo" to "title", "title" to "title", "anuncio" to "title",
        "fecha" to "date", "date" to "date", "dia" to "date",
        "hora" to "time", "time" to "time",
        "detalle" to "detail", "detalles" to "detail", "descripcion" to "detail", "detail" to "detail",
        "hasta" to "until", "until" to "until", "fin" to "until",
    )
    private val DIACRITICS = Regex("\\p{Mn}+")
    private fun fold(s: String) = DIACRITICS.replace(Normalizer.normalize(s, Normalizer.Form.NFD), "").trim().lowercase()

    fun fromCsv(text: String): List<Anuncio> {
        val rows = parseCsv(text)
        val head = rows.firstOrNull() ?: return emptyList()
        val keys = head.map { HEADER[fold(it)] }
        if ("title" !in keys || "date" !in keys) return emptyList()
        return rows.drop(1).mapNotNull { r ->
            val m = mutableMapOf<String, String>()
            keys.forEachIndexed { i, k -> val v = r.getOrNull(i)?.trim() ?: ""; if (k != null && v.isNotEmpty()) m[k] = v }
            val title = m["title"] ?: return@mapNotNull null
            val date = m["date"] ?: return@mapNotNull null
            if (parseDay(date) == null) return@mapNotNull null
            Anuncio(title, date, m["time"], m["detail"], m["until"])
        }
    }

    /** Current or within the next three weeks, soonest first, at most five (site selectAnuncios). */
    fun select(items: List<Anuncio>, nowUtc: Long): List<AnuncioView> {
        val today = Zmanim.localCivilDate(nowUtc)
        val horizon = today + LOOKAHEAD_DAYS * DAY
        return items
            .mapNotNull { a -> parseDay(a.date)?.let { day -> Triple(a, day, parseDay(a.until) ?: day) } }
            .filter { (_, day, until) -> until >= today && day <= horizon }
            .sortedBy { it.second }
            .take(MAX_ITEMS)
            .map { (a, day, _) -> AnuncioView(a.title, a.time, a.detail, Zmanim.dayLabel(day), day == today) }
    }

    /** Local entries plus the sheet (if configured and reachable), selected for today. */
    suspend fun load(local: List<Anuncio>, csvUrl: String, nowUtc: Long): List<AnuncioView> {
        val sheet = if (csvUrl.isBlank()) emptyList() else runCatching { fromCsv(Http.get(csvUrl)) }.getOrDefault(emptyList())
        return select(local + sheet, nowUtc)
    }
}
