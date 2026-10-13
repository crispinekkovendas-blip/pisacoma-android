package pe.pisacoma.iglesia

import androidx.test.runner.AndroidJUnitRunner

/**
 * Before any UI test: copy the downloadable content (bundled in the test APK as its own assets,
 * see app/build.gradle.kts) into the app's store and mark the mode «todo», so every screen
 * reads from disk and no test needs the network.
 */
class ContentTestRunner : AndroidJUnitRunner() {
    override fun onStart() {
        TestState.seedContent()
        TestState.setMode("todo")
        super.onStart()
    }
}
