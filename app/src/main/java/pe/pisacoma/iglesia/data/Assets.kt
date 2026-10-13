package pe.pisacoma.iglesia.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File

/** Where the bundled content is read from: the APK's assets on a phone, a folder in JVM tests. */
interface AssetSource {
    suspend fun read(path: String): String
    suspend fun exists(path: String): Boolean
}

class AndroidAssetSource(private val context: Context) : AssetSource {
    override suspend fun read(path: String): String = withContext(Dispatchers.IO) {
        context.assets.open(path).bufferedReader(Charsets.UTF_8).use { it.readText() }
    }
    override suspend fun exists(path: String): Boolean = withContext(Dispatchers.IO) {
        runCatching { context.assets.open(path).close(); true }.getOrDefault(false)
    }
}

class FileAssetSource(private val root: File) : AssetSource {
    override suspend fun read(path: String): String = withContext(Dispatchers.IO) { File(root, path).readText(Charsets.UTF_8) }
    override suspend fun exists(path: String): Boolean = File(root, path).isFile
}

/** Several sources layered: the first one that has the path answers (JVM tests read assets + content/). */
class CompositeAssetSource(private val sources: List<AssetSource>) : AssetSource {
    override suspend fun read(path: String): String =
        (sources.firstOrNull { it.exists(path) } ?: throw java.io.FileNotFoundException(path)).read(path)
    override suspend fun exists(path: String): Boolean = sources.any { it.exists(path) }
}

/** One lenient parser for every bundled file: unknown keys are ignored so the site can add fields. */
val AppJson: Json = Json { ignoreUnknownKeys = true; isLenient = true; coerceInputValues = true }
