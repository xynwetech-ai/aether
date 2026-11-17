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
import com.aethlauncher.kyn.BuildConfig
import com.aethlauncher.kyn.ui.components.ConfirmDialog
import com.aethlauncher.kyn.ui.components.HintText
import com.aethlauncher.kyn.ui.components.InfoRow
import com.aethlauncher.kyn.ui.components.SettingsCard

@Composable
fun AboutSettings(viewModel: SettingsViewModel) {
    var confirmReset by remember { mutableStateOf(false) }

    SettingsCard(title = "Aether Launcher") {
        InfoRow("Versi", BuildConfig.VERSION_NAME)
        InfoRow("Build", BuildConfig.VERSION_CODE.toString() + if (BuildConfig.DEBUG) " (debug)" else "")
        InfoRow("Package", BuildConfig.APPLICATION_ID)
        InfoRow("Pengembang", "Kyanthropic")
        HintText("Refined. Adaptive. Reliable.")
    }

    SettingsCard(title = "Privasi") {
        HintText(
            "Aether tidak memakai internet dan tidak mengirim data apa pun. Semua pengaturan, susunan Home, " +
                "dan preferensi aplikasi disimpan lokal di perangkat. Izin yang diminta hanya untuk membuka panel notifikasi."
        )
    }

    SettingsCard(title = "Reset") {
        OutlinedButton(
            onClick = { confirmReset = true },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Reset semua pengaturan") }
        HintText("Mengembalikan pengaturan, susunan Home, kategori, dan daftar aplikasi tersembunyi ke default.")
    }

    if (confirmReset) {
        ConfirmDialog(
            title = "Reset semua pengaturan?",
            message = "Susunan Home, kategori kustom, aplikasi tersembunyi/terlindungi, dan PIN akan dihapus. Tindakan ini tidak bisa dibatalkan.",
            confirmLabel = "Reset",
            onConfirm = {
                viewModel.resetAll()
                confirmReset = false
            },
            onDismiss = { confirmReset = false }
        )
    }
}
