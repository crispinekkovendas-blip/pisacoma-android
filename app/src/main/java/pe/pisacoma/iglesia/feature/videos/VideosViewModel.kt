package pe.pisacoma.iglesia.feature.videos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.pisacoma.iglesia.data.net.Http
import pe.pisacoma.iglesia.data.prefs.UserState

data class VideoRowUi(val key: String, val label: String, val num: String, val videos: List<TaggedVideo>)

data class VideosUi(
    val videos: List<TaggedVideo> = emptyList(),
    val loading: Boolean = true,
    val offline: Boolean = false,
    val query: String = "",
) {
    val isSearching get() = query.trim().isNotEmpty()
    val results: List<TaggedVideo> get() = if (!isSearching) emptyList() else videos.filter { it.title.lowercase().contains(query.trim().lowercase()) }
    val hero: TaggedVideo? get() = videos.firstOrNull { it.hero } ?: videos.firstOrNull()

    /** Site rows: «Añadidos recientemente», one per channel, one per category (only non-empty). */
    val rows: List<VideoRowUi>
        get() {
            val out = mutableListOf(VideoRowUi("recientes", "Añadidos recientemente", "01", videos.take(24)))
            var n = 2
            for (ch in VideoCatalog.CHANNELS) {
                val vs = videos.filter { "channel:${ch.key}" in it.tags }
                if (vs.isNotEmpty()) { out += VideoRowUi("channel:${ch.key}", ch.label, n.toString().padStart(2, '0'), vs); n++ }
            }
            for (c in VideoCatalog.CATS) {
                val vs = videos.filter { c.key in it.tags }
                if (vs.isNotEmpty()) { out += VideoRowUi(c.key, c.label, n.toString().padStart(2, '0'), vs); n++ }
            }
            return out
        }
}

/**
 * Loads the catalogue from the site's /videos/api/youtube (the four channels' RSS, merged and
 * sorted there) and keeps the last good copy for 24 h so the screen opens offline.
 */
class VideosViewModel(private val userState: UserState) : ViewModel() {
    private val _state = MutableStateFlow(VideosUi())
    val state: StateFlow<VideosUi> = _state

    init { load() }

    fun load() = viewModelScope.launch {
        _state.update { it.copy(loading = true, offline = false) }
        val cached = userState.string(UserState.Keys.videosCache)
        val cachedAt = userState.string(UserState.Keys.videosCacheAt)?.toLongOrNull() ?: 0L
        val fresh = cachedAt > System.currentTimeMillis() - 24 * 3_600_000L
        if (cached != null && fresh) { show(cached); return@launch }
        try {
            val body = Http.get(VideoCatalog.FEED_URL)
            if (VideoCatalog.parseFeed(body).isNotEmpty()) {
                userState.set(UserState.Keys.videosCache, body)
                userState.set(UserState.Keys.videosCacheAt, System.currentTimeMillis().toString())
            }
            show(body)
        } catch (e: Exception) {
            if (cached != null) show(cached) else _state.update { it.copy(loading = false, offline = true) }
        }
    }

    private fun show(body: String) {
        val seen = HashSet<String>()
        val list = VideoCatalog.parseFeed(body).filter { it.videoId.isNotEmpty() && seen.add(it.videoId) }.map(VideoCatalog::tagVideo)
        _state.update { it.copy(videos = list, loading = false, offline = list.isEmpty()) }
    }

    fun query(q: String) = _state.update { it.copy(query = q) }
}
