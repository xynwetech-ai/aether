package com.aethlauncher.kyn.ui

import android.os.SystemClock
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.aethlauncher.kyn.core.AppContainer
import com.aethlauncher.kyn.core.preferences.AppPrefs
import com.aethlauncher.kyn.core.preferences.AppSettings
import com.aethlauncher.kyn.core.preferences.GestureAction
import com.aethlauncher.kyn.core.recovery.RecoveryDecision
import com.aethlauncher.kyn.core.security.PinHasher
import com.aethlauncher.kyn.core.theme.AetherMotion
import com.aethlauncher.kyn.core.theme.AetherTheme
import com.aethlauncher.kyn.core.theme.LocalRenderPolicy
import com.aethlauncher.kyn.core.theme.LocalRootSize
import com.aethlauncher.kyn.launcher.manager.AppLauncher
import com.aethlauncher.kyn.launcher.manager.SystemActions
import com.aethlauncher.kyn.launcher.resolver.LaunchableApp
import com.aethlauncher.kyn.ui.components.PinPromptDialog
import com.aethlauncher.kyn.ui.drawer.DrawerScreen
import com.aethlauncher.kyn.ui.drawer.DrawerViewModel
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
    val appPrefs by mainViewModel.appPrefs.collectAsStateWithLifecycle()
    val recovery by mainViewModel.recoveryState.collectAsStateWithLifecycle()
    val wallpaper by mainViewModel.wallpaper.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val effective = settings ?: AppSettings()
    var rootSize by remember { mutableStateOf(IntSize.Zero) }

    // UI pertama sudah tampil: startup dianggap selesai.
    LaunchedEffect(Unit) { mainViewModel.onStartupCompleted() }

    Box(modifier = Modifier.fillMaxSize().onSizeChanged { rootSize = it }) {
        CompositionLocalProvider(LocalRootSize provides rootSize) {
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
                    safeMode = effective.safeMode,
                    renderQuality = effective.renderQuality,
                    animations = effective.animationsEnabled,
                    wallpaper = wallpaper
                ) {
                    val loaded = settings
                    if (loaded != null) {
                        LauncherNavigation(
                            container = container,
                            mainViewModel = mainViewModel,
                            settings = loaded,
                            appPrefs = appPrefs
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LauncherNavigation(
    container: AppContainer,
    mainViewModel: MainViewModel,
    settings: AppSettings,
    appPrefs: AppPrefs
) {
    var screen by rememberSaveable { mutableStateOf(LauncherScreen.HOME) }
    var pickerTarget by rememberSaveable { mutableIntStateOf(PICKER_TARGET_DOCK) }
    var drawerOpen by rememberSaveable { mutableStateOf(false) }
    var drawerFocusSearch by rememberSaveable { mutableStateOf(false) }
    var lastUnlock by rememberSaveable { mutableLongStateOf(0L) }
    var pendingLock by remember { mutableStateOf<LaunchableApp?>(null) }
    val context = LocalContext.current
    val animations = LocalRenderPolicy.current.animationsEnabled

    val homeViewModel: HomeViewModel = viewModel(
        factory = viewModelFactory { initializer { HomeViewModel(container) } }
    )
    val drawerViewModel: DrawerViewModel = viewModel(
        factory = viewModelFactory { initializer { DrawerViewModel(container) } }
    )
    val layout by homeViewModel.layout.collectAsStateWithLifecycle()
    val apps by homeViewModel.apps.collectAsStateWithLifecycle()
    val appsByKey by homeViewModel.appsByKey.collectAsStateWithLifecycle()
    val appsLoaded by homeViewModel.appsLoaded.collectAsStateWithLifecycle()
    val drawerState by drawerViewModel.state.collectAsStateWithLifecycle()

    val toast: (String) -> Unit = { message ->
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
    val launchNow: (LaunchableApp) -> Unit = { app ->
        if (!AppLauncher.launch(context, app.ref)) toast("Aplikasi tidak bisa dibuka")
    }
    // App Lock hanya melindungi peluncuran lewat Aether, dengan batas waktu sejak PIN terakhir benar.
    val requestLaunch: (LaunchableApp) -> Unit = { app ->
        val protectedApp = settings.appLockEnabled && settings.hasPin && app.ref.key in appPrefs.locked
        val expired = lastUnlock == 0L ||
            SystemClock.elapsedRealtime() - lastUnlock >= settings.lockTimeoutSeconds * 1000L
        if (protectedApp && expired) pendingLock = app else launchNow(app)
    }
    val closeDrawer: () -> Unit = {
        drawerOpen = false
        drawerViewModel.setQuery("")
    }
    val openDrawer: (Boolean) -> Unit = { focusSearch ->
        drawerFocusSearch = focusSearch
        drawerOpen = true
    }
    val onGesture: (GestureAction) -> Unit = { action ->
        when (action) {
            GestureAction.NONE -> Unit
            GestureAction.APP_DRAWER -> openDrawer(false)
            GestureAction.SEARCH -> openDrawer(true)
            GestureAction.SETTINGS -> screen = LauncherScreen.SETTINGS
            GestureAction.NOTIFICATIONS -> {
                if (!SystemActions.expandNotifications(context)) {
                    toast("Perangkat ini tidak mengizinkan Aether membuka panel notifikasi")
                }
            }
        }
    }
    val homePage = layout?.let { settings.defaultPage.coerceIn(0, (it.pages.size - 1).coerceAtLeast(0)) } ?: 0
    val dockVisibleCount = layout?.dock?.count { appsByKey.containsKey(it.key) } ?: 0

    BackHandler(enabled = screen != LauncherScreen.HOME) { screen = LauncherScreen.HOME }
    BackHandler(enabled = drawerOpen && screen == LauncherScreen.HOME) { closeDrawer() }

    Box(modifier = Modifier.fillMaxSize()) {
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
                            onOpenDrawer = { openDrawer(false) },
                            onOpenPicker = { page ->
                                pickerTarget = page
                                screen = LauncherScreen.PICKER
                            },
                            onOpenDockPicker = {
                                pickerTarget = PICKER_TARGET_DOCK
                                screen = LauncherScreen.PICKER
                            },
                            onLaunch = requestLaunch,
                            onDropOnPage = homeViewModel::dropOnPage,
                            onDropOnDock = homeViewModel::dropOnDock,
                            onRemoveEverywhere = homeViewModel::removeEverywhere,
                            onAppInfo = { ref -> SystemActions.openAppInfo(context, ref.packageName) },
                            onHide = homeViewModel::hide,
                            onGesture = onGesture,
                            onSetDockGeometry = mainViewModel::setDockGeometry,
                            onEmergencyExit = { mainViewModel.openEmergencyExit(context) }
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
                    title = if (pickerTarget >= 0) "Tambah ke halaman ${pickerTarget + 1}" else "Tambah ke dock",
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

        if (screen == LauncherScreen.HOME) {
            AnimatedVisibility(
                visible = drawerOpen,
                enter = slideInVertically(AetherMotion.soft(animations)) { it } + fadeIn(AetherMotion.soft(animations)),
                exit = slideOutVertically(AetherMotion.soft(animations)) { it } + fadeOut(AetherMotion.soft(animations))
            ) {
                DrawerScreen(
                    state = drawerState,
                    iconLoader = homeViewModel.iconLoader,
                    focusSearch = drawerFocusSearch,
                    onQueryChange = drawerViewModel::setQuery,
                    onSelectCategory = drawerViewModel::selectCategory,
                    onLaunch = { app ->
                        requestLaunch(app)
                        closeDrawer()
                    },
                    onAddToHome = { app ->
                        homeViewModel.addToPage(homePage, app)
                        toast("Ditambahkan ke Home")
                    },
                    onAddToDock = { app ->
                        if (dockVisibleCount >= settings.dockCount) {
                            toast("Dock penuh")
                        } else {
                            homeViewModel.addToDock(app)
                            toast("Ditambahkan ke dock")
                        }
                    },
                    onHide = drawerViewModel::hide,
                    onAppInfo = { app -> SystemActions.openAppInfo(context, app.ref.packageName) },
                    onMoveCategory = drawerViewModel::moveToCategory,
                    onWebSearch = { query -> SystemActions.webSearch(context, query) },
                    onClose = closeDrawer
                )
            }
        }
    }

    val locked = pendingLock
    if (locked != null) {
        PinPromptDialog(
            title = "Buka ${locked.label}",
            verify = { pin -> PinHasher.verify(pin, settings.pinSalt, settings.pinHash) },
            onSuccess = {
                lastUnlock = SystemClock.elapsedRealtime()
                pendingLock = null
                launchNow(locked)
            },
            onDismiss = { pendingLock = null }
        )
    }
}
