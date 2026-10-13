package pe.pisacoma.iglesia

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import pe.pisacoma.iglesia.data.net.Http

/** Videos with no network: the site's offline copy and a «Reintentar» button, never a spinner forever. */
@RunWith(AndroidJUnit4::class)
class VideosOfflineTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()

    @Before fun goOffline() { TestState.clearUserState(); Http.forceOffline = true }
    @After fun goOnline() { Http.forceOffline = false }

    @Test
    fun offlineStateWithRetry() {
        rule.onNodeWithTag("nav-menu").performClick()
        rule.onNodeWithTag("menu-Videos").performClick()
        rule.waitUntil(10_000) { rule.onAllNodes(androidx.compose.ui.test.hasTestTag("videos-offline")).fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithTag("videos-offline").assertIsDisplayed()
        rule.onNodeWithText("Reintentar").assertIsDisplayed()
        Shots.save(rule, "videos-offline")
    }
}
