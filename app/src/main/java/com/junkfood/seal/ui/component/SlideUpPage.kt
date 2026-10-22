package com.junkfood.seal.ui.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

// MD3 emphasized decelerate / accelerate curves for entering and exiting the screen
private val EmphasizedDecelerate = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)
private val EmphasizedAccelerate = CubicBezierEasing(0.3f, 0f, 0.8f, 0.15f)

/** Drives a [SlideUpPage]: [show] and [hide] suspend until the animation finishes. */
@Stable
class SlideUpPageState {
    // 1f = fully below the screen, 0f = fully shown
    internal val offsetFraction = Animatable(1f)

    suspend fun show() = offsetFraction.animateTo(0f, tween(400, easing = EmphasizedDecelerate))

    suspend fun hide() = offsetFraction.animateTo(1f, tween(200, easing = EmphasizedAccelerate))
}

@Composable fun rememberSlideUpPageState(): SlideUpPageState = remember { SlideUpPageState() }

/** A full-screen page that slides up over the current screen. Content draws its own surface. */
@Composable
fun SlideUpPage(
    state: SlideUpPageState,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier =
            modifier.fillMaxSize().graphicsLayer {
                translationY = size.height * state.offsetFraction.value
            }
    ) {
        Column(modifier = Modifier.weight(1f)) { content() }
        NavigationBarSpacer(
            modifier =
                Modifier.background(MaterialTheme.colorScheme.surfaceContainerHigh).fillMaxWidth()
        )
    }
}
