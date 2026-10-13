package pe.pisacoma.iglesia

import android.graphics.Bitmap
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File

/**
 * Saves a PNG of the whole screen (UiAutomation, so dialogs and sheets are included) to the
 * app's external files dir (`/sdcard/Android/data/pe.pisacoma.iglesia/files/shots/<name>.png`),
 * which the CI workflow pulls as the "ui-<run>" artifact: the parity evidence against the site.
 */
object Shots {
    fun save(rule: AndroidComposeTestRule<*, *>, name: String) {
        rule.waitForIdle()
        try {
            val inst = InstrumentationRegistry.getInstrumentation()
            val bitmap = inst.uiAutomation.takeScreenshot() ?: return
            val dir = File(inst.targetContext.getExternalFilesDir(null), "shots").apply { mkdirs() }
            File(dir, "$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 90, it) }
        } catch (e: Throwable) {
            android.util.Log.w("Shots", "could not capture $name: ${e.message}")
        }
    }
}
