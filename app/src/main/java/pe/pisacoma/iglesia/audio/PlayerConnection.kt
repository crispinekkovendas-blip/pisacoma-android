package pe.pisacoma.iglesia.audio

import android.content.ComponentName
import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class PlayerState(
    val mediaId: String? = null,
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val positionMs: Long = 0,
    val durationMs: Long = 0,
    val error: String? = null,
)

/** The screens' handle on [HymnPlaybackService]: one MediaController, one state flow. */
class PlayerConnection(private val context: Context) {
    private val _state = MutableStateFlow(PlayerState())
    val state: StateFlow<PlayerState> = _state
    private var controller: MediaController? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var ticker: Job? = null
    private val pending = mutableListOf<(MediaController) -> Unit>()

    fun connect() {
        if (controller != null) return
        val token = SessionToken(context, ComponentName(context, HymnPlaybackService::class.java))
        val future = MediaController.Builder(context, token).buildAsync()
        future.addListener({
            val c = runCatching { future.get() }.getOrNull() ?: return@addListener
            controller = c
            c.addListener(object : Player.Listener {
                override fun onEvents(player: Player, events: Player.Events) = refresh(player)
                override fun onPlayerError(error: PlaybackException) {
                    _state.update { it.copy(error = "No se pudo cargar el audio", isPlaying = false, isBuffering = false) }
                }
            })
            refresh(c)
            pending.forEach { it(c) }
            pending.clear()
        }, MoreExecutors.directExecutor())
    }

    private fun refresh(p: Player) {
        _state.update {
            it.copy(
                mediaId = p.currentMediaItem?.mediaId,
                isPlaying = p.isPlaying,
                isBuffering = p.playbackState == Player.STATE_BUFFERING,
                positionMs = p.currentPosition.coerceAtLeast(0),
                durationMs = p.duration.takeIf { d -> d > 0 } ?: 0,
                error = if (p.playerError == null) null else it.error,
            )
        }
        if (p.isPlaying && ticker == null) ticker = scope.launch {
            while (isActive) { delay(500); controller?.let { c -> _state.update { it.copy(positionMs = c.currentPosition.coerceAtLeast(0), durationMs = c.duration.takeIf { d -> d > 0 } ?: 0) } } }
        }
        if (!p.isPlaying) { ticker?.cancel(); ticker = null }
    }

    private fun withController(block: (MediaController) -> Unit) {
        val c = controller
        if (c != null) block(c) else { pending += block; connect() }
    }

    /** Plays `url` as `mediaId`, replacing whatever was playing; toggles when it is already current. */
    fun play(mediaId: String, title: String, url: String) = withController { c ->
        if (c.currentMediaItem?.mediaId == mediaId) {
            if (c.isPlaying) c.pause() else { if (c.playbackState == Player.STATE_IDLE) c.prepare(); c.play() }
            return@withController
        }
        val item = MediaItem.Builder().setMediaId(mediaId).setUri(url)
            .setMediaMetadata(MediaMetadata.Builder().setTitle(title).setArtist("Himnario · Pisacoma").build()).build()
        c.setMediaItem(item)
        c.prepare()
        c.play()
        _state.update { it.copy(mediaId = mediaId, error = null) }
    }

    fun toggle() = withController { c -> if (c.isPlaying) c.pause() else c.play() }
    fun seekTo(ms: Long) = withController { c -> c.seekTo(ms) }
    fun stop() = withController { c -> c.stop(); c.clearMediaItems() }

    fun release() {
        ticker?.cancel()
        controller?.release()
        controller = null
    }
}
