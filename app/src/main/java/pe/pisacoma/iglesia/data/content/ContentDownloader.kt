package pe.pisacoma.iglesia.data.content

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.IOException

sealed class DownloadState {
    data object Idle : DownloadState()
    data class Running(val doneFiles: Int, val totalFiles: Int, val doneBytes: Long, val totalBytes: Long, val failed: Int) : DownloadState()
    data class Done(val totalBytes: Long) : DownloadState()
    data class Error(val failed: Int, val totalFiles: Int) : DownloadState()
}

/**
 * Downloads the packs a mode asks for, one file after another, skipping what is already stored
 * and retrying each file up to three times. [onComplete] gets `true` when every file is on disk.
 */
class ContentDownloader(
    private val store: ContentStore,
    private val scope: CoroutineScope,
    private val onComplete: (Boolean) -> Unit = {},
) {
    private val _state = MutableStateFlow<DownloadState>(DownloadState.Idle)
    val state: StateFlow<DownloadState> = _state

    @Volatile private var running = false

    /** The files of [mode] that are not stored yet. */
    fun missingFor(mode: ContentMode): List<ContentFile> = store.manifest.filesFor(mode).filter { !store.isStored(it.path) }

    /** Starts the download; null when nothing is missing (state becomes Done) or one is already running. */
    fun start(mode: ContentMode): Job? {
        if (running) return null
        val all = store.manifest.filesFor(mode)
        val todo = all.filter { !store.isStored(it.path) }
        val totalBytes = all.sumOf { it.bytes }
        if (todo.isEmpty()) {
            _state.value = DownloadState.Done(totalBytes)
            onComplete(true)
            return null
        }
        running = true
        val doneBefore = all.size - todo.size
        val bytesBefore = totalBytes - todo.sumOf { it.bytes }
        _state.value = DownloadState.Running(doneBefore, all.size, bytesBefore, totalBytes, 0)
        return scope.launch {
            var done = doneBefore
            var bytes = bytesBefore
            var failed = 0
            try {
                for (f in todo) {
                    if (fetchWithRetries(f.path)) { done++; bytes += f.bytes } else failed++
                    _state.value = DownloadState.Running(done, all.size, bytes, totalBytes, failed)
                }
                // The pending flag is settled before the final state is published, so whoever
                // waits on the state sees both consistent.
                onComplete(failed == 0)
                _state.value = if (failed == 0) DownloadState.Done(totalBytes) else DownloadState.Error(failed, all.size)
            } finally {
                running = false
            }
        }
    }

    private suspend fun fetchWithRetries(path: String): Boolean {
        repeat(ATTEMPTS) {
            try {
                store.download(path)
                return true
            } catch (e: IOException) {
                // try again
            }
        }
        return false
    }

    companion object { const val ATTEMPTS = 3 }
}
