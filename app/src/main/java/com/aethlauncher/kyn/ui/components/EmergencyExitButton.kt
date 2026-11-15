package com.aethlauncher.kyn.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aethlauncher.kyn.core.recovery.EmergencyExitResult
import com.aethlauncher.kyn.core.recovery.EmergencyRecoveryController

/** Tombol Emergency Exit. Jika semua rute sistem gagal, tampilkan instruksi manual. */
@Composable
fun EmergencyExitButton(
    onOpen: () -> EmergencyExitResult,
    modifier: Modifier = Modifier
) {
    var failed by rememberSaveable { mutableStateOf(false) }

    Column(modifier = modifier) {
        OutlinedButton(
            onClick = { failed = onOpen() is EmergencyExitResult.Failed },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Emergency Exit")
        }
        if (failed) {
            Text(
                text = EmergencyRecoveryController.MANUAL_INSTRUCTIONS,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}
