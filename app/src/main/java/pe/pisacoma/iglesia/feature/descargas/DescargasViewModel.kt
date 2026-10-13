package pe.pisacoma.iglesia.feature.descargas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import pe.pisacoma.iglesia.data.content.ContentDownloader
import pe.pisacoma.iglesia.data.content.ContentMode
import pe.pisacoma.iglesia.data.content.ContentStore
import pe.pisacoma.iglesia.data.content.DownloadState
import pe.pisacoma.iglesia.data.content.Packs
import pe.pisacoma.iglesia.data.content.formatMb
import pe.pisacoma.iglesia.data.prefs.UserState

data class DescargasUi(
    /** The chosen mode; null on the first open. */
    val current: ContentMode?,
    /** The four options in the spec's order. */
    val options: List<Option>,
    val download: DownloadState,
    /** «Guardado en este teléfono · 12 MB», or empty when nothing is stored. */
    val storedText: String,
    val canContinue: Boolean,
    /** Files the current mode wants that are not on disk (→ «Completar»). */
    val missingCount: Int,
) {
    data class Option(val mode: ContentMode, val desc: String, val size: String)
}

class DescargasViewModel(
    private val store: ContentStore,
    private val downloader: ContentDownloader,
    private val userState: UserState,
) : ViewModel() {
    private val _state = MutableStateFlow(build())
    val state: StateFlow<DescargasUi> = _state

    init {
        viewModelScope.launch { downloader.state.collect { _state.value = build() } }
    }

    private fun current(): ContentMode? = ContentMode.of(userState.string(UserState.Keys.contentMode))

    private fun build(): DescargasUi {
        val current = current()
        val m = store.manifest
        val stored = store.storedBytes()
        return DescargasUi(
            current = current,
            options = listOf(
                DescargasUi.Option(ContentMode.TODO, "Himnarios, Biblia, lecciones, quiz, búsqueda y los 25 libros de EGW.", formatMb(m.totalBytes)),
                DescargasUi.Option(ContentMode.ESENCIAL, "Himnario, Biblia, lecciones, quiz y búsqueda. Los libros de EGW se guardan cuando los abras.", formatMb(m.bytesOf(Packs.ESSENTIAL))),
                DescargasUi.Option(ContentMode.USO, "No descarga nada ahora; cada himno, capítulo o libro que abras se guarda para la próxima vez.", ""),
                DescargasUi.Option(ContentMode.EN_LINEA, "Nada se guarda en el teléfono; hace falta señal para leer.", ""),
            ),
            download = downloader.state.value,
            storedText = if (stored > 0) "Guardado en este teléfono · ${formatMb(stored)}" else "",
            canContinue = current != null,
            missingCount = if (current?.downloads == true) downloader.missingFor(current).size else 0,
        )
    }

    /** Stores the choice; the two download modes start at once and stay «pendiente» until complete. */
    fun choose(mode: ContentMode) {
        userState.set(UserState.Keys.contentMode, mode.value)
        if (mode.downloads) {
            userState.set(UserState.Keys.contentPending, true)
            downloader.start(mode)
        } else {
            userState.set(UserState.Keys.contentPending, false)
        }
        _state.value = build()
    }

    /** «Reintentar» / «Completar»: download what the current mode still lacks. */
    fun complete() {
        val mode = current() ?: return
        if (mode.downloads) {
            userState.set(UserState.Keys.contentPending, true)
            downloader.start(mode)
        }
        _state.value = build()
    }

    /** «Borrar lo descargado»: empties the store, keeps the mode. */
    fun clear() {
        store.clear()
        if (current()?.downloads == true) userState.set(UserState.Keys.contentPending, true)
        _state.value = build()
    }

    fun refresh() { _state.value = build() }
}
