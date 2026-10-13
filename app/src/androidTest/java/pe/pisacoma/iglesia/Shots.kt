package pe.pisacoma.iglesia

import android.graphics.Bitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.onRoot
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File

/**
 * Saves a PNG of the current screen to the app's external files dir
 * (`/sdcard/Android/data/pe.pisacoma.iglesia/files/shots/<name>.png`), which the CI
 * workflow pulls as the "ui-<run>" artifact: the parity evidence against the site.
 */
object Shots {
    fun save(rule: AndroidComposeTestRule<*, *>, name: String) {
        rule.waitForIdle()
        try {
            val bitmap = rule.onRoot().captureToImage().asAndroidBitmap()
            val ctx = InstrumentationRegistry.getInstrumentation().targetContext
            val dir = File(ctx.getExternalFilesDir(null), "shots").apply { mkdirs() }
            File(dir, "$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        } catch (e: Throwable) {
            // a dialog window on top makes the root capture fail; the screenshot is evidence, not the test
            android.util.Log.w("Shots", "could not capture $name: ${e.message}")
        }
    }
}
