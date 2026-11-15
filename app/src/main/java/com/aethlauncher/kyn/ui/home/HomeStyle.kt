package com.aethlauncher.kyn.ui.home

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import com.aethlauncher.kyn.core.theme.LocalVisualStyle

/** Warna teks di atas wallpaper. Safe Mode memakai warna tema standar. */
@Composable
fun homeContentColor(): Color {
    val visual = LocalVisualStyle.current
    return if (visual.safeMode) MaterialTheme.colorScheme.onBackground else Color.White
}

/** Bayangan tipis supaya teks tetap terbaca di atas wallpaper terang. */
@Composable
fun homeTextStyle(): TextStyle {
    val visual = LocalVisualStyle.current
    return if (visual.safeMode) {
        TextStyle.Default
    } else {
        TextStyle(
            shadow = Shadow(
                color = Color.Black.copy(alpha = 0.55f),
                offset = Offset(0f, 2f),
                blurRadius = 6f
            )
        )
    }
}
