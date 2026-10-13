package pe.pisacoma.iglesia

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasAnyChild
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.semantics.SemanticsActions
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
        Shots.save(rule, "himnario-index")
        rule.onNodeWithTag("hymn-list").performScrollToNode(hasTestTag("hymn-count"))
        rule.onNodeWithTag("hymn-count").assertIsDisplayed()
        rule.onNodeWithTag("hymn-count").assert(hasAnyChild(hasText("613")))
        rule.onNodeWithTag("hymn-list").performScrollToNode(hasTestTag("tab-pt"))
        rule.onNodeWithTag("tab-pt").performClick()
        rule.waitForIdle()
        rule.onNodeWithTag("hymn-list").performScrollToNode(hasTestTag("hymn-count"))
        rule.onNodeWithTag("hymn-count").assert(hasAnyChild(hasText("610")))
        rule.onNodeWithTag("hymn-list").performScrollToNode(hasTestTag("tab-pt-novo"))
        rule.onNodeWithTag("tab-pt-novo").performClick()
        rule.waitForIdle()
        rule.onNodeWithTag("hymn-list").performScrollToNode(hasTestTag("hymn-count"))
        rule.onNodeWithTag("hymn-count").assert(hasAnyChild(hasText("600"))) // 601 entries, 587 twice → the index dedupes like the site
        rule.onNodeWithTag("hymn-list").performScrollToNode(hasTestTag("tab-es"))
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
        rule.onNodeWithTag("hymn-number").assertTextEquals("25")
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
        rule.onNodeWithTag("hymn-next").performScrollTo().performClick()
        rule.waitForIdle()
        rule.onNodeWithTag("hymn-number").assertTextEquals("2")
        // the navigation transition keeps both hymn pages briefly: wait for exactly one «Proyectar»
        rule.waitUntil(8_000) { rule.onAllNodes(hasTestTag("hymn-project")).fetchSemanticsNodes().size == 1 }
        rule.onNodeWithTag("hymn-project").performScrollTo().performClick()
        Shots.save(rule, "himno-proyectar-abriendo")
        rule.waitUntil(8_000) { rule.onAllNodes(hasTestTag("projector")).fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithTag("projector-stage").performClick() // cover → first stanza
        rule.waitForIdle()
        rule.onNodeWithText("ESTROFA 1").assertExists()
        Shots.save(rule, "himno-proyectar")
        rule.onNodeWithTag("projector-close").performSemanticsAction(SemanticsActions.OnClick)
        rule.waitUntil(8_000) { rule.onAllNodes(hasTestTag("projector")).fetchSemanticsNodes().isEmpty() }
        Shots.save(rule, "himno-tras-proyectar")
        rule.waitUntil(8_000) { rule.onAllNodes(hasTestTag("hymn-lyrics"), useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty() }
        Shots.save(rule, "himno-tras-proyectar-2")
    }

    @Test
    fun visitedHymnShowsUnderRecientes() {
        openHymnal()
        rule.onNodeWithTag("hymn-list").performScrollToNode(hasTestTag("hymn-row-3"))
        rule.onNodeWithTag("hymn-row-3").performClick()
        rule.waitForIdle()
        rule.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
        rule.waitForIdle()
        rule.onNodeWithTag("hymn-list").performScrollToNode(hasTestTag("recent-3"))
        rule.onNodeWithTag("recent-3").assertIsDisplayed()
    }
}
