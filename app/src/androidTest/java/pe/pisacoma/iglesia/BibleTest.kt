package pe.pisacoma.iglesia

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Biblia: opens on Juan 3 like the site, pages chapters, picks books, bookmarks. */
@RunWith(AndroidJUnit4::class)
class BibleTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()

    @Before fun clearState() = TestState.clearUserState()

    private fun openBible() {
        rule.onNodeWithTag("nav-menu").performClick()
        rule.onNodeWithTag("menu-Biblia").performClick()
        rule.waitForIdle()
    }

    @Test
    fun opensOnJuan3AndPagesChapters() {
        openBible()
        rule.onNodeWithTag("bible-title").assertIsDisplayed()
        rule.onNodeWithText("Juan 3").assertIsDisplayed()
        rule.onNodeWithTag("bible-list").performScrollToNode(hasTestTag("verse-16"))
        rule.onNodeWithText("Porque de tal manera amó Dios al mundo", substring = true).assertIsDisplayed()
        Shots.save(rule, "biblia-juan-3")
        rule.onNodeWithTag("bible-list").performScrollToNode(hasTestTag("bible-title"))
        rule.onNodeWithTag("bible-next").performClick()
        rule.waitForIdle()
        rule.onNodeWithText("Juan 4").assertIsDisplayed()
        rule.onNodeWithTag("bible-prev").performSemanticsAction(SemanticsActions.OnClick)
        rule.waitUntil(5_000) { rule.onAllNodes(hasText("Juan 3")).fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithTag("bible-prev").performSemanticsAction(SemanticsActions.OnClick)
        rule.waitUntil(5_000) { rule.onAllNodes(hasText("Juan 2")).fetchSemanticsNodes().isNotEmpty() }
        Shots.save(rule, "biblia-juan-2")
        rule.onNodeWithText("Juan 2").assertExists()
    }

    @Test
    fun pickerChoosesGenesis1() {
        openBible()
        rule.onNodeWithTag("bible-title").performClick()
        rule.waitForIdle()
        rule.onNodeWithTag("picker-books").performClick()
        rule.onNodeWithTag("picker-tab-AT").performClick()
        rule.onNodeWithTag("picker-book-1").performScrollTo().performClick()
        rule.onNodeWithTag("picker-chapter-1").performClick()
        rule.waitForIdle()
        rule.onNodeWithText("Génesis 1").assertIsDisplayed()
        rule.onNodeWithText("En el principio creó Dios", substring = true).assertIsDisplayed()
    }

    @Test
    fun bookmarkAppearsInThePanel() {
        openBible()
        rule.onNodeWithTag("bible-bookmark").performClick()
        rule.onNodeWithTag("bible-panel").performClick()
        rule.waitForIdle()
        rule.onNodeWithTag("panel-tab-bookmarks").performClick()
        rule.onNode(hasText("Juan 3") and hasAnyAncestor(hasTestTag("bible-drawer"))).assertIsDisplayed()
        Shots.save(rule, "biblia-panel-bookmarks")
    }
}
