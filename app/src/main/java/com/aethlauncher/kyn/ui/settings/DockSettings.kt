package com.aethlauncher.kyn.ui.settings

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.aethlauncher.kyn.core.preferences.AppSettings
import com.aethlauncher.kyn.ui.components.SectionTitle
import com.aethlauncher.kyn.ui.components.StepperRow
import com.aethlauncher.kyn.ui.components.SwitchRow
import kotlin.math.roundToInt

@Composable
fun DockSettings(settings: AppSettings, viewModel: SettingsViewModel) {
    SectionTitle("Dock")
    StepperRow(
        title = "Jumlah ikon",
        value = settings.dockCount,
        range = AppSettings.MIN_DOCK..AppSettings.MAX_DOCK,
        onValueChange = viewModel::setDockCount
    )

    // Nilai slider disimpan lokal selama digeser; baru ditulis ke DataStore saat dilepas.
    var opacity by remember(settings.dockOpacityPercent) {
        mutableFloatStateOf(settings.dockOpacityPercent.toFloat())
    }
    Text(
        text = "Opasitas latar: ${opacity.roundToInt()}%",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onBackground
    )
    Slider(
        value = opacity,
        onValueChange = { opacity = it },
        onValueChangeFinished = { viewModel.setDockOpacity(opacity.roundToInt()) },
        valueRange = AppSettings.MIN_OPACITY.toFloat()..AppSettings.MAX_OPACITY.toFloat()
    )

    SwitchRow(
        title = "Tampilkan label di dock",
        checked = settings.showDockLabels,
        onCheckedChange = viewModel::setShowDockLabels
    )
}
