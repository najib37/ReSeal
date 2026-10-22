package com.junkfood.seal.ui.component

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

/**
 * The large collapsing app bar used by every full-screen page. Built on the M3 Expressive
 * [LargeFlexibleTopAppBar], whose expanded height fits the title instead of reserving the fixed
 * 152dp band the legacy LargeTopAppBar left empty above it.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SealLargeTopAppBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    expandedHeight: Dp = TopAppBarDefaults.LargeFlexibleAppBarWithoutSubtitleExpandedHeight,
    scrollBehavior: TopAppBarScrollBehavior? = null,
) {
    LargeFlexibleTopAppBar(
        title = title,
        modifier = modifier,
        navigationIcon = navigationIcon,
        actions = actions,
        expandedHeight = expandedHeight,
        scrollBehavior = scrollBehavior,
    )
}
