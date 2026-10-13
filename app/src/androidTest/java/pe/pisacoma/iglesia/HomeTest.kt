package pe.pisacoma.iglesia

import android.content.pm.ActivityInfo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import pe.pisacoma.iglesia.data.prefs.UserState

/** Inicio «Un día en el Altiplano»: eleven scenes in order, actions navigate, pause persists, rotation survives. */
@RunWith(AndroidJUnit4::class)
class HomeTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()

    @Before fun clearState() = TestState.clearUserState()

    private val scenes = listOf("inicio", "himnario", "videos", "escuela", "biblia", "diccionario", "egw", "quiz", "quien", "sabado", "noche")

    @Test
    fun elevenScenesInOrder() {
        for ((i, id) in scenes.withIndex()) {
            rule.onNodeWithTag("home").performScrollToNode(hasTestTag("scene-$id"))
            rule.onNodeWithTag("scene-$id").assertIsDisplayed()
            if (i == 0 || id == "sabado" || id == "noche") Shots.save(rule, "home-$id")
        }
    }

    @Test
    fun sceneActionNavigates() {
        rule.onNodeWithTag("home").performScrollToNode(hasTestTag("scene-himnario-action"))
        rule.onNodeWithTag("scene-himnario-action").performClick()
        rule.waitForIdle()
        rule.onNodeWithTag("hymn-count").assertIsDisplayed()
    }

    @Test
    fun pausePersistsAndRotationKeepsIt() {
        rule.onNodeWithTag("home-pause").performClick()
        rule.waitForIdle()
        val ctx = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("1", ctx.getSharedPreferences(UserState.PREFS_NAME, 0).getString(UserState.Keys.homePaused, null))
        rule.activityRule.scenario.onActivity { it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE }
        rule.waitForIdle()
        rule.onNodeWithTag("scene-inicio").assertIsDisplayed()
        rule.onNodeWithTag("home-pause-on").assertIsDisplayed()
        Shots.save(rule, "home-landscape-paused")
        rule.activityRule.scenario.onActivity { it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT }
        rule.waitForIdle()
        rule.onNodeWithTag("home-pause-on").assertIsDisplayed()
    }
}
