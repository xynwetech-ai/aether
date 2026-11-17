package com.aethlauncher.kyn.ui.settings

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aethlauncher.kyn.core.preferences.AppSettings
import com.aethlauncher.kyn.core.preferences.CustomCategory
import com.aethlauncher.kyn.core.preferences.DrawerSort
import com.aethlauncher.kyn.ui.components.ChoiceChips
import com.aethlauncher.kyn.ui.components.ConfirmDialog
import com.aethlauncher.kyn.ui.components.HintText
import com.aethlauncher.kyn.ui.components.NameDialog
import com.aethlauncher.kyn.ui.components.SettingsCard
import com.aethlauncher.kyn.ui.components.StepperRow

@Composable
fun DrawerSettings(state: SettingsUiState, viewModel: SettingsViewModel) {
    val settings = state.settings
    var adding by remember { mutableStateOf(false) }
    var renaming by remember { mutableStateOf<CustomCategory?>(null) }
    var deleting by remember { mutableStateOf<CustomCategory?>(null) }

    SettingsCard(title = "Tampilan") {
        StepperRow(
            title = "Kolom",
            value = settings.drawerColumns,
            range = AppSettings.MIN_DRAWER_COLUMNS..AppSettings.MAX_DRAWER_COLUMNS,
            onValueChange = viewModel::setDrawerColumns
        )
        ChoiceChips(
            options = DrawerSort.entries.toList(),
            selected = settings.drawerSort,
            label = {
                when (it) {
                    DrawerSort.ALPHABETICAL -> "A-Z"
                    DrawerSort.RECENTLY_INSTALLED -> "Terbaru dipasang"
                }
            },
            onSelect = viewModel::setDrawerSort
        )
        HintText("Buka App Drawer dengan swipe ke atas di Home. Tekan lama aplikasi untuk menu.")
    }

    SettingsCard(title = "Kategori kustom") {
        if (state.appPrefs.customCategories.isEmpty()) {
            HintText("Belum ada kategori kustom. Kategori otomatis (Komunikasi, Game, dan lain-lain) tetap aktif.")
        }
        state.appPrefs.customCategories.forEach { category ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = { renaming = category }) { Text("Ubah") }
                TextButton(onClick = { deleting = category }) { Text("Hapus") }
            }
        }
        OutlinedButton(
            onClick = { adding = true },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        ) { Text("Tambah kategori") }
        HintText("Pindahkan aplikasi ke kategori lewat tekan lama aplikasi di App Drawer. Pilihan manual mengalahkan kategori otomatis.")
    }

    if (adding) {
        NameDialog(
            title = "Kategori baru",
            initial = "",
            onConfirm = {
                viewModel.addCategory(it)
                adding = false
            },
            onDismiss = { adding = false }
        )
    }
    val toRename = renaming
    if (toRename != null) {
        NameDialog(
            title = "Ubah nama kategori",
            initial = toRename.name,
            onConfirm = {
                viewModel.renameCategory(toRename.id, it)
                renaming = null
            },
            onDismiss = { renaming = null }
        )
    }
    val toDelete = deleting
    if (toDelete != null) {
        ConfirmDialog(
            title = "Hapus kategori?",
            message = "Aplikasi di \"${toDelete.name}\" kembali ke kategori otomatis.",
            confirmLabel = "Hapus",
            onConfirm = {
                viewModel.deleteCategory(toDelete.id)
                deleting = null
            },
            onDismiss = { deleting = null }
        )
    }
}
