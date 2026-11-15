package com.aethlauncher.kyn.core.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

/**
 * Tema Aether di atas Material 3.
 *
 * Safe Mode memakai warna standar sistem: tanpa dynamic color dan tanpa gaya khusus.
 * AMOLED Dark selalu gelap dengan background hitam murni.
 */
@Composable
fun AetherTheme(
    style: ThemeStyle = ThemeStyle.LIQUID_GLASS,
    mode: ThemeMode = ThemeMode.SYSTEM,
    dynamicColor: Boolean = true,
    safeMode: Boolean = false,
    content: @Composable () -> Unit
) {
    val policy = RenderPolicy.forSafeMode(safeMode)
    val context = LocalContext.current
    val systemDark = isSystemInDarkTheme()

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
        LocalVisualStyle provides VisualStyle(style, safeMode)
    ) {
        MaterialTheme(colorScheme = colorScheme, content = content)
    }
}
