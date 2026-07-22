package com.srpski.app.ui.media

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView

/**
 * Compose wrapper over YouTube IFrame Player. Auto-plays [videoId] from
 * [startSec] and pauses at [endSec] (if provided). Provides simple
 * Replay + Slow controls. Player is released when leaving composition.
 */
@Composable
fun YouTubeClipPlayer(
    videoId: String,
    startSec: Int?,
    endSec: Int?,
    modifier: Modifier = Modifier
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    var player by remember { mutableStateOf<YouTubePlayer?>(null) }
    var slow by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(true) }
    val startSecSafe = (startSec ?: 0).coerceAtLeast(0).toFloat()

    Column(modifier = modifier) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
        ) {
            AndroidView(
                factory = { ctx ->
                    val options = IFramePlayerOptions.Builder()
                        .controls(1)
                        .rel(0)
                        .ivLoadPolicy(3)
                        .build()
                    YouTubePlayerView(ctx).apply {
                        enableAutomaticInitialization = false
                        lifecycleOwner.lifecycle.addObserver(this)
                        initialize(object : AbstractYouTubePlayerListener() {
                            override fun onReady(youTubePlayer: YouTubePlayer) {
                                player = youTubePlayer
                                loading = false
                                youTubePlayer.loadVideo(videoId, startSecSafe)
                            }

                            override fun onCurrentSecond(youTubePlayer: YouTubePlayer, second: Float) {
                                val end = endSec ?: return
                                if (end > 0 && second >= end) {
                                    youTubePlayer.pause()
                                    youTubePlayer.seekTo(startSecSafe)
                                }
                            }
                        }, options)
                    }
                }
            )
        }

        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = {
                player?.seekTo(startSecSafe)
                player?.play()
            }, enabled = !loading) { Text("Replay") }
            OutlinedButton(onClick = {
                slow = !slow
                player?.setPlaybackRate(
                    if (slow) com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants.PlaybackRate.RATE_0_75
                    else com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants.PlaybackRate.RATE_1
                )
            }, enabled = !loading) { Text(if (slow) "Normal speed" else "Slow (0.75x)") }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            player = null
        }
    }
}
