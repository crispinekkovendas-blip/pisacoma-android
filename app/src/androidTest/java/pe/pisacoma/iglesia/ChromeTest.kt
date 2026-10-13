package pe.pisacoma.iglesia

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import pe.pisacoma.iglesia.nav.NAV_ITEMS

/** The site's chrome: hamburger opens the menu with the eleven destinations; a row navigates. */
@RunWith(AndroidJUnit4::class)
class ChromeTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()

    @Test
    fun menuListsTheElevenDestinations() {
        rule.onNodeWithTag("nav-menu").performClick()
        rule.waitForIdle()
        for (item in NAV_ITEMS) rule.onNodeWithTag("menu-${item.label}").assertIsDisplayed()
        rule.onNodeWithText("Pisacoma · 3.940 m").assertIsDisplayed()
    }

    @Test
    fun menuRowNavigatesToHimnario() {
        rule.onNodeWithTag("nav-menu").performClick()
        rule.waitForIdle()
        rule.onNodeWithTag("menu-Himnario").performClick()
        rule.waitForIdle()
        rule.onNodeWithTag("hymn-list").assertIsDisplayed()
    }

    @Test
    fun searchButtonOpensBuscar() {
        rule.onNodeWithTag("nav-search").performClick()
        rule.waitForIdle()
        rule.onNodeWithTag("search-page").assertIsDisplayed()
    }

    @Test
    fun screenshotHelperSavesToExternalFiles() {
        Shots.save(rule, "chrome-home")
    }
}
