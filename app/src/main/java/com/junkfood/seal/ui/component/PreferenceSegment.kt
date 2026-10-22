package com.junkfood.seal.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.junkfood.seal.ui.theme.applyOpacity

/**
 * Shared metrics for every preference row, so spacing and type stay consistent across all settings
 * screens. Rows render as M3 Expressive list segments: tonal containers separated by a small gap.
 */
object PreferenceDefaults {
    /** Distance between a segment and the screen edge. */
    val ScreenPadding = 16.dp
    /** Visual gap between two stacked segments. */
    val SegmentGap = 2.dp
    val ContentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp)
    val MinHeight = 64.dp
    val IconContainerSize = 40.dp
    val IconSize = 20.dp

    /** Shape of a standalone segment and of a group's outer corners. */
    val shape: Shape
        @Composable get() = MaterialTheme.shapes.extraLarge

    /** Corners where two segments of the same group meet. */
    val innerShape: Shape
        @Composable get() = MaterialTheme.shapes.extraSmall

    val containerColor: Color
        @Composable get() = MaterialTheme.colorScheme.surfaceContainer

    val titleStyle: TextStyle
        @Composable get() = MaterialTheme.typography.titleMedium

    val descriptionStyle: TextStyle
        @Composable get() = MaterialTheme.typography.bodyMedium
}

/**
 * The container all preference rows share. [interaction] carries the click/toggle modifier so the
 * ripple is clipped to the segment shape.
 */
@Composable
fun PreferenceSegment(
    modifier: Modifier = Modifier,
    interaction: Modifier = Modifier,
    containerColor: Color = PreferenceDefaults.containerColor,
    contentPadding: PaddingValues = PreferenceDefaults.ContentPadding,
    content: @Composable RowScope.() -> Unit,
) {
    // Inside a PreferenceGroup the group owns margins, gaps and the large outer corners
    val inGroup = LocalInPreferenceGroup.current
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .then(
                    if (inGroup) Modifier
                    else
                        Modifier.padding(
                            horizontal = PreferenceDefaults.ScreenPadding,
                            vertical = PreferenceDefaults.SegmentGap,
                        )
                )
                .clip(if (inGroup) PreferenceDefaults.innerShape else PreferenceDefaults.shape)
                .background(containerColor)
                .then(interaction)
                .heightIn(min = PreferenceDefaults.MinHeight)
                .padding(contentPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        content = content,
    )
}

private val LocalInPreferenceGroup = staticCompositionLocalOf { false }

/**
 * M3 Expressive segmented list group, as used by the Pixel Settings app: rows are separated by
 * [ListItemDefaults.SegmentedGap], the first and last rows get the large outer corners and inner
 * edges keep small corners. Clipping the column gives exactly the shapes
 * `ListItemDefaults.segmentedShapes(index, count)` produces, without callers tracking indices, so
 * rows can be shown or hidden conditionally.
 */
@Composable
fun PreferenceGroup(
    modifier: Modifier = Modifier,
    title: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        title?.let { PreferenceSubtitle(text = it) }
        Column(
            modifier =
                Modifier.padding(horizontal = PreferenceDefaults.ScreenPadding)
                    .clip(PreferenceDefaults.shape),
            verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
        ) {
            CompositionLocalProvider(LocalInPreferenceGroup provides true) { content() }
        }
        Spacer(Modifier.height(PreferenceDefaults.SegmentGap))
    }
}

/** Leading icon in a tonal circle, the M3 Expressive list treatment. Accepts ImageVector/Painter. */
@Composable
fun PreferenceLeadingIcon(
    icon: Any?,
    enabled: Boolean = true,
    containerColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onSecondaryContainer,
) {
    if (icon !is ImageVector && icon !is Painter) return
    PreferenceIconContainer(enabled = enabled, containerColor = containerColor) {
        val iconModifier = Modifier.size(PreferenceDefaults.IconSize)
        val tint = contentColor.applyOpacity(enabled)
        when (icon) {
            is ImageVector ->
                Icon(icon, contentDescription = null, modifier = iconModifier, tint = tint)
            is Painter -> Icon(icon, contentDescription = null, modifier = iconModifier, tint = tint)
        }
    }
}

/** Tonal circle for custom leading content (e.g. a progress indicator) so it lines up too. */
@Composable
fun PreferenceIconContainer(
    enabled: Boolean = true,
    containerColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onSecondaryContainer,
    content: @Composable () -> Unit,
) {
    Box(
        modifier =
            Modifier.size(PreferenceDefaults.IconContainerSize)
                .clip(CircleShape)
                .background(containerColor.applyOpacity(enabled)),
        contentAlignment = Alignment.Center,
    ) {
        CompositionLocalProvider(LocalContentColor provides contentColor.applyOpacity(enabled)) {
            content()
        }
    }
}

/** Title + optional description column; takes the remaining width of the row. */
@Composable
fun RowScope.PreferenceText(
    title: String,
    description: String? = null,
    enabled: Boolean = true,
    titleMaxLines: Int = 2,
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    descriptionColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    Column(modifier = Modifier.weight(1f)) {
        Text(
            text = title,
            maxLines = titleMaxLines,
            overflow = TextOverflow.Ellipsis,
            style = PreferenceDefaults.titleStyle,
            color = titleColor.applyOpacity(enabled),
        )
        if (!description.isNullOrEmpty()) {
            Text(
                text = description,
                modifier = Modifier.padding(top = 2.dp),
                style = PreferenceDefaults.descriptionStyle,
                color = descriptionColor.applyOpacity(enabled),
            )
        }
    }
}
