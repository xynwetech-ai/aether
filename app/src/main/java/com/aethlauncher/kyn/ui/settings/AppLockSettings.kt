package com.aethlauncher.kyn.ui.settings

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.aethlauncher.kyn.core.preferences.AppSettings
import com.aethlauncher.kyn.core.security.PinHasher
import com.aethlauncher.kyn.ui.components.AppSwitchList
import com.aethlauncher.kyn.ui.components.ChoiceChips
import com.aethlauncher.kyn.ui.components.HintText
import com.aethlauncher.kyn.ui.components.PinPromptDialog
import com.aethlauncher.kyn.ui.components.PinSetupDialog
import com.aethlauncher.kyn.ui.components.SettingsCard
import com.aethlauncher.kyn.ui.components.SwitchRow

private enum class PinDialogState { NONE, SETUP, VERIFY_CHANGE, VERIFY_REMOVE }

@Composable
fun AppLockSettings(state: SettingsUiState, viewModel: SettingsViewModel) {
    val settings = state.settings
    var dialog by remember { mutableStateOf(PinDialogState.NONE) }

    SettingsCard(title = "PIN") {
        HintText(
            if (settings.hasPin) "PIN sudah diatur." else "Belum ada PIN. App Lock dan Hidden Apps butuh PIN."
        )
        if (!settings.hasPin) {
            OutlinedButton(
                onClick = { dialog = PinDialogState.SETUP },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Atur PIN") }
        } else {
            OutlinedButton(
                onClick = { dialog = PinDialogState.VERIFY_CHANGE },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Ubah PIN") }
            OutlinedButton(
                onClick = { dialog = PinDialogState.VERIFY_REMOVE },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Hapus PIN") }
        }
        HintText("PIN disimpan sebagai hash PBKDF2 dengan salt acak, bukan teks asli.")
    }

    SettingsCard(title = "App Lock") {
        SwitchRow(
            title = "Aktifkan App Lock",
            description = "Minta PIN saat membuka aplikasi terlindungi lewat Aether.",
            checked = settings.appLockEnabled && settings.hasPin,
            onCheckedChange = viewModel::setAppLockEnabled,
            enabled = settings.hasPin
        )
        ChoiceChips(
            options = AppSettings.LOCK_TIMEOUTS,
            selected = settings.lockTimeoutSeconds,
            label = {
                when (it) {
                    0 -> "Selalu tanya"
                    60 -> "1 menit"
                    300 -> "5 menit"
                    else -> "$it detik"
                }
            },
            onSelect = viewModel::setLockTimeout
        )
        HintText(
            "App Lock hanya melindungi aplikasi yang dibuka lewat Aether. Ini bukan kunci tingkat sistem: " +
                "aplikasi tetap bisa dibuka dari launcher lain, notifikasi, atau Pengaturan."
        )
    }

    SettingsCard(title = "Aplikasi terlindungi") {
        if (!settings.hasPin) {
            HintText("Atur PIN dulu untuk memilih aplikasi yang dilindungi.")
        } else {
            AppSwitchList(
                apps = state.apps,
                checked = state.appPrefs.locked,
                iconLoader = viewModel.iconLoader,
                onToggle = { app, locked -> viewModel.setLocked(app.ref.key, locked) }
            )
        }
    }

    val verify: (String) -> Boolean = { pin -> PinHasher.verify(pin, settings.pinSalt, settings.pinHash) }
    when (dialog) {
        PinDialogState.NONE -> Unit
        PinDialogState.SETUP -> PinSetupDialog(
            onSave = {
                viewModel.setPin(it)
                dialog = PinDialogState.NONE
            },
            onDismiss = { dialog = PinDialogState.NONE }
        )
        PinDialogState.VERIFY_CHANGE -> PinPromptDialog(
            title = "Masukkan PIN lama",
            verify = verify,
            onSuccess = { dialog = PinDialogState.SETUP },
            onDismiss = { dialog = PinDialogState.NONE }
        )
        PinDialogState.VERIFY_REMOVE -> PinPromptDialog(
            title = "Masukkan PIN untuk menghapus",
            verify = verify,
            onSuccess = {
                viewModel.clearPin()
                dialog = PinDialogState.NONE
            },
            onDismiss = { dialog = PinDialogState.NONE }
        )
    }
}
