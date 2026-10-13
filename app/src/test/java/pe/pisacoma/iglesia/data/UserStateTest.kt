package pe.pisacoma.iglesia.data

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import pe.pisacoma.iglesia.data.prefs.UserState

/** UserState over an in-memory store, keyed exactly like the site's localStorage. */
class UserStateTest {
    private val state = UserState(MemoryKeyValueStore())

    @Test fun stringRoundTrip() {
        assertNull(state.string("biblia:bk1:ch1:notes"))
        state.set("biblia:bk1:ch1:notes", "hola")
        assertEquals("hola", state.string("biblia:bk1:ch1:notes"))
        state.set("biblia:bk1:ch1:notes", null)
        assertNull(state.string("biblia:bk1:ch1:notes"))
    }

    @Test fun boolDefaultsFalse() {
        assertFalse(state.bool("inicio.videos.pausa"))
        state.set("inicio.videos.pausa", true)
        assertTrue(state.bool("inicio.videos.pausa"))
    }

    @Test fun listIsStoredAsJsonArrayLikeTheSite() {
        state.set("biblia:bookmarks", listOf("1:1", "43:3"))
        assertEquals("""["1:1","43:3"]""", state.string("biblia:bookmarks"))
        assertEquals(listOf("1:1", "43:3"), state.stringList("biblia:bookmarks"))
        assertEquals(emptyList<String>(), state.stringList("missing"))
    }

    @Test fun flowEmitsCurrentThenChanges() = runBlocking {
        state.set("egw:cc:last", "3")
        assertEquals("3", state.flow("egw:cc:last").first())
    }

    @Test fun keysMatchTheSite() {
        assertEquals("biblia:bk43:ch3:notes", UserState.Keys.bibleNotes(43, 3))
        assertEquals("egw:cc:ch2:done", UserState.Keys.egwDone("cc", 2))
        assertEquals("egw:cc:ch2:notes", UserState.Keys.egwNotes("cc", 2))
        assertEquals("egw:cc:last", UserState.Keys.egwLast("cc"))
        assertEquals("escuela:2026-03:lecc4:day2:done", UserState.Keys.lessonDone("2026-03", 4, 2))
        assertEquals("escuela:2026-03:lecc4:day2:notes", UserState.Keys.lessonNotes("2026-03", 4, 2))
        assertEquals("escuela:2026-03:lecc4:lastDay", UserState.Keys.lessonLastDay("2026-03", 4))
        assertEquals("escuela:2026-03:bookmarks", UserState.Keys.lessonBookmarks("2026-03"))
        assertEquals("quiz.leccion-13-gracia-amor-comunion.v1", UserState.Keys.quiz("leccion-13-gracia-amor-comunion"))
        assertEquals("himnario.recent", UserState.Keys.hymnRecent)
        assertEquals("inicio.videos.pausa", UserState.Keys.homePaused)
        assertEquals("biblia:bookmarks", UserState.Keys.bibleBookmarks)
        assertEquals("egw:bookmarks", UserState.Keys.egwBookmarks)
    }
}
