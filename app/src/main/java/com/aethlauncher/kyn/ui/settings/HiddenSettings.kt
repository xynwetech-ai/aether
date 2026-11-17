package com.aethlauncher.kyn.ui.settings

import androidx.compose.runtime.Composable
import com.aethlauncher.kyn.ui.components.AppSwitchList
import com.aethlauncher.kyn.ui.components.HintText
import com.aethlauncher.kyn.ui.components.SettingsCard

@Composable
fun HiddenSettings(state: SettingsUiState, viewModel: SettingsViewModel) {
    SettingsCard(title = "Sembunyikan aplikasi") {
        HintText(
            "Aplikasi tersembunyi hilang dari Home, App Drawer, dan pencarian Aether. " +
                "Aplikasinya tetap terpasang dan tetap terlihat di Pengaturan Android serta launcher lain."
        )
        AppSwitchList(
            apps = state.apps,
            checked = state.appPrefs.hidden,
            iconLoader = viewModel.iconLoader,
            onToggle = { app, hidden -> viewModel.setHidden(app.ref.key, hidden) }
        )
    }
}
