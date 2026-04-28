package com.huanchengfly.tieba.post.ui.widgets.compose

import android.content.Context
import android.net.Uri
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PauseCircleFilled
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.ui.common.theme.compose.ExtendedTheme
import com.huanchengfly.tieba.post.utils.FileUtil
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

private enum class VoicePlayerState {
    Loading,
    Playing,
    Pausing,
}

@Composable
fun VoicePlayer(
    url: String,
    duration: Int,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val controller = remember { ComposeVoicePlayerController(context.applicationContext) }
    var state by remember { mutableStateOf(VoicePlayerState.Pausing) }
    var currentPosition by remember { mutableIntStateOf(0) }
    val currentUrl by VoicePlayerManager.currentUrl.collectAsState()
    val isCurrent = currentUrl == url

    DisposableEffect(controller) {
        controller.addListener(
            object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (!controller.isCurrent(url)) {
                        return
                    }
                    when (playbackState) {
                        Player.STATE_READY -> {
                            state = VoicePlayerState.Playing
                        }
                        Player.STATE_ENDED -> {
                            state = VoicePlayerState.Pausing
                            currentPosition = 0
                        }
                    }
                }

                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    if (controller.isCurrent(url)) {
                        state = if (isPlaying) VoicePlayerState.Playing else VoicePlayerState.Pausing
                    }
                }
            }
        )
        onDispose {
            controller.release()
        }
    }

    LaunchedEffect(url) {
        state = VoicePlayerState.Pausing
        currentPosition = 0
    }

    LaunchedEffect(currentUrl, url) {
        if (currentUrl != url) {
            state = VoicePlayerState.Pausing
            currentPosition = 0
        }
    }

    LaunchedEffect(state, url) {
        while (state == VoicePlayerState.Playing && controller.isCurrent(url)) {
            currentPosition = controller.currentPosition
            delay(500)
        }
    }

    LongClickMenu(
        enabled = url.isNotBlank(),
        menuContent = {
            DropdownMenuItem(
                onClick = {
                    val uri = Uri.parse(url)
                    val md5 = uri.getQueryParameter("voice_md5")
                    FileUtil.downloadBySystem(
                        context,
                        FileUtil.FILE_TYPE_AUDIO,
                        url,
                        "${md5 ?: System.currentTimeMillis()}.mp3"
                    )
                }
            ) {
                Text(text = stringResource(id = R.string.menu_save_audio))
            }
        },
        onClick = {
            state = when {
                !isCurrent -> {
                    controller.play(url)
                    VoicePlayerState.Loading
                }
                controller.isPlaying -> {
                    controller.pause()
                    VoicePlayerState.Pausing
                }
                else -> {
                    controller.resume(url)
                    VoicePlayerState.Playing
                }
            }
        },
        shape = RoundedCornerShape(50.dp),
        modifier = modifier,
    ) {
        VoicePlayerContent(
            state = state,
            currentPosition = currentPosition,
            duration = duration,
        )
    }
}

@Composable
private fun VoicePlayerContent(
    state: VoicePlayerState,
    currentPosition: Int,
    duration: Int,
) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.lottie_audio_wave))
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier.size(18.dp),
            contentAlignment = Alignment.Center,
        ) {
            when (state) {
                VoicePlayerState.Loading -> CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = ExtendedTheme.colors.primary,
                )
                VoicePlayerState.Playing -> Icon(
                    imageVector = Icons.Filled.PauseCircleFilled,
                    contentDescription = null,
                    tint = ExtendedTheme.colors.accent,
                    modifier = Modifier.size(18.dp),
                )
                VoicePlayerState.Pausing -> Icon(
                    imageVector = Icons.Filled.PlayCircleFilled,
                    contentDescription = null,
                    tint = ExtendedTheme.colors.accent,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
        if (state == VoicePlayerState.Playing) {
            LottieAnimation(
                composition = composition,
                iterations = LottieConstants.IterateForever,
                modifier = Modifier
                    .width(96.dp)
                    .height(18.dp),
            )
        }
        Text(
            text = formatVoiceTime(if (state == VoicePlayerState.Playing) currentPosition / 1000 else duration / 1000),
            color = ExtendedTheme.colors.accent,
            modifier = Modifier.width(32.dp),
        )
    }
}

private fun formatVoiceTime(sec: Int): String {
    val min = sec / 60
    return if (min > 0) {
        "$min'${sec % 60}''"
    } else {
        "$sec''"
    }
}

fun releaseVoicePlayer() {
    VoicePlayerManager.release()
}

@Stable
private class ComposeVoicePlayerController(context: Context) {
    private val player = VoicePlayerManager.getPlayer(context)

    fun addListener(listener: Player.Listener) {
        player.addListener(listener)
    }

    fun play(url: String) {
        VoicePlayerManager.play(url)
        player.stop()
        player.setMediaItem(MediaItem.fromUri(url))
        player.prepare()
        player.play()
    }

    fun resume(url: String) {
        if (!isCurrent(url)) {
            play(url)
            return
        }
        player.play()
    }

    fun pause() {
        player.pause()
    }

    fun isCurrent(url: String): Boolean = VoicePlayerManager.currentUrl.value == url

    val isPlaying: Boolean
        get() = player.isPlaying

    val currentPosition: Int
        get() = player.currentPosition.toInt()

    fun release() {
        VoicePlayerManager.release()
    }
}

private object VoicePlayerManager {
    private var player: ExoPlayer? = null
    private val mutableCurrentUrl = MutableStateFlow<String?>(null)
    val currentUrl = mutableCurrentUrl.asStateFlow()

    fun play(url: String) {
        mutableCurrentUrl.value = url
    }

    @OptIn(UnstableApi::class)
    fun getPlayer(context: Context): ExoPlayer {
        if (player == null) {
            player = ExoPlayer.Builder(context)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(C.USAGE_MEDIA)
                        .setContentType(C.AUDIO_CONTENT_TYPE_SPEECH)
                        .build(),
                    true,
                )
                .setHandleAudioBecomingNoisy(true)
                .setRenderersFactory(
                    DefaultRenderersFactory(context).setExtensionRendererMode(
                        DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER
                    )
                )
                .build()
                .apply { playWhenReady = true }
        }
        return player!!
    }

    fun release() {
        player?.release()
        player = null
        mutableCurrentUrl.value = null
    }
}
