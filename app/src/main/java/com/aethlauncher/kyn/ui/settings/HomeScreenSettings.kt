package com.aethlauncher.kyn.ui.settings

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aethlauncher.kyn.core.preferences.AppSettings
import com.aethlauncher.kyn.domain.usecase.HomeLayoutOps
import com.aethlauncher.kyn.ui.components.ChoiceChips
import com.aethlauncher.kyn.ui.components.SectionTitle
import com.aethlauncher.kyn.ui.components.StepperRow
import com.aethlauncher.kyn.ui.components.SwitchRow

@Composable
fun HomeScreenSettings(state: SettingsUiState, viewModel: SettingsViewModel) {
    val settings = state.settings
    val pages = state.layout.pages

    SectionTitle("Grid (kolom x baris)")
    ChoiceChips(
        options = AppSettings.GRID_PRESETS,
        selected = settings.gridColumns to settings.gridRows,
        label = { "${it.first} x ${it.second}" },
        onSelect = { viewModel.setGrid(it.first, it.second) }
    )
    StepperRow(
        title = "Kolom",
        value = settings.gridColumns,
        range = AppSettings.MIN_COLUMNS..AppSettings.MAX_COLUMNS,
        onValueChange = viewModel::setGridColumns
    )
    StepperRow(
        title = "Baris",
        value = settings.gridRows,
        range = AppSettings.MIN_ROWS..AppSettings.MAX_ROWS,
        onValueChange = viewModel::setGridRows
    )
    SwitchRow(
        title = "Tampilkan label aplikasi",
        checked = settings.showLabels,
        onCheckedChange = viewModel::setShowLabels
    )

    SectionTitle("Jam")
    SwitchRow(
        title = "Tampilkan jam",
        description = "Jam dinonaktifkan otomatis di Safe Mode.",
        checked = settings.showClock,
        onCheckedChange = viewModel::setShowClock
    )
    SwitchRow(
        title = "Tampilkan tanggal",
        checked = settings.showDate,
        onCheckedChange = viewModel::setShowDate,
        enabled = settings.showClock
    )

    SectionTitle("Halaman")
    pages.forEachIndexed { index, page ->
        val suffix = if (index == settings.defaultPage) " (default)" else ""
        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
            Text(
                text = "Halaman ${index + 1}$suffix - ${page.size} aplikasi",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                TextButton(
                    onClick = { viewModel.setDefaultPage(index) },
                    enabled = index != settings.defaultPage
                ) { Text("Jadikan default") }
                TextButton(
                    onClick = { viewModel.movePage(index, -1) },
                    enabled = index > 0
                ) { Text("Geser kiri") }
                TextButton(
                    onClick = { viewModel.movePage(index, 1) },
                    enabled = index < pages.lastIndex
                ) { Text("Geser kanan") }
                TextButton(
                    onClick = { viewModel.removePage(index) },
                    enabled = pages.size > 1
                ) { Text("Hapus") }
            }
        }
    }
    Text(
        text = "Menghapus halaman juga menghapus susunan ikon di halaman itu.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    OutlinedButton(
        onClick = viewModel::addPage,
        enabled = pages.size < HomeLayoutOps.MAX_PAGES,
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
    ) {
        Text("Tambah halaman")
    }
}
