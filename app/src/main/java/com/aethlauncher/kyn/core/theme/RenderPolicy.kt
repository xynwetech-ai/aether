package com.aethlauncher.kyn.core.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Menentukan efek visual mana yang boleh aktif. Safe Mode mematikan semuanya.
 * Fitur visual wajib membaca policy ini.
 */
@Immutable
data class RenderPolicy(
    val animationsEnabled: Boolean,
    val blurEnabled: Boolean,
    val dynamicColorEnabled: Boolean,
    val experimentalEnabled: Boolean,
    val widgetsEnabled: Boolean
) {
    companion object {
        val Standard = RenderPolicy(
            animationsEnabled = true,
            blurEnabled = true,
            dynamicColorEnabled = true,
            experimentalEnabled = true,
            widgetsEnabled = true
        )

        val Safe = RenderPolicy(
            animationsEnabled = false,
            blurEnabled = false,
            dynamicColorEnabled = false,
            experimentalEnabled = false,
            widgetsEnabled = false
        )

        fun forSafeMode(safeMode: Boolean): RenderPolicy = if (safeMode) Safe else Standard
    }
}

val LocalRenderPolicy = staticCompositionLocalOf { RenderPolicy.Standard }

val LocalVisualStyle = staticCompositionLocalOf { VisualStyle(ThemeStyle.LIQUID_GLASS, false) }
