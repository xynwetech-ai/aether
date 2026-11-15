package com.aethlauncher.kyn.ui.settings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

private enum class SettingsCategory(val title: String, val summary: String) {
    APPEARANCE("Appearance & Theme", "Gaya tema, mode terang/gelap, warna dinamis"),
    HOME("Home Screen & Widgets", "Grid, halaman, label, jam"),
    DOCK("Dock", "Jumlah ikon, opasitas, label"),
    SECURITY("Security & Privacy", "Safe Mode, pemulihan crash, Emergency Exit")
}

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var category by rememberSaveable { mutableStateOf<SettingsCategory?>(null) }

    LaunchedEffect(Unit) { viewModel.refreshStatus() }

    // Back dari kategori kembali ke daftar kategori, bukan langsung keluar dari Pengaturan.
    BackHandler(enabled = category != null) { category = null }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = { if (category != null) category = null else onBack() }) {
                    Text("Kembali")
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = category?.title ?: "Pengaturan",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(Modifier.height(8.dp))

            when (category) {
                null -> CategoryList(onSelect = { category = it })
                SettingsCategory.APPEARANCE -> AppearanceSettings(state.settings, viewModel)
                SettingsCategory.HOME -> HomeScreenSettings(state, viewModel)
                SettingsCategory.DOCK -> DockSettings(state.settings, viewModel)
                SettingsCategory.SECURITY -> SecuritySettings(state, viewModel)
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun CategoryList(onSelect: (SettingsCategory) -> Unit) {
    SettingsCategory.entries.forEach { item ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onSelect(item) }
                .padding(vertical = 14.dp)
        ) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = item.summary,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        HorizontalDivider()
    }
}

