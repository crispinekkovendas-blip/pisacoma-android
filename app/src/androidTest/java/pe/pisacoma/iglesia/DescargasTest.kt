package pe.pisacoma.iglesia

import android.content.Intent
import android.net.Uri
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import pe.pisacoma.iglesia.data.net.Http

/**
 * /descargas: the first open is the chooser and nothing else; the four modes; offline states.
 * The activity decides «first run» in onCreate, so every test prepares the state and then launches.
 */
@RunWith(AndroidJUnit4::class)
class DescargasTest {
    @get:Rule val rule = createEmptyComposeRule()
    private var scenario: ActivityScenario<MainActivity>? = null

    @After fun tearDown() {
        scenario?.close()
        Http.forceOffline = false
        App.content.missing.value = null
        TestState.restore()
    }

    private fun launch(path: String? = null) {
        val ctx = InstrumentationRegistry.getInstrumentation().targetContext
        val intent = Intent(ctx, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            if (path != null) { action = Intent.ACTION_VIEW; data = Uri.parse("https://quiz-qwik.vercel.app$path") }
        }
        scenario = ActivityScenario.launch(intent)
        rule.waitForIdle()
    }

    private val modes = listOf("todo", "esencial", "uso", "en-linea")

    @Test
    fun firstRunShowsTheFourOptionsAndNothingElse() {
        TestState.setMode(null)
        launch()
        rule.onNodeWithTag("descargas").assertExists()
        for (m in modes) {
            rule.onNodeWithTag("descargas").performScrollToNode(hasTestTag("modo-$m"))
            rule.onNodeWithTag("modo-$m").assertExists()
        }
        rule.onNodeWithTag("descargas").performScrollToNode(hasTestTag("descargas-continuar"))
        rule.onNodeWithTag("descargas-continuar").assertIsNotEnabled()
        rule.onNodeWithTag("descargas").performScrollToNode(hasTestTag("modo-todo"))
        Shots.save(rule, "descargas-primera-vez")
        // No menu button: the chooser is the whole screen.
        assertEquals(0, rule.onAllNodes(hasTestTag("nav-menu")).fetchSemanticsNodes().size)
    }

    @Test
    fun chooseUsoContinuesToTheHomeAndStoresTheMode() {
        TestState.setMode(null)
        launch()
        rule.onNodeWithTag("descargas").performScrollToNode(hasTestTag("modo-uso"))
        rule.onNodeWithTag("modo-uso").performClick()
        rule.onNodeWithTag("descargas").performScrollToNode(hasTestTag("descargas-continuar"))
        rule.onNodeWithTag("descargas-continuar").assertIsEnabled()
        Shots.save(rule, "descargas-uso-elegido")
        rule.onNodeWithTag("descargas-continuar").performClick()
        rule.waitUntil(10_000) { rule.onAllNodes(hasTestTag("home")).fetchSemanticsNodes().isNotEmpty() }
        assertEquals("uso", TestState.mode())
    }

    @Test
    fun offlineFirstRunShowsTheErrorState() {
        Http.forceOffline = true
        TestState.firstRun()
        launch()
        rule.onNodeWithTag("descargas").performScrollToNode(hasTestTag("modo-todo"))
        rule.onNodeWithTag("modo-todo").performClick()
        rule.waitUntil(30_000) { rule.onAllNodes(hasText("No se pudo guardar todo", substring = true)).fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithTag("descargas").performScrollToNode(hasTestTag("descarga-progreso"))
        Shots.save(rule, "descargas-error")
        rule.onNodeWithTag("descargas-continuar").assertIsEnabled()
    }

    @Test
    fun offlineMissingContentShowsTheNoticeAndLeadsToDescargas() {
        TestState.setMode("uso")
        TestState.wipeContent()
        Http.forceOffline = true
        // Apocalipsis: a book no earlier test opened, so the repository's in-process cache cannot answer.
        launch("/biblia?libro=66&cap=1")
        rule.waitUntil(15_000) { rule.onAllNodes(hasTestTag("sin-conexion")).fetchSemanticsNodes().isNotEmpty() }
        Shots.save(rule, "sin-conexion-biblia")
        rule.onNodeWithTag("sin-conexion-descargas").performClick()
        rule.waitUntil(10_000) { rule.onAllNodes(hasTestTag("descargas")).fetchSemanticsNodes().isNotEmpty() }
        Shots.save(rule, "descargas-desde-aviso")
    }

    @Test
    fun firstRunContinuesToTheDeepLink() {
        TestState.setMode(null)
        launch("/himnario")
        rule.onNodeWithTag("descargas").performScrollToNode(hasTestTag("modo-en-linea"))
        rule.onNodeWithTag("modo-en-linea").performClick()
        rule.onNodeWithTag("descargas").performScrollToNode(hasTestTag("descargas-continuar"))
        rule.onNodeWithTag("descargas-continuar").performClick()
        rule.waitUntil(10_000) { rule.onAllNodes(hasTestTag("hymn-list")).fetchSemanticsNodes().isNotEmpty() }
        assertEquals("en-linea", TestState.mode())
    }
}
