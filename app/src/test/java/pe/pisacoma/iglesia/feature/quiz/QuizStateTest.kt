package pe.pisacoma.iglesia.feature.quiz

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import pe.pisacoma.iglesia.data.model.QuizLevel
import pe.pisacoma.iglesia.data.model.QuizOption
import pe.pisacoma.iglesia.data.model.QuizPack
import pe.pisacoma.iglesia.data.model.QuizQuestion
import pe.pisacoma.iglesia.data.model.Memorize

/**
 * The site's quiz-view.tsx state machine: pick → pending (modal) → confirm locks the answer;
 * nothing can be changed afterwards; «Ver resultados» submits; persisted as
 * `{"committed":{"1":"B"},"submitted":false}` under `quiz.<slug>.v1`.
 */
class QuizStateTest {
    private val pack = QuizPack(
        slug = "t", lessonNumber = 1, title = "T", badge = "B", subtitle = "S",
        memorize = Memorize("x", "Jn 1:1"), metaTitle = "", metaDescription = "",
        questions = (1..3).map { n ->
            QuizQuestion(n = n, text = "Q$n", ref = null, options = listOf(QuizOption("A", "a"), QuizOption("B", "b"), QuizOption("C", "c"), QuizOption("D", "d")), correct = "B", explain = "porque sí")
        },
        levels = null,
    )

    @Test fun pickOpensTheConfirmModal() {
        val s = QuizState().requestCommit(1, "B")
        assertEquals(QuizState.Pending(1, "B"), s.pending)
        assertTrue(s.committed.isEmpty())
    }

    @Test fun cancelClosesWithoutCommitting() {
        val s = QuizState().requestCommit(1, "B").cancel()
        assertNull(s.pending)
        assertTrue(s.committed.isEmpty())
    }

    @Test fun confirmLocksTheAnswer() {
        val s = QuizState().requestCommit(1, "B").confirm()
        assertNull(s.pending)
        assertEquals("B", s.committed[1])
        // locked: a new pick on the same question is ignored
        assertNull(s.requestCommit(1, "C").pending)
        assertEquals("B", s.requestCommit(1, "C").committed[1])
    }

    @Test fun scoreCountsCorrectCommittedAnswers() {
        val s = QuizState().requestCommit(1, "B").confirm().requestCommit(2, "A").confirm()
        assertEquals(1, s.score(pack))
        assertEquals(2, s.committedCount)
        assertFalse(s.allCommitted(pack))
        assertEquals(67, s.pct(pack))
    }

    @Test fun submitRevealsResultsAndBlocksPicks() {
        val s = QuizState().requestCommit(1, "B").confirm().submit()
        assertTrue(s.submitted)
        assertNull(s.requestCommit(2, "A").pending)
    }

    @Test fun resetClearsEverything() {
        val s = QuizState().requestCommit(1, "B").confirm().submit().reset()
        assertTrue(s.committed.isEmpty())
        assertFalse(s.submitted)
        assertNull(s.pending)
    }

    @Test fun levelIsTheHighestMinScoreReached() {
        val levels = QuizState.DEFAULT_LEVELS
        assertEquals("Caleb", QuizState.tier(15, levels).name)
        assertEquals("Caleb", QuizState.tier(14, levels).name)
        assertEquals("Daniel", QuizState.tier(11, levels).name)
        assertEquals("Débora", QuizState.tier(8, levels).name)
        assertEquals("Josías", QuizState.tier(0, levels).name)
        val custom = listOf(QuizLevel(2, "Alto", "a"), QuizLevel(0, "Bajo", "b"))
        assertEquals("Alto", QuizState.tier(3, custom).name)
    }

    @Test fun defaultLevelsMatchTheSite() {
        assertEquals(listOf(14, 11, 8, 0), QuizState.DEFAULT_LEVELS.map { it.minScore })
        assertEquals("¡Excelente! Conoces bien la lección de esta semana.", QuizState.DEFAULT_LEVELS[0].desc)
    }

    @Test fun persistedJsonMatchesTheSite() {
        val s = QuizState().requestCommit(1, "B").confirm().requestCommit(3, "D").confirm()
        assertEquals("""{"committed":{"1":"B","3":"D"},"submitted":false}""", s.toJson())
        val back = QuizState.fromJson("""{"committed":{"1":"B","3":"D"},"submitted":true}""")
        assertEquals(mapOf(1 to "B", 3 to "D"), back.committed)
        assertTrue(back.submitted)
        assertNull(back.pending)
        assertEquals(QuizState(), QuizState.fromJson("garbage"))
        assertEquals(QuizState(), QuizState.fromJson(null))
    }
}
