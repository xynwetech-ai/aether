package com.aethlauncher.kyn.core.theme

enum class ThemeStyle {
    LIQUID_GLASS,
    ULTRA_MINIMAL,
    AMOLED_DARK
}

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

/** Gaya visual yang aktif, dibaca oleh Home Screen untuk memilih background dan Dock. */
data class VisualStyle(
    val style: ThemeStyle,
    val safeMode: Boolean
)
