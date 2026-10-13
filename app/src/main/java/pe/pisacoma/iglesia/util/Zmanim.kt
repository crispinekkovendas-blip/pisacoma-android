package pe.pisacoma.iglesia.util

import java.util.Calendar
import java.util.TimeZone
import kotlin.math.PI
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan

/**
 * Port of the site's src/utils/zmanim.ts: NOAA sunset/sunrise for Pisacoma, the week's Sabbath
 * (Friday sunset → Saturday sunset), Peru civil dates and the labels the home shows.
 * Instants are epoch milliseconds (UTC); civil days are 00:00 UTC of the local date.
 */
object Zmanim {
    data class Place(val lat: Double, val lng: Double, val tzOffsetHours: Int)
    val PISACOMA = Place(-16.908, -69.365, -5)

    private const val DEG2RAD = PI / 180
    private const val RAD2DEG = 180 / PI
    private const val DAY = 86_400_000L

    private fun julianCenturies(ms: Long): Double = (ms / 86400000.0 + 2440587.5 - 2451545.0) / 36525.0
    private fun meanLongitudeSun(t: Double) = (280.46646 + t * (36000.76983 + t * 0.0003032)) % 360
    private fun meanAnomalySun(t: Double) = 357.52911 + t * (35999.05029 - 0.0001537 * t)
    private fun equationOfCenter(t: Double): Double {
        val m = meanAnomalySun(t) * DEG2RAD
        return sin(m) * (1.914602 - t * (0.004817 + 0.000014 * t)) + sin(2 * m) * (0.019993 - 0.000101 * t) + sin(3 * m) * 0.000289
    }
    private fun sunApparentLongitude(t: Double): Double {
        val omega = (125.04 - 1934.136 * t) * DEG2RAD
        return meanLongitudeSun(t) + equationOfCenter(t) - 0.00569 - 0.00478 * sin(omega)
    }
    private fun obliquityCorrection(t: Double): Double {
        val e0 = 23 + (26 + (21.448 - t * (46.815 + t * (0.00059 - t * 0.001813))) / 60) / 60
        val omega = (125.04 - 1934.136 * t) * DEG2RAD
        return e0 + 0.00256 * cos(omega)
    }
    private fun sunDeclination(t: Double): Double {
        val e = obliquityCorrection(t) * DEG2RAD
        val lambda = sunApparentLongitude(t) * DEG2RAD
        return asin(sin(e) * sin(lambda)) * RAD2DEG
    }
    private fun equationOfTime(t: Double): Double {
        val e = obliquityCorrection(t) * DEG2RAD
        val l0 = meanLongitudeSun(t) * DEG2RAD
        val m = meanAnomalySun(t) * DEG2RAD
        val ec = 0.016708634 - t * (0.000042037 + 0.0000001267 * t)
        val y = tan(e / 2) * tan(e / 2)
        val eot = y * sin(2 * l0) - 2 * ec * sin(m) + 4 * ec * y * sin(m) * cos(2 * l0) - 0.5 * y * y * sin(4 * l0) - 1.25 * ec * ec * sin(2 * m)
        return eot * 4 * RAD2DEG
    }

    /** Sunset (or sunrise) as a UTC instant for the civil date `dayUtcMidnight`, or null at the poles. */
    private fun sunEvent(dayUtcMidnight: Long, lat: Double, lng: Double, rising: Boolean): Long? {
        val t = julianCenturies(dayUtcMidnight)
        val eqTime = equationOfTime(t)
        val decl = sunDeclination(t) * DEG2RAD
        val latR = lat * DEG2RAD
        val zenR = 90.833 * DEG2RAD
        val cosHA = (cos(zenR) - sin(latR) * sin(decl)) / (cos(latR) * cos(decl))
        if (cosHA < -1 || cosHA > 1) return null
        val haDeg = acos(cosHA) * RAD2DEG
        val solarNoon = 720 - 4 * lng - eqTime
        val minutesUtc = solarNoon + 4 * (if (rising) -haDeg else haDeg)
        return dayUtcMidnight + (minutesUtc * 60000).toLong()
    }

    fun sunsetUtc(dayUtcMidnight: Long, lat: Double = PISACOMA.lat, lng: Double = PISACOMA.lng): Long? = sunEvent(dayUtcMidnight, lat, lng, false)
    fun sunriseUtc(dayUtcMidnight: Long, lat: Double = PISACOMA.lat, lng: Double = PISACOMA.lng): Long? = sunEvent(dayUtcMidnight, lat, lng, true)

    data class SabbathTimes(val start: Long?, val end: Long?, val friday: Long, val saturday: Long, val isSabbath: Boolean)

    /** The Sabbath of the current week in Pisacoma local time; after Saturday's sunset, the next one. */
    fun sabbathTimes(nowUtc: Long, tzOffset: Int = PISACOMA.tzOffsetHours): SabbathTimes {
        val local = utcCalendar(nowUtc + tzOffset * 3_600_000L)
        val y = local.get(Calendar.YEAR); val m = local.get(Calendar.MONTH); val d = local.get(Calendar.DAY_OF_MONTH)
        val wd = local.get(Calendar.DAY_OF_WEEK) - 1 // 0 Sun … 6 Sat
        var toFriday = if (wd == 6) -1 else (5 - wd + 7) % 7
        var end = sunsetUtc(utcMidnight(y, m, d + toFriday + 1))
        if (end != null && nowUtc >= end) {
            toFriday += 7
            end = sunsetUtc(utcMidnight(y, m, d + toFriday + 1))
        }
        val friday = utcMidnight(y, m, d + toFriday)
        val saturday = utcMidnight(y, m, d + toFriday + 1)
        val start = sunsetUtc(friday)
        val isSabbath = start != null && end != null && nowUtc >= start && nowUtc < end
        return SabbathTimes(start, end, friday, saturday, isSabbath)
    }

    /** 00:00 UTC of the civil date in Pisacoma. */
    fun localCivilDate(nowUtc: Long, tzOffset: Int = PISACOMA.tzOffsetHours): Long {
        val local = utcCalendar(nowUtc + tzOffset * 3_600_000L)
        return utcMidnight(local.get(Calendar.YEAR), local.get(Calendar.MONTH), local.get(Calendar.DAY_OF_MONTH))
    }

    /** "27/06/2026" → 00:00 UTC of that date. */
    fun parseDMY(s: String): Long {
        val (d, mo, y) = s.trim().split("/").map { it.toInt() }
        return utcMidnight(y, mo - 1, d)
    }

    enum class QuarterState { UPCOMING, ACTIVE, ENDED }
    data class QuarterDates(val startDate: String, val endDate: String)
    data class QuarterStatus(val state: QuarterState, val nextStart: Long)

    /** Where today (Pisacoma) falls relative to a quarterly's "dd/mm/yyyy" dates. */
    fun quarterStatus(q: QuarterDates, nowUtc: Long): QuarterStatus {
        val today = localCivilDate(nowUtc)
        val start = parseDMY(q.startDate)
        val end = parseDMY(q.endDate)
        val state = when {
            today < start -> QuarterState.UPCOMING
            today > end -> QuarterState.ENDED
            else -> QuarterState.ACTIVE
        }
        return QuarterStatus(state, end + DAY)
    }

    /** Index of the lesson whose start_date is the latest one on or before today (Pisacoma). */
    fun currentLessonIndex(startDates: List<String>, nowUtc: Long): Int {
        val today = localCivilDate(nowUtc)
        for (i in startDates.indices.reversed()) if (today >= parseDMY(startDates[i])) return i
        return 0
    }

    data class Clock12(val time: String, val suffix: String)

    /** "5:37" / "PM" in Peru time, or "--:--" for a null instant (site clock12). */
    fun clock12(utc: Long?, tzOffset: Int = PISACOMA.tzOffsetHours): Clock12 {
        if (utc == null) return Clock12("--:--", "")
        val c = utcCalendar(utc + tzOffset * 3_600_000L)
        val h24 = c.get(Calendar.HOUR_OF_DAY)
        val h12 = if (h24 % 12 == 0) 12 else h24 % 12
        return Clock12("$h12:${c.get(Calendar.MINUTE).toString().padStart(2, '0')}", if (h24 < 12) "AM" else "PM")
    }

    private val WEEKDAYS = listOf("domingo", "lunes", "martes", "miércoles", "jueves", "viernes", "sábado")
    // es-PE short months (CLDR): note "set" for septiembre
    private val MONTHS_SHORT = listOf("ene", "feb", "mar", "abr", "may", "jun", "jul", "ago", "set", "oct", "nov", "dic")
    private val MONTHS_LONG = listOf("enero", "febrero", "marzo", "abril", "mayo", "junio", "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre")

    /** "Viernes 18 set" for a 00:00 UTC civil date (site dayLabel, es-PE). */
    fun dayLabel(dayUtcMidnight: Long): String {
        val c = utcCalendar(dayUtcMidnight)
        val wd = WEEKDAYS[c.get(Calendar.DAY_OF_WEEK) - 1]
        return "${wd.replaceFirstChar { it.uppercase() }} ${c.get(Calendar.DAY_OF_MONTH)} ${MONTHS_SHORT[c.get(Calendar.MONTH)]}"
    }

    /** "sábado 26 de septiembre" for a 00:00 UTC civil date (site longDayLabel). */
    fun longDayLabel(dayUtcMidnight: Long): String {
        val c = utcCalendar(dayUtcMidnight)
        return "${WEEKDAYS[c.get(Calendar.DAY_OF_WEEK) - 1]} ${c.get(Calendar.DAY_OF_MONTH)} de ${MONTHS_LONG[c.get(Calendar.MONTH)]}"
    }

    private fun utcCalendar(ms: Long): Calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = ms }

    private fun utcMidnight(y: Int, m0: Int, d: Int): Long = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        clear(); set(y, m0, 1, 0, 0, 0); add(Calendar.DAY_OF_MONTH, d - 1)
    }.timeInMillis
}
