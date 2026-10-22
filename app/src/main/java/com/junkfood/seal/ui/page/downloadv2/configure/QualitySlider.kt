package com.junkfood.seal.ui.page.downloadv2.configure

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.junkfood.seal.ui.component.PreferenceLeadingIcon
import com.junkfood.seal.util.Format
import com.junkfood.seal.util.toFileSizeText
import kotlin.math.roundToInt

/**
 * Simple-mode quality picker: a tonal card with an Expressive slider that has a thick track, one
 * stop per [QualityOption]. [selected] is a position in [options], lowest quality first.
 */
@Composable
fun QualitySlider(
    modifier: Modifier = Modifier,
    title: String,
    icon: ImageVector,
    options: List<QualityOption>,
    selected: Int,
    duration: Double,
    onSelect: (Int) -> Unit,
) {
    val current = options.getOrNull(selected) ?: return
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.extraLarge)
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .padding(20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            PreferenceLeadingIcon(icon = icon)
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = current.format.describe(duration),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = current.label,
                style = MaterialTheme.typography.headlineSmallEmphasized,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        if (options.size > 1) {
            Spacer(Modifier.height(12.dp))
            Slider(
                value = selected.toFloat(),
                onValueChange = { onSelect(it.roundToInt()) },
                valueRange = 0f..options.lastIndex.toFloat(),
                steps = (options.size - 2).coerceAtLeast(0),
                track = { state ->
                    // Thick ("semi-bold") Expressive track
                    SliderDefaults.Track(sliderState = state, modifier = Modifier.height(16.dp))
                },
            )
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = options.first().label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = options.last().label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/** "MP4 · AVC1 · 12.3 MB": container, main codec and (estimated) size. */
@Composable
private fun Format.describe(duration: Double): String {
    val codec =
        (if (vcodec != null && vcodec != "none") vcodec else acodec)
            .orEmpty()
            .substringBefore('.')
            .uppercase()
    val size =
        fileSize
            ?: fileSizeApprox
            ?: tbr?.takeIf { duration > 0 }?.let { it * 1000 / 8 * duration }
    return listOfNotNull(ext?.uppercase(), codec.ifEmpty { null }, size?.toFileSizeText())
        .joinToString(" · ")
}
