package pe.pisacoma.iglesia.feature.videos

import android.icu.text.RelativeDateTimeFormatter
import android.icu.util.ULocale
import android.os.Build
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import java.time.Instant

/** One video of the catalogue (site videos/data.ts VideoEntry). */
@Serializable
data class VideoEntry(
    val videoId: String,
    val title: String,
    val published: String? = null,
    val tags: List<String>? = null,
    val hero: Boolean? = null,
    val channel: String? = null,
)

data class TaggedVideo(val entry: VideoEntry, val tags: List<String>) {
    val videoId get() = entry.videoId
    val title get() = entry.title
    val published get() = entry.published
    val hero get() = entry.hero == true
}

data class Channel(val key: String, val id: String, val label: String, val shortLabel: String)
data class Category(val key: String, val label: String, val regex: Regex)

/** Port of the site's videos/data.ts: channels, categories, tagging, times, thumbnails, feed parsing. */
object VideoCatalog {
    const val CHANNEL_NAME = "Iglesia Adventista del Séptimo Día — Pisacoma"
    const val FEED_URL = "https://quiz-qwik.vercel.app/videos/api/youtube"

    val CHANNELS = listOf(
        Channel("bullon", "UCzY9YOmZquSEERTbl_u-MSQ", "Pr. Alejandro Bullón", "Bullón"),
        Channel("flores", "UCwFBSOeEwzF5hloXaUsvWyw", "Pr. Joel Flores", "Flores"),
        Channel("sumtv", "UCFqRy7pWgY3Yii43GyjtAuA", "SUMtv Latino · Pr. Bohr", "SUMtv"),
        Channel("lecciones", "UChboqca1Z0bIcoHQk5yGqnQ", "Lecciones Escuela Sabática", "Lecciones"),
    )

    private val I = RegexOption.IGNORE_CASE
    val CATS = listOf(
        Category("sermones", "Sermones · Predicación", Regex("serm[oó]n|predicac", I)),
        Category("escuela_sabatica", "Escuela Sabática", Regex("escuela sab[aá]tica|lecci[oó]n", I)),
        Category("musica", "Música & Himnos", Regex("m[uú]sica|himno|canto|coro|alabanza", I)),
        Category("profecia", "Profecía", Regex("profec[ií]a|apocalipsis|daniel", I)),
        Category("salud_familia", "Salud & Familia", Regex("salud|familia|hogar|matrimonio", I)),
        Category("en_vivo", "En Vivo", Regex("en vivo|directo|\\blive\\b", I)),
        Category("juveniles", "Juveniles · JA", Regex("j[oó]ven|juvenil|\\bja\\b", I)),
        Category("ninos", "Niños", Regex("ni[ñn]o|infantil", I)),
        Category("egw", "Espíritu de Profecía", Regex("elena|white|egw|esp[ií]ritu de profec", I)),
        Category("pisacoma", "Pisacoma · Local", Regex("pisacoma|chucuito|aimara|altiplano", I)),
    )

    fun tagVideo(v: VideoEntry): TaggedVideo {
        val auto = CATS.filter { it.regex.containsMatchIn(v.title) }.map { it.key }
        val manual = v.tags ?: emptyList()
        val channel = v.channel?.let { listOf("channel:$it") } ?: emptyList()
        return TaggedVideo(v, (auto + manual + channel).distinct())
    }

    /** "1:15" for seconds (site fmtTime). */
    fun fmtTime(s: Double): String {
        if (s.isNaN() || s <= 0) return "0:00"
        val m = (s / 60).toInt()
        val ss = (s % 60).toInt().toString().padStart(2, '0')
        return "$m:$ss"
    }

    val THUMB_SIZES = listOf("maxresdefault", "hqdefault", "sddefault", "mqdefault", "default")
    fun thumb(videoId: String, size: String = "hqdefault") = "https://i.ytimg.com/vi/$videoId/$size.jpg"

    /** Intl.RelativeTimeFormat("es", { numeric: "auto" }) over the site's thresholds. */
    fun relTimeEs(published: Long?, now: Long = System.currentTimeMillis()): String {
        if (published == null) return ""
        val diff = maxOf(0L, Math.round((now - published) / 1000.0))
        val (value, unit) = when {
            diff < 60 -> diff to Unit.SECOND
            diff < 3600 -> Math.round(diff / 60.0) to Unit.MINUTE
            diff < 86400 -> Math.round(diff / 3600.0) to Unit.HOUR
            diff < 86400 * 30 -> Math.round(diff / 86400.0) to Unit.DAY
            diff < 86400 * 365 -> Math.round(diff / (86400.0 * 30)) to Unit.MONTH
            else -> Math.round(diff / (86400.0 * 365)) to Unit.YEAR
        }
        return format(value, unit)
    }

    fun relTimeEsIso(iso: String?, now: Long = System.currentTimeMillis()): String =
        relTimeEs(iso?.let { runCatching { Instant.parse(it).toEpochMilli() }.getOrNull() }, now)

    private enum class Unit { SECOND, MINUTE, HOUR, DAY, MONTH, YEAR }

    private fun format(value: Long, unit: Unit): String {
        // numeric: "auto" — ayer / anteayer / el mes pasado / el año pasado, like the browser
        when (unit) {
            Unit.DAY -> if (value == 1L) return "ayer" else if (value == 2L) return "anteayer"
            Unit.MONTH -> if (value == 1L) return "el mes pasado"
            Unit.YEAR -> if (value == 1L) return "el año pasado"
            Unit.SECOND -> if (value == 0L) return "ahora"
            else -> {}
        }
        if (Build.VERSION.SDK_INT >= 24) {
            val f = RelativeDateTimeFormatter.getInstance(ULocale("es"))
            val u = when (unit) {
                Unit.SECOND -> RelativeDateTimeFormatter.RelativeUnit.SECONDS
                Unit.MINUTE -> RelativeDateTimeFormatter.RelativeUnit.MINUTES
                Unit.HOUR -> RelativeDateTimeFormatter.RelativeUnit.HOURS
                Unit.DAY -> RelativeDateTimeFormatter.RelativeUnit.DAYS
                Unit.MONTH -> RelativeDateTimeFormatter.RelativeUnit.MONTHS
                Unit.YEAR -> RelativeDateTimeFormatter.RelativeUnit.YEARS
            }
            return f.format(value.toDouble(), RelativeDateTimeFormatter.Direction.LAST, u)
        }
        val word = when (unit) {
            Unit.SECOND -> if (value == 1L) "segundo" else "segundos"
            Unit.MINUTE -> if (value == 1L) "minuto" else "minutos"
            Unit.HOUR -> if (value == 1L) "hora" else "horas"
            Unit.DAY -> "días"
            Unit.MONTH -> "meses"
            Unit.YEAR -> "años"
        }
        return "hace $value $word"
    }

    /** `{"videos":[…]}` from the site's /videos/api/youtube; empty on anything odd. */
    fun parseFeed(body: String): List<VideoEntry> = runCatching {
        val arr = Json.parseToJsonElement(body).jsonObject["videos"]?.jsonArray ?: return emptyList()
        val json = Json { ignoreUnknownKeys = true; isLenient = true }
        arr.mapNotNull { runCatching { json.decodeFromJsonElement(VideoEntry.serializer(), it) }.getOrNull() }
    }.getOrDefault(emptyList())
}
