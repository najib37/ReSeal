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
import kotlin.math.roundToInt

/**
 * Quality picker: a tonal card with an Expressive slider that has a thick track, one stop per
 * entry of [labels] (lowest quality first). [selected] is a position in [labels].
 */
@Composable
fun QualitySlider(
    modifier: Modifier = Modifier,
    title: String,
    detail: String,
    icon: ImageVector,
    labels: List<String>,
    selected: Int,
    onSelect: (Int) -> Unit,
) {
    val current = labels.getOrNull(selected) ?: return
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.extraLarge)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
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
                    text = detail,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = current,
                style = MaterialTheme.typography.headlineSmallEmphasized,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        if (labels.size > 1) {
            Spacer(Modifier.height(12.dp))
            Slider(
                value = selected.toFloat(),
                onValueChange = { onSelect(it.roundToInt()) },
                valueRange = 0f..labels.lastIndex.toFloat(),
                steps = (labels.size - 2).coerceAtLeast(0),
                track = { state ->
                    // Thick ("semi-bold") Expressive track
                    SliderDefaults.Track(sliderState = state, modifier = Modifier.height(16.dp))
                },
            )
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = labels.first(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = labels.last(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
