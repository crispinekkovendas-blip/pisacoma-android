package pe.pisacoma.iglesia.data

import java.io.File

/** JVM tests read the real generated files: the bundled assets layered over content/. */
object TestAssets {
    val source: AssetSource = CompositeAssetSource(listOf(FileAssetSource(File("src/main/assets")), FileAssetSource(File("../content"))))
}
