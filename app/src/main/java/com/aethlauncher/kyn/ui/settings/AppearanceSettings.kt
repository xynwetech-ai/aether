package com.aethlauncher.kyn.ui.settings

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.aethlauncher.kyn.core.preferences.AppSettings
import com.aethlauncher.kyn.core.theme.ThemeMode
import com.aethlauncher.kyn.core.theme.ThemeStyle
import com.aethlauncher.kyn.ui.components.ChoiceChips
import com.aethlauncher.kyn.ui.components.SectionTitle
import com.aethlauncher.kyn.ui.components.SwitchRow

@Composable
fun AppearanceSettings(settings: AppSettings, viewModel: SettingsViewModel) {
    if (settings.safeMode) {
        Text(
            text = "Safe Mode aktif: warna standar sistem dipakai, pilihan tema di bawah belum berlaku.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error
        )
    }

    SectionTitle("Gaya tema")
    ChoiceChips(
        options = ThemeStyle.entries.toList(),
        selected = settings.themeStyle,
        label = ::styleLabel,
        onSelect = viewModel::setThemeStyle
    )

    SectionTitle("Mode")
    ChoiceChips(
        options = ThemeMode.entries.toList(),
        selected = settings.themeMode,
        label = ::modeLabel,
        onSelect = viewModel::setThemeMode
    )
    if (settings.themeStyle == ThemeStyle.AMOLED_DARK) {
        Text(
            text = "AMOLED Dark selalu memakai mode gelap.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    val dynamicSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    SwitchRow(
        title = "Warna dinamis (Material You)",
        description = if (dynamicSupported) {
            "Warna mengikuti wallpaper."
        } else {
            "Butuh Android 12 atau lebih baru."
        },
        checked = settings.dynamicColor && dynamicSupported,
        onCheckedChange = viewModel::setDynamicColor,
        enabled = dynamicSupported
    )
}

private fun styleLabel(style: ThemeStyle): String = when (style) {
    ThemeStyle.LIQUID_GLASS -> "Liquid Glass"
    ThemeStyle.ULTRA_MINIMAL -> "Ultra Minimal"
    ThemeStyle.AMOLED_DARK -> "AMOLED Dark"
}

private fun modeLabel(mode: ThemeMode): String = when (mode) {
    ThemeMode.SYSTEM -> "Ikuti sistem"
    ThemeMode.LIGHT -> "Terang"
    ThemeMode.DARK -> "Gelap"
}
