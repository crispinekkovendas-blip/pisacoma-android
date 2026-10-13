package pe.pisacoma.iglesia.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

/**
 * Reference values computed with the site's own src/utils/zmanim.ts (tsx, 2026-10-13):
 *   now 2026-10-13T15:00Z → friday 2026-10-16, start 2026-10-16T22:37:26Z, end 2026-10-17T22:37:40Z
 *   now 2026-06-20T12:00Z → friday 2026-06-19, start 2026-06-19T22:12:18Z, end 2026-06-20T22:12:30Z, isSabbath
 *   now 2026-12-20T12:00Z → friday 2026-12-25, start 2026-12-25T23:11:20Z, end 2026-12-26T23:11:47Z
 *   now 2026-10-17T23:30Z (Saturday after sunset) → rolls to friday 2026-10-23, start 2026-10-23T22:39:20Z
 *   now 2026-10-17T22:00Z (Saturday before sunset) → friday 2026-10-16, isSabbath
 *   sunset 2026-10-16 = 22:37:26Z, sunrise = 10:08:46Z; localCivilDate(2026-10-14T03:00Z) = 2026-10-13
 */
class ZmanimTest {
    private fun ms(iso: String) = Instant.parse(iso).toEpochMilli()
    private fun assertMinute(expectedIso: String, actual: Long?) {
        assertTrue("null instant", actual != null)
        assertEquals(ms(expectedIso) / 60_000, actual!! / 60_000)
    }

    @Test fun sunsetAndSunriseForOneDay() {
        val day = ms("2026-10-16T00:00:00Z")
        assertMinute("2026-10-16T22:37:26Z", Zmanim.sunsetUtc(day))
        assertMinute("2026-10-16T10:08:46Z", Zmanim.sunriseUtc(day))
    }

    @Test fun midweekPointsToTheComingSabbath() {
        val t = Zmanim.sabbathTimes(ms("2026-10-13T15:00:00Z"))
        assertEquals(ms("2026-10-16T00:00:00Z"), t.friday)
        assertEquals(ms("2026-10-17T00:00:00Z"), t.saturday)
        assertMinute("2026-10-16T22:37:26Z", t.start)
        assertMinute("2026-10-17T22:37:40Z", t.end)
        assertFalse(t.isSabbath)
    }

    @Test fun saturdayMorningIsSabbath() {
        val t = Zmanim.sabbathTimes(ms("2026-06-20T12:00:00Z"))
        assertEquals(ms("2026-06-19T00:00:00Z"), t.friday)
        assertMinute("2026-06-19T22:12:18Z", t.start)
        assertMinute("2026-06-20T22:12:30Z", t.end)
        assertTrue(t.isSabbath)
    }

    @Test fun decemberSunsetIsLater() {
        val t = Zmanim.sabbathTimes(ms("2026-12-20T12:00:00Z"))
        assertEquals(ms("2026-12-25T00:00:00Z"), t.friday)
        assertMinute("2026-12-25T23:11:20Z", t.start)
        assertMinute("2026-12-26T23:11:47Z", t.end)
    }

    @Test fun saturdayAfterSunsetRollsToNextWeek() {
        val t = Zmanim.sabbathTimes(ms("2026-10-17T23:30:00Z"))
        assertEquals(ms("2026-10-23T00:00:00Z"), t.friday)
        assertMinute("2026-10-23T22:39:20Z", t.start)
        assertFalse(t.isSabbath)
    }

    @Test fun saturdayBeforeSunsetIsStillThisSabbath() {
        val t = Zmanim.sabbathTimes(ms("2026-10-17T22:00:00Z"))
        assertEquals(ms("2026-10-16T00:00:00Z"), t.friday)
        assertTrue(t.isSabbath)
    }

    @Test fun localCivilDateUsesPeruTime() {
        assertEquals(ms("2026-10-13T00:00:00Z"), Zmanim.localCivilDate(ms("2026-10-14T03:00:00Z")))
    }

    // site clock12 → { time: "5:37", suffix: "PM" } in Peru time; dayLabel → "Viernes 18 set" (es-PE)
    @Test fun clockAndDayLabelsInSpanish() {
        assertEquals(Zmanim.Clock12("5:37", "PM"), Zmanim.clock12(ms("2026-10-16T22:37:26Z")))
        assertEquals(Zmanim.Clock12("12:05", "AM"), Zmanim.clock12(ms("2026-10-17T05:05:00Z")))
        assertEquals(Zmanim.Clock12("--:--", ""), Zmanim.clock12(null))
        assertEquals("Viernes 16 oct", Zmanim.dayLabel(ms("2026-10-16T00:00:00Z")))
        assertEquals("Sábado 17 oct", Zmanim.dayLabel(ms("2026-10-17T00:00:00Z")))
        assertEquals("Viernes 18 set", Zmanim.dayLabel(ms("2026-09-18T00:00:00Z")))
    }

    @Test fun parseDmy() {
        assertEquals(ms("2026-06-27T00:00:00Z"), Zmanim.parseDMY("27/06/2026"))
    }

    // site quarterStatus / currentLessonIndex / longDayLabel (escuela-sabatica overview)
    @Test fun quarterStatusAgainstPeruDate() {
        val q = Zmanim.QuarterDates("27/06/2026", "25/09/2026")
        assertEquals(Zmanim.QuarterState.ACTIVE, Zmanim.quarterStatus(q, ms("2026-08-01T12:00:00Z")).state)
        assertEquals(Zmanim.QuarterState.UPCOMING, Zmanim.quarterStatus(q, ms("2026-06-20T12:00:00Z")).state)
        val ended = Zmanim.quarterStatus(q, ms("2026-10-13T15:00:00Z"))
        assertEquals(Zmanim.QuarterState.ENDED, ended.state)
        assertEquals(ms("2026-09-26T00:00:00Z"), ended.nextStart)
        // 25/09 23:30 Lima = 26/09 04:30 UTC → still 25/09 in Pisacoma → active
        assertEquals(Zmanim.QuarterState.ACTIVE, Zmanim.quarterStatus(q, ms("2026-09-26T04:30:00Z")).state)
    }

    @Test fun currentLessonIndexIsLatestStartedLesson() {
        val starts = listOf("27/06/2026", "04/07/2026", "11/07/2026")
        assertEquals(0, Zmanim.currentLessonIndex(starts, ms("2026-06-20T12:00:00Z")))
        assertEquals(1, Zmanim.currentLessonIndex(starts, ms("2026-07-05T12:00:00Z")))
        assertEquals(2, Zmanim.currentLessonIndex(starts, ms("2026-12-01T12:00:00Z")))
        // Friday 10/07 19:30 Lima = 11/07 00:30 UTC: still lesson 2 in Pisacoma
        assertEquals(1, Zmanim.currentLessonIndex(starts, ms("2026-07-11T00:30:00Z")))
    }

    @Test fun longDayLabel() {
        assertEquals("sábado 26 de septiembre", Zmanim.longDayLabel(ms("2026-09-26T00:00:00Z")))
    }
}
