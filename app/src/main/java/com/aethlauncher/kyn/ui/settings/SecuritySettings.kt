package com.aethlauncher.kyn.ui.settings

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.aethlauncher.kyn.BuildConfig
import com.aethlauncher.kyn.ui.components.EmergencyExitButton
import com.aethlauncher.kyn.ui.components.InfoRow
import com.aethlauncher.kyn.ui.components.SectionTitle
import com.aethlauncher.kyn.ui.components.SwitchRow
import com.aethlauncher.kyn.ui.components.rememberDefaultLauncherState
import java.text.DateFormat
import java.util.Date

@Composable
fun SecuritySettings(state: SettingsUiState, viewModel: SettingsViewModel) {
    val isDefault by rememberDefaultLauncherState()
    val context = LocalContext.current

    SwitchRow(
        title = "Safe Mode",
        description = "Matikan animasi, blur, warna dinamis, komponen eksperimental, " +
            "dan widget nonesensial.",
        checked = state.settings.safeMode,
        onCheckedChange = viewModel::setSafeMode
    )

    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

    Text(
        text = "Crash Recovery",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onBackground
    )
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
    Text(
        text = "Hanya crash yang tertangkap yang tercatat. ANR, native crash, dan " +
            "penghentian proses oleh sistem tidak selalu terdeteksi.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(Modifier.height(8.dp))
    OutlinedButton(
        onClick = viewModel::clearCrashHistory,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Hapus riwayat crash")
    }

    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

    Text(
        text = "Launcher",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onBackground
    )
    InfoRow("Status", if (isDefault) "Default launcher aktif" else "Sandbox (bukan default)")
    InfoRow("Versi", BuildConfig.VERSION_NAME)
    Spacer(Modifier.height(8.dp))
    EmergencyExitButton(onOpen = { viewModel.openEmergencyExit(context) })

    if (BuildConfig.DEBUG) {
        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
        SectionTitle("Debug (hanya build debug)")
        OutlinedButton(
            onClick = viewModel::simulateCrashLoop,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Simulasi crash berulang")
        }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(
            onClick = { throw RuntimeException("Debug crash") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Crash sekarang")
        }
    }
}
