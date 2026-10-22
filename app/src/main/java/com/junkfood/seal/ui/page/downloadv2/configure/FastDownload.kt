package com.junkfood.seal.ui.page.downloadv2.configure

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AudioFile
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.VideoFile
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.junkfood.seal.R
import com.junkfood.seal.util.DownloadUtil

/** Fixed video stops for Fast download, lowest first. */
val FAST_HEIGHTS = listOf(360, 480, 720, 1080, 1440, 2160)

/** Target audio bitrates (kbps) for Low / Medium / High. */
private val FAST_AUDIO_KBPS = listOf(64, 128, 192)

private fun heightLabel(height: Int) = if (height == 2160) "4K" else "${height}p"

/**
 * Preferences for an optimistic download chosen without probing formats. yt-dlp sorts by
 * resolution first, so it takes the best format at or below [height] and falls back to the next
 * lower one; MP4/M4A with H.264 wins ties so the file plays everywhere. A null [height] downloads
 * audio only.
 */
fun DownloadUtil.DownloadPreferences.toFastDownload(
    height: Int?,
    audioLevel: Int,
): DownloadUtil.DownloadPreferences {
    val abr = FAST_AUDIO_KBPS.getOrElse(audioLevel) { FAST_AUDIO_KBPS.last() }
    return copy(
        extractAudio = height == null,
        formatIdString = "",
        formatSorting = true,
        sortingFields =
            if (height != null) "res:$height,ext:mp4:m4a,vcodec:h264,abr~$abr"
            else "abr~$abr,ext:m4a",
    )
}

/** Fast download sliders shown inline in the configure sheet. */
@Composable
fun FastDownloadOptions(
    modifier: Modifier = Modifier,
    audioOnly: Boolean,
    heightIndex: Int,
    audioLevel: Int,
    onHeightIndexChange: (Int) -> Unit,
    onAudioLevelChange: (Int) -> Unit,
) {
    val audioLabels =
        listOf(
            stringResource(R.string.low_quality),
            stringResource(R.string.medium_quality),
            stringResource(R.string.high_quality),
        )
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (!audioOnly) {
            QualitySlider(
                title = stringResource(R.string.video_quality),
                detail = stringResource(R.string.fast_download_video_detail),
                icon = Icons.Outlined.VideoFile,
                labels = FAST_HEIGHTS.map(::heightLabel),
                selected = heightIndex,
                onSelect = onHeightIndexChange,
            )
        }
        QualitySlider(
            title = stringResource(R.string.audio_quality),
            detail = "~${FAST_AUDIO_KBPS[audioLevel]} kbps · M4A",
            icon = Icons.Outlined.AudioFile,
            labels = audioLabels,
            selected = audioLevel,
            onSelect = onAudioLevelChange,
        )
        Text(
            text = stringResource(R.string.fast_download_note),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 4.dp),
        )
    }
}
