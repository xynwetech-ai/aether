package com.aethlauncher.kyn.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.aethlauncher.kyn.launcher.manager.DefaultLauncherChecker

/** Status default launcher, diperbarui setiap layar kembali aktif. */
@Composable
fun rememberDefaultLauncherState(): State<Boolean> {
    val context = LocalContext.current
    val state = remember { mutableStateOf(false) }

    LifecycleResumeEffect(Unit) {
        state.value = DefaultLauncherChecker.isDefaultHome(context)
        onPauseOrDispose { }
    }
    return state
}
