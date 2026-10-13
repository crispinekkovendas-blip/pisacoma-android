package pe.pisacoma.iglesia.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import pe.pisacoma.iglesia.ui.theme.Tokens

/**
 * The hymn page's `.yt` box for the Novo Hinário (NHA): YouTube's own IFrame player (YouTube
 * requires its embed) in a 16:9 box capped at 160 dp, with the site's forest gradient behind it.
 */
@Composable
fun YouTubeEmbed(videoId: String, modifier: Modifier = Modifier, autoplay: Boolean = false) {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val shape = RoundedCornerShape(16.dp)
    Box(
        modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .heightIn(max = 160.dp)
            .clip(shape)
            .background(Brush.linearGradient(listOf(Tokens.forest, Tokens.inkDeep)))
            .border(1.dp, Tokens.sageA(0.22f), shape)
            .testTag("youtube-$videoId"),
    ) {
        AndroidView(
            factory = { ctx ->
                YouTubePlayerView(ctx).apply {
                    enableAutomaticInitialization = false
                    lifecycle.addObserver(this)
                    initialize(
                        object : AbstractYouTubePlayerListener() {
                            override fun onReady(youTubePlayer: YouTubePlayer) {
                                if (autoplay) youTubePlayer.loadVideo(videoId, 0f) else youTubePlayer.cueVideo(videoId, 0f)
                            }
                        },
                        IFramePlayerOptions.Builder(ctx).controls(1).rel(0).build(),
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
