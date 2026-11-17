package com.aethlauncher.kyn.ui.settings

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.aethlauncher.kyn.core.preferences.AppSettings
import com.aethlauncher.kyn.core.preferences.GestureAction
import com.aethlauncher.kyn.ui.components.ChoiceChips
import com.aethlauncher.kyn.ui.components.HintText
import com.aethlauncher.kyn.ui.components.SettingsCard

@Composable
fun GestureSettings(settings: AppSettings, viewModel: SettingsViewModel) {
    SettingsCard(title = "Swipe ke atas") {
        GestureChips(settings.swipeUpAction, viewModel::setSwipeUp)
    }
    SettingsCard(title = "Swipe ke bawah") {
        GestureChips(settings.swipeDownAction, viewModel::setSwipeDown)
    }
    SettingsCard(title = "Ketuk dua kali di area kosong") {
        GestureChips(settings.doubleTapAction, viewModel::setDoubleTap)
    }
    SettingsCard(title = "Catatan") {
        HintText("Tekan lama di area kosong selalu membuka Menu Home dan tidak bisa diubah.")
        HintText(
            "Panel notifikasi dibuka lewat metode tidak resmi dan bisa gagal di sebagian perangkat. " +
                "Jika gagal, Aether menampilkan pesan penjelasan."
        )
        Text(
            text = "Buka Quick Settings dan kunci layar tidak tersedia untuk launcher biasa.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun GestureChips(selected: GestureAction, onSelect: (GestureAction) -> Unit) {
    ChoiceChips(
        options = GestureAction.entries.toList(),
        selected = selected,
        label = {
            when (it) {
                GestureAction.NONE -> "Tidak ada"
                GestureAction.APP_DRAWER -> "App Drawer"
                GestureAction.NOTIFICATIONS -> "Notifikasi"
                GestureAction.SEARCH -> "Cari"
                GestureAction.SETTINGS -> "Pengaturan"
            }
        },
        onSelect = onSelect
    )
}
