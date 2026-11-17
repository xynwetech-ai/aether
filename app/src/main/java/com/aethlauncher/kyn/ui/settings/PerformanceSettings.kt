package com.aethlauncher.kyn.ui.settings

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.aethlauncher.kyn.core.theme.LocalRenderPolicy
import com.aethlauncher.kyn.core.theme.RenderQuality
import com.aethlauncher.kyn.ui.components.ChoiceChips
import com.aethlauncher.kyn.ui.components.HintText
import com.aethlauncher.kyn.ui.components.InfoRow
import com.aethlauncher.kyn.ui.components.SettingsCard
import com.aethlauncher.kyn.ui.components.SwitchRow

@Composable
fun PerformanceSettings(state: SettingsUiState, viewModel: SettingsViewModel) {
    val settings = state.settings
    val policy = LocalRenderPolicy.current
    val runtime = Runtime.getRuntime()
    val usedMb = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024)
    val maxMb = runtime.maxMemory() / (1024 * 1024)

    SettingsCard(title = "Kualitas render") {
        ChoiceChips(
            options = RenderQuality.entries.toList(),
            selected = settings.renderQuality,
            label = {
                when (it) {
                    RenderQuality.AUTO -> "Otomatis"
                    RenderQuality.HIGH -> "Tinggi"
                    RenderQuality.BALANCED -> "Seimbang"
                    RenderQuality.BATTERY_SAVER -> "Hemat baterai"
                }
            },
            onSelect = viewModel::setRenderQuality
        )
        HintText(
            "Tinggi: blur lebih halus. Seimbang: blur lebih ringan. Hemat baterai: tanpa blur dan animasi. " +
                "Otomatis memilih hemat baterai pada perangkat RAM rendah."
        )
        SwitchRow(
            title = "Animasi",
            description = "Efek kenyal, transisi halaman, dan animasi panel. Otomatis mati jika animasi sistem dimatikan.",
            checked = settings.animationsEnabled,
            onCheckedChange = viewModel::setAnimations
        )
    }

    SettingsCard(title = "Status saat ini") {
        InfoRow("Blur kaca", if (policy.blurEnabled) "aktif" else "nonaktif")
        InfoRow("Animasi", if (policy.animationsEnabled) "aktif" else "nonaktif")
        InfoRow("Memori aplikasi", "$usedMb MB dari $maxMb MB")
    }

    SettingsCard(title = "Cache") {
        OutlinedButton(
            onClick = viewModel::clearIconCache,
            modifier = Modifier.fillMaxWidth()
        ) { Text("Bersihkan cache ikon") }
        HintText("Ikon dimuat ulang saat dibutuhkan. Cache dibatasi 1/16 memori aplikasi.")
    }
}
