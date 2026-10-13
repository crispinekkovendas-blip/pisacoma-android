package pe.pisacoma.iglesia

import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Escuela Sabática: quarter overview → lesson, day tabs, done marks, last day, Bible sheet. */
@RunWith(AndroidJUnit4::class)
class LessonTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()

    @Before fun clearState() = TestState.clearUserState()

    private fun openLesson1() {
        rule.onNodeWithTag("nav-menu").performClick()
        rule.onNodeWithTag("menu-Esc. Sabática").performClick()
        rule.waitForIdle()
        rule.onNodeWithTag("lesson-list").performScrollToNode(hasTestTag("lesson-1"))
        rule.onNodeWithTag("lesson-1").performClick()
        rule.waitForIdle()
    }

    @Test
    fun overviewListsThirteenLessons() {
        rule.onNodeWithTag("nav-menu").performClick()
        rule.onNodeWithTag("menu-Esc. Sabática").performClick()
        rule.waitForIdle()
        rule.onNodeWithText("Primera y segunda", substring = true).assertIsDisplayed()
        rule.onNodeWithTag("lesson-list").performScrollToNode(hasTestTag("lesson-13"))
        rule.onNodeWithTag("lesson-13").assertIsDisplayed()
        Shots.save(rule, "escuela-overview")
    }

    @Test
    fun dayTabsDoneAndLastDayPersist() {
        openLesson1()
        rule.onNodeWithText("Lección 1: El ministerio de Pablo en Corinto").assertExists()
        rule.onNodeWithTag("day-tab-1").performClick()
        rule.waitForIdle()
        rule.onNodeWithTag("day-done").performClick()
        rule.waitForIdle()
        Shots.save(rule, "escuela-lesson-day2-done")
        // back to the overview: lesson 1 shows one filled dot
        rule.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
        rule.waitForIdle()
        rule.onNodeWithTag("lesson-list").performScrollToNode(hasTestTag("lesson-1"))
        rule.onNodeWithTag("lesson-1-dots-done-1").assertIsDisplayed()
        // reopen: day 2 is still selected (lastDay)
        rule.onNodeWithTag("lesson-1").performClick()
        rule.waitForIdle()
        rule.onNodeWithTag("day-tab-1-selected").assertIsDisplayed()
    }

    @Test
    fun bibleReferenceOpensVerseSheet() {
        openLesson1()
        // the «Lee para el estudio» paragraph starts with "1 Corintios 1: 1": tap its first words
        rule.onNode(hasText("1 Corintios 1: 1", substring = true)).performScrollTo()
            .performTouchInput { click(androidx.compose.ui.geometry.Offset(24f, 20f)) }
        rule.waitForIdle()
        rule.onNodeWithText("Pablo, llamado a ser apóstol", substring = true).assertIsDisplayed()
        Shots.save(rule, "escuela-bible-sheet")
    }
}
