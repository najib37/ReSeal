package com.junkfood.seal.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role

/** Top-level settings entry; shares the segment layout with every other preference row. */
@Composable
fun SettingItem(title: String, description: String, icon: ImageVector?, onClick: () -> Unit) {
    PreferenceSegment(interaction = Modifier.clickable(role = Role.Button) { onClick() }) {
        PreferenceLeadingIcon(icon = icon)
        PreferenceText(title = title, description = description, titleMaxLines = 1)
    }
}
