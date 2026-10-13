package pe.pisacoma.iglesia.data.content

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import pe.pisacoma.iglesia.data.AssetSource
import java.io.File
import java.io.IOException

/** A file that is neither bundled nor stored, asked for while offline. */
class ContentMissing(val path: String) : IOException("content not stored and offline: $path")

/**
 * Every content read goes through here: the APK's assets first, then the files already saved
 * under [dir] (`filesDir/content`), then the network. What is fetched is kept on disk unless
 * the mode is «Mantener solo en línea». A fetch that fails offline sets [missing] so the chrome
 * can show one notice, and throws [ContentMissing].
 */
class ContentStore(
    private val bundled: AssetSource,
    val dir: File,
    val manifest: Manifest,
    private val mode: () -> ContentMode?,
    private val fetch: suspend (String) -> ByteArray,
) : AssetSource {
    /** The last path that could not be read offline; cleared by the notice's buttons. */
    val missing = MutableStateFlow<String?>(null)

    private fun file(path: String) = File(dir, path)

    /** On disk with the size the manifest expects — so a new APK's manifest makes changed files download again. */
    fun isStored(path: String): Boolean {
        val f = file(path)
        if (!f.isFile) return false
        val expected = manifest.byPath(path)?.bytes ?: return true
        return f.length() == expected
    }

    private fun storedFiles(): Sequence<File> = dir.walkTopDown().filter { it.isFile && !it.name.endsWith(".part") }

    fun storedBytes(): Long = storedFiles().sumOf { it.length() }

    fun storedCount(): Int = storedFiles().count()

    override suspend fun exists(path: String): Boolean =
        bundled.exists(path) || isStored(path) || manifest.byPath(path) != null

    override suspend fun read(path: String): String {
        if (bundled.exists(path)) return bundled.read(path)
        return withContext(Dispatchers.IO) {
            val f = file(path)
            if (isStored(path)) return@withContext f.readText(Charsets.UTF_8)
            val bytes = fetchVerified(path, report = true)
            if (mode()?.persists != false) writeAtomic(f, bytes)
            String(bytes, Charsets.UTF_8)
        }
    }

    /**
     * Saves one file for later; a no-op when it is already stored. Throws on network trouble
     * without raising the notice: a background download failing is not the reader's problem.
     */
    suspend fun download(path: String) {
        withContext(Dispatchers.IO) {
            val f = file(path)
            if (isStored(path)) return@withContext
            writeAtomic(f, fetchVerified(path, report = false))
        }
    }

    /** Deletes every stored file (the mode is kept). */
    fun clear() {
        dir.deleteRecursively()
    }

    private suspend fun fetchVerified(path: String, report: Boolean): ByteArray {
        val bytes = try {
            fetch(path)
        } catch (e: IOException) {
            if (report) missing.value = path
            throw ContentMissing(path)
        }
        val expected = manifest.byPath(path)?.bytes
        if (expected != null && bytes.size.toLong() != expected) throw IOException("short body for $path: ${bytes.size} of $expected bytes")
        return bytes
    }

    private fun writeAtomic(target: File, bytes: ByteArray) {
        target.parentFile?.mkdirs()
        val part = File(target.path + ".part")
        part.writeBytes(bytes)
        if (!part.renameTo(target)) {
            target.delete()
            if (!part.renameTo(target)) throw IOException("cannot write ${target.path}")
        }
    }
}
