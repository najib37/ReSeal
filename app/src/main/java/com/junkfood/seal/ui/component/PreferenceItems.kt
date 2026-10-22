package com.junkfood.seal.ui.component

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.TipsAndUpdates
import androidx.compose.material.icons.outlined.ToggleOn
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material.icons.outlined.Update
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.junkfood.seal.R
import com.junkfood.seal.ui.theme.FixedAccentColors
import com.junkfood.seal.ui.theme.SealTheme
import com.kyant.monet.LocalTonalPalettes
import com.kyant.monet.TonalPalettes.Companion.toTonalPalettes

private val PreferenceTitle
    @Composable get() = PreferenceDefaults.titleStyle

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PreferenceItem(
    title: String,
    description: String? = null,
    icon: Any? = null,
    enabled: Boolean = true,
    onLongClickLabel: String? = null,
    onLongClick: (() -> Unit)? = null,
    onClickLabel: String? = null,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    onClick: () -> Unit = {},
) {
    PreferenceSegment(
        interaction =
            Modifier.combinedClickable(
                onClick = onClick,
                onClickLabel = onClickLabel,
                enabled = enabled,
                onLongClickLabel = onLongClickLabel,
                onLongClick = onLongClick,
            )
    ) {
        leadingIcon?.let { PreferenceIconContainer(enabled = enabled, content = it) }
        PreferenceLeadingIcon(icon = icon, enabled = enabled)
        PreferenceText(title = title, description = description, enabled = enabled)
        trailingIcon?.let {
            PreferenceTrailingDivider()
            it.invoke()
        }
    }
}

@Composable
private fun PreferenceTrailingDivider() {
    VerticalDivider(
        modifier = Modifier.height(32.dp),
        color = MaterialTheme.colorScheme.outlineVariant,
        thickness = 1.dp,
    )
}

@Composable
@Preview
fun PreferenceItemPreview() {
    SealTheme {
        Surface {
            Column {
                PreferenceSubtitle(text = "Preview")
                PreferenceItem(title = "title", description = "description")
                PreferenceItem(
                    title = "title",
                    description = "description",
                    icon = Icons.Outlined.Update,
                )
                PreferenceItemVariant(
                    title = "title",
                    description = "description",
                    icon = Icons.Outlined.Update,
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PreferenceItemVariant(
    modifier: Modifier = Modifier,
    title: String,
    description: String? = null,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    onLongClickLabel: String? = null,
    onLongClick: () -> Unit = {},
    onClickLabel: String? = null,
    onClick: () -> Unit = {},
) {
    PreferenceSegment(
        modifier = modifier,
        interaction =
            Modifier.combinedClickable(
                enabled = enabled,
                onClick = onClick,
                onClickLabel = onClickLabel,
                onLongClick = onLongClick,
                onLongClickLabel = onLongClickLabel,
            ),
    ) {
        PreferenceLeadingIcon(icon = icon, enabled = enabled)
        PreferenceText(title = title, description = description, enabled = enabled)
    }
}

@Composable
fun PreferenceSingleChoiceItem(
    modifier: Modifier = Modifier,
    text: String,
    selected: Boolean,
    contentPadding: PaddingValues = PreferenceDefaults.ContentPadding,
    onClick: () -> Unit,
) {
    PreferenceSegment(
        modifier = modifier,
        contentPadding = contentPadding,
        interaction =
            Modifier.selectable(selected = selected, onClick = onClick, role = Role.RadioButton),
    ) {
        PreferenceText(title = text, titleMaxLines = 1)
        RadioButton(
            selected = selected,
            onClick = onClick,
            modifier = Modifier.clearAndSetSemantics {},
        )
    }
}

@Composable
@Preview
fun PreferenceSwitchPreview() {
    var b by remember { mutableStateOf(false) }
    SealTheme {
        PreferenceSwitch(
            title = "PreferenceSwitch",
            description = "Supporting text",
            icon = Icons.Outlined.ToggleOn,
            isChecked = b,
        ) {
            b = !b
        }
    }
}

@Composable
@Preview
fun PreferenceSwitchWithDividerPreview() {
    PreferenceSwitchWithDivider(
        title = "PreferenceSwitch",
        description = "Supporting text",
        icon = Icons.Outlined.ToggleOn,
    )
}

@Composable
fun rememberThumbContent(
    isChecked: Boolean,
    checkedIcon: ImageVector = Icons.Outlined.Check,
): (@Composable () -> Unit)? =
    remember(isChecked, checkedIcon) {
        if (isChecked) {
            {
                Icon(
                    imageVector = checkedIcon,
                    contentDescription = null,
                    modifier = Modifier.size(SwitchDefaults.IconSize),
                )
            }
        } else {
            null
        }
    }

@Composable
fun PreferenceSwitchVariant(
    title: String,
    description: String? = null,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    isChecked: Boolean = true,
    thumbContent: (@Composable () -> Unit)? = rememberThumbContent(isChecked = isChecked),
    onClick: (() -> Unit) = {},
) =
    PreferenceSwitch(
        title = title,
        description = description,
        icon = icon,
        enabled = enabled,
        isChecked = isChecked,
        thumbContent = thumbContent,
        onClick = onClick,
    )

@Composable
fun PreferenceSwitch(
    title: String,
    description: String? = null,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    isChecked: Boolean = true,
    thumbContent: (@Composable () -> Unit)? = rememberThumbContent(isChecked = isChecked),
    onClick: (() -> Unit) = {},
) {

    val interactionSource = remember { MutableInteractionSource() }
    PreferenceSegment(
        interaction =
            Modifier.toggleable(
                value = isChecked,
                enabled = enabled,
                onValueChange = { onClick() },
                indication = LocalIndication.current,
                interactionSource = interactionSource,
                role = Role.Switch,
            )
    ) {
        PreferenceLeadingIcon(icon = icon, enabled = enabled)
        PreferenceText(title = title, description = description, enabled = enabled)
        Switch(
            checked = isChecked,
            onCheckedChange = null,
            interactionSource = interactionSource,
            enabled = enabled,
            thumbContent = thumbContent,
        )
    }
}

@Composable
fun PreferenceSwitchWithDivider(
    title: String,
    description: String? = null,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    isSwitchEnabled: Boolean = enabled,
    isChecked: Boolean = true,
    thumbContent: (@Composable () -> Unit)? = rememberThumbContent(isChecked = isChecked),
    onClick: (() -> Unit) = {},
    onChecked: () -> Unit = {},
) {

    PreferenceSegment(
        interaction =
            Modifier.clickable(
                enabled = enabled,
                onClick = onClick,
                onClickLabel = stringResource(id = R.string.open_settings),
                role = Role.Button,
            )
    ) {
        PreferenceLeadingIcon(icon = icon, enabled = enabled)
        PreferenceText(title = title, description = description, enabled = enabled)
        PreferenceTrailingDivider()
        Switch(
            checked = isChecked,
            onCheckedChange = { onChecked() },
            modifier = Modifier.semantics { contentDescription = title },
            enabled = isSwitchEnabled,
            thumbContent = thumbContent,
        )
    }
}

@Preview
@Composable
fun PreferencesHintCardPreview() {
    CompositionLocalProvider(LocalTonalPalettes provides Color.Green.toTonalPalettes()) {
        PreferencesHintCard(
            title = "Explore new features",
            icon = Icons.Outlined.TipsAndUpdates,
            description = "Find out what's new in this version",
            containerColor = FixedAccentColors.primaryFixed,
            contentColor = FixedAccentColors.onPrimaryFixed,
        )
    }
}

@Composable
fun PreferencesHintCard(
    title: String = "Title ".repeat(2),
    description: String? = "Description text ".repeat(3),
    icon: ImageVector? = Icons.Outlined.Translate,
    containerColor: Color = FixedAccentColors.secondaryFixed,
    contentColor: Color = FixedAccentColors.onSecondaryFixed,
    onClick: () -> Unit = {},
) {
    // Standalone highlighted segment: extra vertical space separates it from the grouped list
    PreferenceSegment(
        modifier = Modifier.padding(vertical = 8.dp),
        containerColor = containerColor,
        interaction = Modifier.clickable(role = Role.Button) { onClick() },
    ) {
        PreferenceLeadingIcon(
            icon = icon,
            containerColor = contentColor.copy(alpha = 0.12f),
            contentColor = contentColor,
        )
        PreferenceText(
            title = title,
            description = description,
            titleMaxLines = 1,
            titleColor = contentColor,
            descriptionColor = contentColor,
        )
    }
}

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Night", uiMode = Configuration.UI_MODE_NIGHT_YES)
private fun PreferenceSwitchWithContainerPreview() {
    var isChecked by remember { mutableStateOf(false) }
    SealTheme {
        PreferenceSwitchWithContainer(
            title = "Title ".repeat(2),
            isChecked = isChecked,
            onClick = { isChecked = !isChecked },
            icon = null,
        )
    }
}

@Composable
fun PreferenceSwitchWithContainer(
    title: String,
    icon: ImageVector? = null,
    isChecked: Boolean,
    thumbContent: @Composable (() -> Unit)? = rememberThumbContent(isChecked = isChecked),
    onClick: () -> Unit,
) {

    val interactionSource = remember { MutableInteractionSource() }
    val contentColor = MaterialTheme.colorScheme.onPrimaryContainer
    PreferenceSegment(
        modifier = Modifier.padding(vertical = 8.dp),
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
        interaction =
            Modifier.toggleable(
                value = isChecked,
                onValueChange = { onClick() },
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                role = Role.Switch,
            ),
    ) {
        PreferenceLeadingIcon(
            icon = icon,
            containerColor = contentColor.copy(alpha = 0.12f),
            contentColor = contentColor,
        )
        PreferenceText(title = title, titleColor = contentColor)
        Switch(
            checked = isChecked,
            interactionSource = interactionSource,
            onCheckedChange = null,
            thumbContent = thumbContent,
        )
    }
}

@Composable
fun CreditItem(
    title: String,
    license: String? = null,
    enabled: Boolean = true,
    onClick: () -> Unit = {},
) {
    PreferenceSegment(interaction = Modifier.clickable(role = Role.Button) { onClick() }) {
        PreferenceText(title = title, description = license, enabled = enabled, titleMaxLines = 1)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
@Preview
fun TemplateItem(
    label: String = "",
    template: String? = null,
    selected: Boolean = false,
    isMultiSelectEnabled: Boolean = false,
    checked: Boolean = false,
    onClick: () -> Unit = {},
    onSelect: () -> Unit = {},
    onCheckedChange: (Boolean) -> Unit = {},
    onLongClick: () -> Unit = {},
) {
    PreferenceSegment(
        interaction =
            Modifier.run {
                if (!isMultiSelectEnabled)
                    then(
                        this.combinedClickable(
                            onClick = onClick,
                            onClickLabel = stringResource(R.string.edit),
                            onLongClick = onLongClick,
                            onLongClickLabel = stringResource(R.string.multiselect_mode),
                        )
                    )
                else {
                    then(
                        this.toggleable(
                            value = checked,
                            onValueChange = onCheckedChange,
                            role = Role.Checkbox,
                        )
                    )
                }
            }
    ) {
        AnimatedVisibility(visible = isMultiSelectEnabled) {
            Checkbox(
                modifier = Modifier.clearAndSetSemantics {},
                checked = checked,
                onCheckedChange = onCheckedChange,
            )
        }

        PreferenceText(title = label, description = template, titleMaxLines = 1)

        AnimatedVisibility(!isMultiSelectEnabled) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                PreferenceTrailingDivider()
                RadioButton(
                    modifier = Modifier.semantics { contentDescription = label },
                    selected = selected,
                    onClick = onSelect,
                )
            }
        }
    }
}

@Composable
fun PreferenceSubtitle(
    text: String,
    modifier: Modifier = Modifier,
    // Group title sits slightly inside the cards' edge, as in Pixel Settings
    contentPadding: PaddingValues = PaddingValues(start = 24.dp, top = 24.dp, bottom = 8.dp),
    color: Color = MaterialTheme.colorScheme.primary,
) {
    Text(
        text = text,
        modifier = modifier.padding(contentPadding),
        color = color,
        style = MaterialTheme.typography.labelLargeEmphasized,
    )
}

@Composable
fun PreferenceInfo(
    modifier: Modifier = Modifier,
    text: String,
    icon: ImageVector = Icons.Outlined.Info,
    applyPaddings: Boolean = true,
) {
    Column(
        modifier =
            modifier.fillMaxWidth().run {
                if (applyPaddings) padding(horizontal = 16.dp, vertical = 16.dp) else this
            }
    ) {
        Icon(modifier = Modifier.padding(), imageVector = icon, contentDescription = null)
        Text(
            modifier = Modifier.padding(top = 16.dp),
            text = text,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
@Preview(showBackground = true)
fun PreferenceInfoPreview() {
    PreferenceInfo(text = stringResource(id = R.string.custom_command_enabled_hint))
}
