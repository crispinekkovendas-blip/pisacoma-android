package pe.pisacoma.iglesia

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** ¿Quién?: the name wheel — add, remove, spin, winner. */
@RunWith(AndroidJUnit4::class)
class WheelTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()

    private fun openWheel() {
        rule.onNodeWithTag("nav-menu").performClick()
        rule.onNodeWithTag("menu-¿Quién?").performClick()
        rule.waitForIdle()
    }

    @Test
    fun addRemoveAndSpin() {
        openWheel()
        rule.onNodeWithTag("wheel-clear").performClick()
        rule.waitForIdle()
        rule.onNodeWithText("Agrega nombres").assertIsDisplayed()
        rule.onNodeWithTag("wheel-input").performTextInput("Ana, Luis")
        rule.onNodeWithTag("wheel-add").performClick()
        rule.waitForIdle()
        rule.onNodeWithTag("wheel-name-Ana").assertIsDisplayed()
        rule.onNodeWithTag("wheel-name-Luis").assertIsDisplayed()
        rule.onNodeWithTag("wheel-segments-2").assertIsDisplayed()
        Shots.save(rule, "quien-ruleta")
        rule.onNodeWithTag("wheel-spin").performClick()
        rule.waitUntil(8_000) { rule.onAllNodesWithTagList("wheel-winner").isNotEmpty() }
        rule.onNodeWithTag("wheel-winner").assertIsDisplayed()
        rule.onNodeWithTag("wheel-remove-Luis").performClick()
        rule.waitForIdle()
        rule.onNodeWithText("Ana").assertIsDisplayed()
        rule.onNodeWithTag("wheel-single").assertIsDisplayed()
    }
}

private fun androidx.compose.ui.test.junit4.AndroidComposeTestRule<*, *>.onAllNodesWithTagList(tag: String) =
    onAllNodes(androidx.compose.ui.test.hasTestTag(tag)).fetchSemanticsNodes()
