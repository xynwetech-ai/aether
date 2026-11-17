package com.aethlauncher.kyn.core.theme

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.delay

/** Spesifikasi gerak. Jika animasi dimatikan (Safe Mode, hemat baterai, reduced motion) langsung snap. */
object AetherMotion {
    fun <T> bouncy(enabled: Boolean): FiniteAnimationSpec<T> =
        if (enabled) spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessMedium) else snap()

    fun <T> soft(enabled: Boolean): FiniteAnimationSpec<T> =
        if (enabled) spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMediumLow) else snap()
}

/** Efek "kenyal": mengecil saat ditekan lalu memantul kembali. */
@Composable
fun Modifier.pressScale(pressed: Boolean, pressedScale: Float = 0.86f): Modifier {
    val enabled = LocalRenderPolicy.current.animationsEnabled
    val scale = animateFloatAsState(
        targetValue = if (pressed) pressedScale else 1f,
        animationSpec = AetherMotion.bouncy(enabled),
        label = "pressScale"
    )
    return this.graphicsLayer {
        scaleX = scale.value
        scaleY = scale.value
    }
}

/** Muncul bertahap dengan pantulan. [index] menentukan jeda antar item. */
@Composable
fun Modifier.staggerIn(index: Int): Modifier {
    val enabled = LocalRenderPolicy.current.animationsEnabled
    val progress = remember { Animatable(if (enabled) 0f else 1f) }

    LaunchedEffect(Unit) {
        if (enabled) {
            delay((index * 16L).coerceAtMost(280L))
            progress.animateTo(1f, spring(dampingRatio = 0.62f, stiffness = 320f))
        }
    }
    return this.graphicsLayer {
        val p = progress.value
        alpha = p.coerceIn(0f, 1f)
        val s = 0.6f + 0.4f * p
        scaleX = s
        scaleY = s
    }
}
