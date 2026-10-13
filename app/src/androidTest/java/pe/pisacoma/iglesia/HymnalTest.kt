package pe.pisacoma.iglesia

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Himnario index (three hymnals, search, picks) and the hymn page (lyrics, pager, projector). */
@RunWith(AndroidJUnit4::class)
class HymnalTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()

    @Before fun clearState() = TestState.clearUserState()

    private fun openHymnal() {
        rule.onNodeWithTag("nav-menu").performClick()
        rule.onNodeWithTag("menu-Himnario").performClick()
        rule.waitForIdle()
    }

    @Test
    fun countsPerHymnalAndSearch() {
        openHymnal()
        rule.onNodeWithTag("hymn-count").assertIsDisplayed()
        rule.onNodeWithText("613").assertIsDisplayed()
        Shots.save(rule, "himnario-index")
        rule.onNodeWithTag("tab-pt").performClick()
        rule.waitForIdle()
        rule.onNodeWithText("610").assertIsDisplayed()
        rule.onNodeWithTag("tab-pt-novo").performClick()
        rule.waitForIdle()
        rule.onNodeWithText("600").assertIsDisplayed() // 601 entries, 587 twice → the index dedupes like the site
        rule.onNodeWithTag("tab-es").performClick()
        rule.onNodeWithTag("hymn-search").performTextInput("santo")
        rule.waitForIdle()
        rule.onNodeWithTag("hymn-list").performScrollToNode(hasTestTag("hymn-row-61"))
        rule.onNodeWithTag("hymn-row-61").assertIsDisplayed() // «Santo, Santo, Santo, Dios Omnipotente»
    }

    @Test
    fun numberSearchOpensTheHymn() {
        openHymnal()
        rule.onNodeWithTag("hymn-search").performTextInput("25")
        rule.waitForIdle()
        rule.onNodeWithTag("hymn-list").performScrollToNode(hasTestTag("hymn-row-25"))
        rule.onNodeWithTag("hymn-row-25").performClick()
        rule.waitForIdle()
        rule.onNodeWithTag("hymn-number").assertIsDisplayed()
        rule.onNodeWithText("25").assertIsDisplayed()
    }

    @Test
    fun hymnPageLyricsPagerAndProjector() {
        openHymnal()
        rule.onNodeWithTag("hymn-list").performScrollToNode(hasTestTag("hymn-row-1"))
        rule.onNodeWithTag("hymn-row-1").performClick()
        rule.waitForIdle()
        rule.onNodeWithText("Cantad Alegres al señor").assertIsDisplayed()
        rule.onNodeWithText("Salmos 100:1-5").assertIsDisplayed()
        Shots.save(rule, "himno-es-1")
        rule.onNodeWithTag("hymn-next").performClick()
        rule.waitForIdle()
        rule.onNodeWithTag("hymn-number").assertIsDisplayed()
        rule.onNodeWithText("2").assertIsDisplayed()
        rule.onNodeWithTag("hymn-project").performClick()
        rule.waitForIdle()
        rule.onNodeWithTag("projector").assertIsDisplayed()
        rule.onNodeWithTag("projector-stage").performClick() // cover → first stanza
        rule.waitForIdle()
        rule.onNodeWithText("Estrofa 1").assertIsDisplayed()
        Shots.save(rule, "himno-proyectar")
        rule.onNodeWithTag("projector-close").performClick()
        rule.waitForIdle()
        rule.onNodeWithTag("hymn-number").assertIsDisplayed()
    }

    @Test
    fun visitedHymnShowsUnderRecientes() {
        openHymnal()
        rule.onNodeWithTag("hymn-list").performScrollToNode(hasTestTag("hymn-row-3"))
        rule.onNodeWithTag("hymn-row-3").performClick()
        rule.waitForIdle()
        rule.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
        rule.waitForIdle()
        rule.onNodeWithTag("recent-3").assertIsDisplayed()
    }
}
