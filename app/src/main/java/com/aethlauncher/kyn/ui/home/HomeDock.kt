package com.aethlauncher.kyn.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aethlauncher.kyn.core.preferences.AppSettings
import com.aethlauncher.kyn.core.theme.LocalVisualStyle
import com.aethlauncher.kyn.core.theme.ThemeStyle
import com.aethlauncher.kyn.core.theme.VisualStyle
import com.aethlauncher.kyn.data.models.ShortcutRef
import com.aethlauncher.kyn.launcher.resolver.AppIconLoader
import com.aethlauncher.kyn.launcher.resolver.LaunchableApp

/** Dock melayang. Berada di luar pager, jadi tetap terlihat di semua halaman. */
@Composable
fun HomeDock(
    refs: List<ShortcutRef>,
    settings: AppSettings,
    appsByKey: Map<String, LaunchableApp>,
    iconLoader: AppIconLoader,
    actions: HomeActions,
    modifier: Modifier = Modifier
) {
    val visual = LocalVisualStyle.current
    val shape = RoundedCornerShape(28.dp)
    val opacity = settings.dockOpacityPercent / 100f
    val items = refs.mapNotNull { appsByKey[it.key] }.take(settings.dockCount)
    val slotHeight = if (settings.showDockLabels) 76.dp else 64.dp
    val iconSize = if (settings.showDockLabels) 40.dp else 46.dp
    val contentColor = homeContentColor()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .dockSurface(visual, opacity, shape)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(settings.dockCount) { index ->
            val app = items.getOrNull(index)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(slotHeight),
                contentAlignment = Alignment.Center
            ) {
                if (app != null) {
                    AppTile(
                        app = app,
                        iconSize = iconSize,
                        showLabel = settings.showDockLabels,
                        labelSize = 10.sp,
                        iconLoader = iconLoader,
                        onMoveLeft = { actions.onMoveDock(app.ref, -1) },
                        onMoveRight = { actions.onMoveDock(app.ref, 1) },
                        onRemove = { actions.onRemoveDock(app.ref) }
                    )
                } else if (index == items.size) {
                    Box(
                        modifier = Modifier
                            .size(iconSize)
                            .clip(CircleShape)
                            .background(contentColor.copy(alpha = 0.12f))
                            .clickable { actions.onAddToDock() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "+", color = contentColor, fontSize = 22.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun Modifier.dockSurface(visual: VisualStyle, opacity: Float, shape: Shape): Modifier {
    if (visual.safeMode) {
        return this.background(MaterialTheme.colorScheme.surfaceVariant, shape)
    }
    return when (visual.style) {
        ThemeStyle.LIQUID_GLASS -> this
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color.White.copy(alpha = 0.50f * opacity),
                        Color.White.copy(alpha = 0.20f * opacity)
                    )
                ),
                shape
            )
            .border(1.dp, Color.White.copy(alpha = 0.40f * opacity), shape)

        ThemeStyle.ULTRA_MINIMAL -> this
            .background(Color.Black.copy(alpha = 0.30f * opacity), shape)

        ThemeStyle.AMOLED_DARK -> this
            .background(Color(0xFF121212).copy(alpha = opacity), shape)
            .border(1.dp, Color(0xFF2A2A2A), shape)
    }
}
