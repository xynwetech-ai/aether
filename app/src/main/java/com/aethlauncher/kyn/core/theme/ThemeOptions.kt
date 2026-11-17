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

/** Sumber wallpaper. SYSTEM memakai wallpaper sistem apa adanya (tanpa blur asli). */
enum class WallpaperMode {
    SYSTEM,
    PRESET,
    CUSTOM
}

enum class RenderQuality {
    AUTO,
    HIGH,
    BALANCED,
    BATTERY_SAVER
}

/** Gaya visual yang aktif, dibaca oleh Home Screen untuk memilih background dan panel. */
data class VisualStyle(
    val style: ThemeStyle,
    val safeMode: Boolean
)
