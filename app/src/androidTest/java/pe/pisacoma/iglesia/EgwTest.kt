package pe.pisacoma.iglesia

import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasTestTag
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

/** EGW: four rails of covers, a book's chapters and reader, done marks and «Continuar». */
@RunWith(AndroidJUnit4::class)
class EgwTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()

    @Before fun clearState() = TestState.clearUserState()

    private fun openEgw() {
        rule.onNodeWithTag("nav-menu").performClick()
        rule.onNodeWithTag("menu-EGW").performClick()
        rule.waitForIdle()
    }

    @Test
    fun catalogueHasFourRailsAndTwentyFiveCovers() {
        openEgw()
        rule.onNodeWithText("Veinticinco libros", substring = true).assertIsDisplayed()
        for (rail in listOf("I", "II", "III", "IV")) {
            rule.onNodeWithTag("egw-page").performScrollToNode(hasTestTag("rail-$rail"))
            rule.onNodeWithTag("rail-$rail").assertIsDisplayed()
        }
        rule.onNodeWithTag("rail-row-IV").performScrollToNode(hasTestTag("cover-afc"))
        rule.onNodeWithTag("cover-afc").assertIsDisplayed()
        Shots.save(rule, "egw-catalogo")
    }

    @Test
    fun bookReaderChaptersDoneAndContinue() {
        openEgw()
        rule.onNodeWithTag("egw-page").performScrollToNode(hasTestTag("rail-I"))
        rule.onNodeWithTag("rail-row-I").performScrollToNode(hasTestTag("cover-cc"))
        rule.onNodeWithTag("cover-cc").performClick()
        rule.waitForIdle()
        rule.onNodeWithTag("egw-chapter-title").assertTextEquals("El camino a Cristo") // chapter 1 title
        Shots.save(rule, "egw-libro-cc")
        rule.onNodeWithTag("egw-chapters").performClick()
        rule.waitForIdle()
        rule.onNodeWithTag("chapter-2").performClick()
        rule.waitForIdle()
        rule.onNodeWithTag("egw-chapter-title").assertTextEquals("Capítulo 1— Amor supremo")
        rule.onNodeWithTag("chapter-done").performScrollTo().performClick()
        rule.waitForIdle()
        rule.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
        rule.waitForIdle()
        rule.onNodeWithTag("egw-page").performScrollToNode(hasTestTag("rail-I"))
        rule.onNodeWithTag("rail-row-I").performScrollToNode(hasTestTag("cover-cc"))
        rule.onNodeWithTag("continue-cc").assertExists()
    }
}
