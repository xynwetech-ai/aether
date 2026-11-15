package com.aethlauncher.kyn.ui

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.aethlauncher.kyn.core.AppContainer
import com.aethlauncher.kyn.core.preferences.AppSettings
import com.aethlauncher.kyn.core.recovery.RecoveryDecision
import com.aethlauncher.kyn.core.theme.AetherTheme
import com.aethlauncher.kyn.ui.home.AppPickerScreen
import com.aethlauncher.kyn.ui.home.HomeActions
import com.aethlauncher.kyn.ui.home.HomeScreen
import com.aethlauncher.kyn.ui.home.HomeViewModel
import com.aethlauncher.kyn.ui.recovery.RecoveryScreen
import com.aethlauncher.kyn.ui.settings.SettingsScreen
import com.aethlauncher.kyn.ui.settings.SettingsViewModel

private enum class LauncherScreen { HOME, SETTINGS, PICKER }

private const val PICKER_TARGET_DOCK = -1

@Composable
fun AetherRoot(container: AppContainer) {
    val mainViewModel: MainViewModel = viewModel(
        factory = viewModelFactory { initializer { MainViewModel(container) } }
    )
    val settings by mainViewModel.settings.collectAsStateWithLifecycle()
    val recovery by mainViewModel.recoveryState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val effective = settings ?: AppSettings()

    // UI pertama sudah tampil: startup dianggap selesai.
    LaunchedEffect(Unit) { mainViewModel.onStartupCompleted() }

    when (val decision = recovery) {
        is RecoveryDecision.Required -> AetherTheme(safeMode = true) {
            RecoveryScreen(
                decision = decision,
                onEnableSafeMode = mainViewModel::enableSafeModeAndContinue,
                onContinueNormally = mainViewModel::continueNormally,
                onEmergencyExit = { mainViewModel.openEmergencyExit(context) }
            )
        }

        RecoveryDecision.Normal -> AetherTheme(
            style = effective.themeStyle,
            mode = effective.themeMode,
            dynamicColor = effective.dynamicColor,
            safeMode = effective.safeMode
        ) {
            val loaded = settings
            if (loaded != null) {
                LauncherNavigation(
                    container = container,
                    mainViewModel = mainViewModel,
                    settings = loaded
                )
            }
        }
    }
}

@Composable
private fun LauncherNavigation(
    container: AppContainer,
    mainViewModel: MainViewModel,
    settings: AppSettings
) {
    var screen by rememberSaveable { mutableStateOf(LauncherScreen.HOME) }
    var pickerTarget by rememberSaveable { mutableIntStateOf(PICKER_TARGET_DOCK) }
    val context = LocalContext.current

    val homeViewModel: HomeViewModel = viewModel(
        factory = viewModelFactory { initializer { HomeViewModel(container) } }
    )
    val layout by homeViewModel.layout.collectAsStateWithLifecycle()
    val apps by homeViewModel.apps.collectAsStateWithLifecycle()
    val appsByKey by homeViewModel.appsByKey.collectAsStateWithLifecycle()
    val appsLoaded by homeViewModel.appsLoaded.collectAsStateWithLifecycle()

    BackHandler(enabled = screen != LauncherScreen.HOME) { screen = LauncherScreen.HOME }

    when (screen) {
        LauncherScreen.HOME -> {
            val currentLayout = layout
            if (currentLayout != null) {
                HomeScreen(
                    settings = settings,
                    layout = currentLayout,
                    appsByKey = appsByKey,
                    iconLoader = homeViewModel.iconLoader,
                    actions = HomeActions(
                        onOpenSettings = { screen = LauncherScreen.SETTINGS },
                        onAddToPage = { page ->
                            pickerTarget = page
                            screen = LauncherScreen.PICKER
                        },
                        onAddToDock = {
                            pickerTarget = PICKER_TARGET_DOCK
                            screen = LauncherScreen.PICKER
                        },
                        onMoveShortcut = homeViewModel::moveShortcut,
                        onRemoveShortcut = homeViewModel::removeShortcut,
                        onMoveDock = homeViewModel::moveDock,
                        onRemoveDock = homeViewModel::removeFromDock,
                        onEmergencyExit = { mainViewModel.openEmergencyExit(context) },
                        onRefreshApps = homeViewModel::refreshApps
                    )
                )
            }
        }

        LauncherScreen.SETTINGS -> {
            val settingsViewModel: SettingsViewModel = viewModel(
                factory = viewModelFactory { initializer { SettingsViewModel(container) } }
            )
            SettingsScreen(
                viewModel = settingsViewModel,
                onBack = { screen = LauncherScreen.HOME }
            )
        }

        LauncherScreen.PICKER -> {
            AppPickerScreen(
                title = if (pickerTarget >= 0) {
                    "Tambah ke halaman ${pickerTarget + 1}"
                } else {
                    "Tambah ke dock"
                },
                apps = apps,
                loaded = appsLoaded,
                iconLoader = homeViewModel.iconLoader,
                onPick = { app ->
                    if (pickerTarget >= 0) {
                        homeViewModel.addToPage(pickerTarget, app)
                    } else {
                        homeViewModel.addToDock(app)
                    }
                    screen = LauncherScreen.HOME
                },
                onClose = { screen = LauncherScreen.HOME }
            )
        }
    }
}
