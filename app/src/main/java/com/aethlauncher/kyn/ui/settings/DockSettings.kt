package com.aethlauncher.kyn.ui.settings

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aethlauncher.kyn.core.preferences.AppSettings
import com.aethlauncher.kyn.ui.components.HintText
import com.aethlauncher.kyn.ui.components.SettingsCard
import com.aethlauncher.kyn.ui.components.SliderRow
import com.aethlauncher.kyn.ui.components.StepperRow
import com.aethlauncher.kyn.ui.components.SwitchRow

@Composable
fun DockSettings(settings: AppSettings, viewModel: SettingsViewModel) {
    SettingsCard(title = "Tampilan dock") {
        StepperRow(
            title = "Jumlah ikon",
            value = settings.dockCount,
            range = AppSettings.MIN_DOCK..AppSettings.MAX_DOCK,
            onValueChange = viewModel::setDockCount
        )
        SliderRow(
            title = "Opasitas latar",
            value = settings.dockOpacityPercent,
            range = AppSettings.MIN_OPACITY..AppSettings.MAX_OPACITY,
            onFinished = viewModel::setDockOpacity
        )
        SwitchRow(
            title = "Tampilkan label di dock",
            checked = settings.showDockLabels,
            onCheckedChange = viewModel::setShowDockLabels
        )
    }

    SettingsCard(title = "Posisi dan ukuran") {
        SliderRow("Posisi X", settings.dockXPercent, 0..100, viewModel::setDockX)
        SliderRow("Posisi Y", settings.dockYPercent, 0..100, viewModel::setDockY)
        SliderRow(
            title = "Ukuran",
            value = settings.dockScalePercent,
            range = AppSettings.MIN_SCALE..AppSettings.MAX_SCALE,
            onFinished = viewModel::setDockScale
        )
        OutlinedButton(
            onClick = viewModel::resetDockGeometry,
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
        ) { Text("Reset posisi dan ukuran") }
        HintText("Di Home, tekan lama latar dock untuk mengatur posisi dan ukurannya langsung lewat popup.")
    }
}
