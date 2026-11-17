package com.aethlauncher.kyn.ui.settings

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.aethlauncher.kyn.BuildConfig
import com.aethlauncher.kyn.ui.components.EmergencyExitButton
import com.aethlauncher.kyn.ui.components.HintText
import com.aethlauncher.kyn.ui.components.InfoRow
import com.aethlauncher.kyn.ui.components.SettingsCard
import com.aethlauncher.kyn.ui.components.SwitchRow
import com.aethlauncher.kyn.ui.components.rememberDefaultLauncherState
import java.text.DateFormat
import java.util.Date

@Composable
fun SecuritySettings(state: SettingsUiState, viewModel: SettingsViewModel) {
    val isDefault by rememberDefaultLauncherState()
    val context = LocalContext.current

    SettingsCard(title = "Safe Mode") {
        SwitchRow(
            title = "Safe Mode",
            description = "Matikan animasi, blur, wallpaper, warna dinamis, komponen eksperimental, " +
                "dan widget nonesensial.",
            checked = state.settings.safeMode,
            onCheckedChange = viewModel::setSafeMode
        )
    }

    SettingsCard(title = "Crash Recovery") {
        val crash = state.crashStatus
        val lastCrash = crash.lastCrash
        InfoRow("Crash tercatat", crash.confirmedCrashCount.toString())
        InfoRow("Startup tidak selesai", crash.unfinishedStartupCount.toString())
        InfoRow(
            "Crash terakhir",
            if (lastCrash == null) {
                "belum ada"
            } else {
                DateFormat.getDateTimeInstance().format(Date(lastCrash.timestampMs)) +
                    " - " + lastCrash.summary
            }
        )
        HintText(
            "Hanya crash yang tertangkap yang tercatat. ANR, native crash, dan penghentian proses " +
                "oleh sistem tidak selalu terdeteksi."
        )
        OutlinedButton(
            onClick = viewModel::clearCrashHistory,
            modifier = Modifier.fillMaxWidth()
        ) { Text("Hapus riwayat crash") }
    }

    SettingsCard(title = "Launcher") {
        InfoRow("Status", if (isDefault) "Default launcher aktif" else "Sandbox (bukan default)")
        Spacer(Modifier.height(8.dp))
        EmergencyExitButton(onOpen = { viewModel.openEmergencyExit(context) })
        HintText("Emergency Exit membuka pengaturan Android untuk memilih launcher lain.")
    }

    if (BuildConfig.DEBUG) {
        SettingsCard(title = "Debug (hanya build debug)") {
            OutlinedButton(
                onClick = viewModel::simulateCrashLoop,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Simulasi crash berulang") }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = { throw RuntimeException("Debug crash") },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Crash sekarang") }
        }
    }
}
