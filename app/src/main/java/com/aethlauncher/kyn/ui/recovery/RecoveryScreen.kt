package com.aethlauncher.kyn.ui.recovery

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aethlauncher.kyn.core.recovery.EmergencyExitResult
import com.aethlauncher.kyn.core.recovery.RecoveryDecision
import com.aethlauncher.kyn.core.recovery.RecoveryReason
import com.aethlauncher.kyn.ui.components.EmergencyExitButton

/** Layar pemulihan. Sengaja minim dependensi dan tanpa efek visual. */
@Composable
fun RecoveryScreen(
    decision: RecoveryDecision.Required,
    onEnableSafeMode: () -> Unit,
    onContinueNormally: () -> Unit,
    onEmergencyExit: () -> EmergencyExitResult,
    modifier: Modifier = Modifier
) {
    val message = when (decision.reason) {
        RecoveryReason.REPEATED_CRASHES ->
            "Aether crash ${decision.eventCount} kali dalam waktu singkat."
        RecoveryReason.SUSPICIOUS_STARTUP_LOOP ->
            "Aether gagal menyelesaikan startup ${decision.eventCount} kali berturut-turut."
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Mode Pemulihan",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Pilih Safe Mode untuk melanjutkan dengan efek visual dimatikan, " +
                    "atau keluar ke launcher lain lewat Emergency Exit.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(24.dp))
            Button(onClick = onEnableSafeMode, modifier = Modifier.fillMaxWidth()) {
                Text("Aktifkan Safe Mode dan lanjut")
            }
            Spacer(Modifier.height(12.dp))
            OutlinedButton(onClick = onContinueNormally, modifier = Modifier.fillMaxWidth()) {
                Text("Lanjut normal")
            }
            Spacer(Modifier.height(12.dp))
            EmergencyExitButton(onOpen = onEmergencyExit)
        }
    }
}
