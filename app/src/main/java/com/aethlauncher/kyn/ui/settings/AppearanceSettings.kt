package com.aethlauncher.kyn.ui.settings

import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.aethlauncher.kyn.core.theme.ThemeMode
import com.aethlauncher.kyn.core.theme.ThemeStyle
import com.aethlauncher.kyn.core.theme.WallpaperMode
import com.aethlauncher.kyn.core.wallpaper.PresetWallpapers
import com.aethlauncher.kyn.ui.components.ChoiceChips
import com.aethlauncher.kyn.ui.components.HintText
import com.aethlauncher.kyn.ui.components.SettingsCard
import com.aethlauncher.kyn.ui.components.SwitchRow

@Composable
fun AppearanceSettings(state: SettingsUiState, viewModel: SettingsViewModel) {
    val settings = state.settings
    val context = LocalContext.current

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            viewModel.importWallpaper(uri) { ok ->
                Toast.makeText(
                    context,
                    if (ok) "Wallpaper diterapkan" else "Gambar tidak bisa dibaca",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    if (settings.safeMode) {
        HintText("Safe Mode aktif: warna standar sistem dipakai dan pilihan tema belum berlaku.")
    }

    SettingsCard(title = "Gaya tema") {
        ChoiceChips(
            options = ThemeStyle.entries.toList(),
            selected = settings.themeStyle,
            label = ::styleLabel,
            onSelect = viewModel::setThemeStyle
        )
        ChoiceChips(
            options = ThemeMode.entries.toList(),
            selected = settings.themeMode,
            label = ::modeLabel,
            onSelect = viewModel::setThemeMode
        )
        if (settings.themeStyle == ThemeStyle.AMOLED_DARK) {
            HintText("AMOLED Dark selalu memakai mode gelap dan background hitam murni.")
        }
        val dynamicSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
        SwitchRow(
            title = "Warna dinamis (Material You)",
            description = if (dynamicSupported) "Warna mengikuti wallpaper." else "Butuh Android 12 atau lebih baru.",
            checked = settings.dynamicColor && dynamicSupported,
            onCheckedChange = viewModel::setDynamicColor,
            enabled = dynamicSupported
        )
    }

    SettingsCard(title = "Wallpaper") {
        val modeText = when (settings.wallpaperMode) {
            WallpaperMode.SYSTEM -> "Saat ini: wallpaper sistem (tanpa blur asli)"
            WallpaperMode.PRESET -> "Saat ini: ${PresetWallpapers.names.getOrElse(settings.wallpaperPreset) { "Aurora" }}"
            WallpaperMode.CUSTOM -> "Saat ini: gambar sendiri"
        }
        Text(
            text = modeText,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        ChoiceChips(
            options = PresetWallpapers.names.indices.toList(),
            selected = if (settings.wallpaperMode == WallpaperMode.PRESET) settings.wallpaperPreset else -1,
            label = { PresetWallpapers.names[it] },
            onSelect = viewModel::setWallpaperPreset
        )
        OutlinedButton(
            onClick = { picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        ) { Text("Pilih gambar sendiri") }
        OutlinedButton(
            onClick = {
                viewModel.copySystemWallpaper { ok ->
                    Toast.makeText(
                        context,
                        if (ok) "Wallpaper sistem disalin" else "Android tidak mengizinkan membaca wallpaper sistem. Pilih gambar sendiri.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Coba salin wallpaper sistem") }
        OutlinedButton(
            onClick = viewModel::useSystemWallpaper,
            modifier = Modifier.fillMaxWidth()
        ) { Text("Pakai wallpaper sistem (tanpa blur asli)") }
        HintText(
            "Efek liquid glass asli memakai wallpaper Aether karena Android tidak memberi aplikasi " +
                "akses untuk mem-blur wallpaper sistem."
        )
    }
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
