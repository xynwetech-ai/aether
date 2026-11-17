package com.aethlauncher.kyn.ui.settings

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aethlauncher.kyn.core.preferences.AppSettings
import com.aethlauncher.kyn.core.security.PinHasher
import com.aethlauncher.kyn.core.theme.AetherMotion
import com.aethlauncher.kyn.core.theme.LocalRenderPolicy
import com.aethlauncher.kyn.core.theme.LocalVisualStyle
import com.aethlauncher.kyn.core.theme.LocalWallpaper
import com.aethlauncher.kyn.core.theme.ThemeStyle
import com.aethlauncher.kyn.core.theme.drawCropped
import com.aethlauncher.kyn.core.theme.pressScale
import com.aethlauncher.kyn.ui.components.GlyphBadge
import com.aethlauncher.kyn.ui.components.PinPromptDialog
import kotlin.math.roundToInt

private enum class SettingsCategory(val glyph: String, val title: String, val summary: String) {
    APPEARANCE("A", "Appearance & Theme", "Tema, mode, warna dinamis, wallpaper"),
    HOME("H", "Home Screen & Widgets", "Grid, ikon, jam, halaman"),
    DOCK("D", "Dock", "Jumlah ikon, opasitas, posisi, ukuran"),
    DRAWER("L", "App Drawer", "Kolom, urutan, kategori"),
    GESTURES("G", "Gestures", "Swipe atas/bawah, ketuk dua kali"),
    SECURITY("S", "Security & Privacy", "Safe Mode, pemulihan crash, Emergency Exit"),
    APP_LOCK("K", "App Lock", "PIN dan aplikasi terlindungi"),
    HIDDEN("T", "Hidden Apps", "Sembunyikan aplikasi dari Aether"),
    PERFORMANCE("P", "Performance", "Kualitas render, animasi, cache"),
    ABOUT("I", "About Aether", "Versi, privasi, reset")
}

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val animations = LocalRenderPolicy.current.animationsEnabled
    var category by rememberSaveable { mutableStateOf<SettingsCategory?>(null) }
    var hiddenUnlocked by remember { mutableStateOf(false) }
    var askPin by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.refreshStatus() }

    // Back dari kategori kembali ke daftar kategori, bukan langsung keluar dari Pengaturan.
    BackHandler(enabled = category != null) { category = null }

    Box(modifier = modifier.fillMaxSize()) {
        SettingsBackdrop()

        Column(modifier = Modifier.fillMaxSize().systemBarsPadding()) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BackChip(onClick = { if (category != null) category = null else onBack() })
                Spacer(Modifier.width(14.dp))
                Text(
                    text = category?.title ?: "Pengaturan",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            AnimatedContent(
                targetState = category,
                modifier = Modifier.weight(1f),
                label = "settingsCategory",
                transitionSpec = {
                    val forward = targetState != null
                    val enterOffset: (Int) -> Int = { if (forward) it / 4 else -it / 4 }
                    val exitOffset: (Int) -> Int = { if (forward) -it / 4 else it / 4 }
                    (slideInHorizontally(AetherMotion.soft(animations), enterOffset) +
                        fadeIn(AetherMotion.soft(animations))) togetherWith
                        (slideOutHorizontally(AetherMotion.soft(animations), exitOffset) +
                            fadeOut(AetherMotion.soft(animations)))
                }
            ) { current ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp)
                ) {
                    when (current) {
                        null -> CategoryList(
                            onSelect = { selected ->
                                if (selected == SettingsCategory.HIDDEN && state.settings.hasPin && !hiddenUnlocked) {
                                    askPin = true
                                } else {
                                    category = selected
                                }
                            }
                        )
                        SettingsCategory.APPEARANCE -> AppearanceSettings(state, viewModel)
                        SettingsCategory.HOME -> HomeScreenSettings(state, viewModel)
                        SettingsCategory.DOCK -> DockSettings(state.settings, viewModel)
                        SettingsCategory.DRAWER -> DrawerSettings(state, viewModel)
                        SettingsCategory.GESTURES -> GestureSettings(state.settings, viewModel)
                        SettingsCategory.SECURITY -> SecuritySettings(state, viewModel)
                        SettingsCategory.APP_LOCK -> AppLockSettings(state, viewModel)
                        SettingsCategory.HIDDEN -> HiddenSettings(state, viewModel)
                        SettingsCategory.PERFORMANCE -> PerformanceSettings(state, viewModel)
                        SettingsCategory.ABOUT -> AboutSettings(viewModel)
                    }
                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }

    if (askPin) {
        val settings: AppSettings = state.settings
        PinPromptDialog(
            title = "Masukkan PIN",
            verify = { pin -> PinHasher.verify(pin, settings.pinSalt, settings.pinHash) },
            onSuccess = {
                hiddenUnlocked = true
                askPin = false
                category = SettingsCategory.HIDDEN
            },
            onDismiss = { askPin = false }
        )
    }
}

@Composable
private fun CategoryList(onSelect: (SettingsCategory) -> Unit) {
    val shape = RoundedCornerShape(20.dp)
    SettingsCategory.entries.forEach { item ->
        var pressed by remember { mutableStateOf(false) }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 5.dp)
                .pressScale(pressed, 0.97f)
                .clip(shape)
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), shape)
                .clickable { onSelect(item) }
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            GlyphBadge(item.glyph)
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = item.summary,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "\u203A",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun BackChip(onClick: () -> Unit) {
    var pressed by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .size(44.dp)
            .pressScale(pressed)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .clickable {
                pressed = false
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "\u2039",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/** Background pengaturan: warna tema dengan sentuhan wallpaper blur tipis agar terasa menyatu dengan Home. */
@Composable
private fun SettingsBackdrop() {
    val wallpaper = LocalWallpaper.current
    val visual = LocalVisualStyle.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .drawBehind {
                if (wallpaper != null && !visual.safeMode && visual.style != ThemeStyle.AMOLED_DARK) {
                    drawCropped(
                        image = wallpaper.blurred,
                        target = IntSize(size.width.roundToInt(), size.height.roundToInt()),
                        alpha = 0.22f
                    )
                }
            }
    )
}
