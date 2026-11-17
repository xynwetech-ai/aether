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
    val widgetsEnabled: Boolean,
    val blurRadius: Int = 8,
    val blurWidth: Int = 128
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

        /** Menggabungkan Safe Mode, pilihan kualitas, preferensi animasi, dan kondisi perangkat. */
        fun resolve(
            safeMode: Boolean,
            quality: RenderQuality,
            animations: Boolean,
            reducedMotion: Boolean,
            lowRam: Boolean
        ): RenderPolicy {
            if (safeMode) return Safe
            val effective = if (quality == RenderQuality.AUTO) {
                if (lowRam) RenderQuality.BATTERY_SAVER else RenderQuality.HIGH
            } else {
                quality
            }
            val animationsOn = animations && !reducedMotion
            return when (effective) {
                RenderQuality.HIGH -> Standard.copy(
                    animationsEnabled = animationsOn,
                    blurRadius = 12,
                    blurWidth = 192
                )
                RenderQuality.BALANCED -> Standard.copy(
                    animationsEnabled = animationsOn,
                    blurRadius = 8,
                    blurWidth = 128
                )
                RenderQuality.BATTERY_SAVER, RenderQuality.AUTO -> Standard.copy(
                    animationsEnabled = false,
                    blurEnabled = false
                )
            }
        }
    }
}

val LocalRenderPolicy = staticCompositionLocalOf { RenderPolicy.Standard }

val LocalVisualStyle = staticCompositionLocalOf { VisualStyle(ThemeStyle.LIQUID_GLASS, false) }
