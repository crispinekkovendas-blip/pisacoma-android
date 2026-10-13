package pe.pisacoma.iglesia

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Quiz: packs index, commit-answer flow with the confirm modal, persistence across relaunch. */
@RunWith(AndroidJUnit4::class)
class QuizTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()

    @Before fun clearState() = TestState.clearUserState()

    private fun openFirstQuiz() {
        rule.onNodeWithTag("nav-menu").performClick()
        rule.onNodeWithTag("menu-Quiz").performClick()
        rule.waitForIdle()
        rule.onNodeWithText("Quizzes de la Escuela Sabática").assertIsDisplayed()
        rule.onNodeWithTag("quiz-card-leccion-13-gracia-amor-comunion").performClick()
        rule.waitForIdle()
    }

    @Test
    fun confirmLocksAnAnswerAndSurvivesRelaunch() {
        openFirstQuiz()
        Shots.save(rule, "quiz-l13")
        rule.onNodeWithTag("quiz-page").performScrollToNode(hasTestTag("q1-B"))
        rule.onNodeWithTag("q1-B").performClick()
        rule.waitForIdle()
        rule.onNodeWithText("¿Confirmar esta respuesta?").assertIsDisplayed()
        Shots.save(rule, "quiz-confirm")
        rule.onNodeWithTag("confirm-yes").performClick()
        rule.waitForIdle()
        rule.onNodeWithTag("q1-locked").assertIsDisplayed()
        rule.onNodeWithText("1 / 15 confirmadas", substring = true).assertIsDisplayed()
        // relaunch the screen: still locked
        rule.activityRule.scenario.recreate()
        rule.waitForIdle()
        openFirstQuiz()
        rule.onNodeWithTag("quiz-page").performScrollToNode(hasTestTag("q1-locked"))
        rule.onNodeWithTag("q1-locked").assertIsDisplayed()
    }

    @Test
    fun cancelKeepsTheQuestionOpen() {
        openFirstQuiz()
        rule.onNodeWithTag("quiz-page").performScrollToNode(hasTestTag("q1-A"))
        rule.onNodeWithTag("q1-A").performClick()
        rule.onNodeWithTag("confirm-no").performClick()
        rule.waitForIdle()
        rule.onNodeWithTag("q1-A").assertIsDisplayed()
    }
}
