package com.aethlauncher.kyn.core.theme

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

/**
 * Tema Aether di atas Material 3.
 *
 * Safe Mode memakai warna standar sistem: tanpa dynamic color, tanpa wallpaper, tanpa efek.
 * AMOLED Dark selalu gelap dengan background hitam murni.
 */
@Composable
fun AetherTheme(
    style: ThemeStyle = ThemeStyle.LIQUID_GLASS,
    mode: ThemeMode = ThemeMode.SYSTEM,
    dynamicColor: Boolean = true,
    safeMode: Boolean = false,
    renderQuality: RenderQuality = RenderQuality.AUTO,
    animations: Boolean = true,
    wallpaper: WallpaperLayers? = null,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val systemDark = isSystemInDarkTheme()
    val reducedMotion = remember { isReducedMotion(context) }
    val lowRam = remember { isLowRam(context) }
    val policy = RenderPolicy.resolve(safeMode, renderQuality, animations, reducedMotion, lowRam)

    val dark = when {
        safeMode -> systemDark
        style == ThemeStyle.AMOLED_DARK -> true
        mode == ThemeMode.DARK -> true
        mode == ThemeMode.LIGHT -> false
        else -> systemDark
    }

    val useDynamic = policy.dynamicColorEnabled && dynamicColor &&
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    var colorScheme: ColorScheme = when {
        useDynamic -> if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        dark -> darkColorScheme()
        else -> lightColorScheme()
    }

    if (!safeMode && style == ThemeStyle.AMOLED_DARK) {
        colorScheme = colorScheme.copy(
            background = Color.Black,
            surface = Color.Black,
            surfaceVariant = Color(0xFF1A1A1A)
        )
    }

    CompositionLocalProvider(
        LocalRenderPolicy provides policy,
        LocalVisualStyle provides VisualStyle(style, safeMode),
        LocalWallpaper provides (if (safeMode) null else wallpaper)
    ) {
        MaterialTheme(colorScheme = colorScheme, content = content)
    }
}

private fun isReducedMotion(context: Context): Boolean =
    Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f

private fun isLowRam(context: Context): Boolean =
    (context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager)?.isLowRamDevice == true
