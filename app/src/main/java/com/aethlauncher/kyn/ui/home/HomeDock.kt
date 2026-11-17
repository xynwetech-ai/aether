package com.aethlauncher.kyn.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import com.aethlauncher.kyn.core.preferences.AppSettings
import com.aethlauncher.kyn.core.theme.aetherSurface
import com.aethlauncher.kyn.data.models.ShortcutRef
import com.aethlauncher.kyn.launcher.resolver.AppIconLoader
import com.aethlauncher.kyn.launcher.resolver.LaunchableApp

/** Posisi dan ukuran dock. X/Y dalam persen ruang kosong, Y = 0 berarti menempel di bawah. */
data class DockGeometry(val xPercent: Int, val yPercent: Int, val scalePercent: Int)

class DockDims(
    val width: Dp,
    val height: Dp,
    val padding: Dp,
    val slotWidth: Dp,
    val slotHeight: Dp,
    val iconSize: Dp
)

/** Ukuran dock dihitung dari rumus, bukan diukur, sehingga posisinya bisa dihitung sebelum digambar. */
fun dockDimensions(count: Int, scalePercent: Int, showLabels: Boolean, availableWidth: Dp): DockDims {
    val s = scalePercent / 100f
    val padding = 8.dp * s
    val slotHeight = (if (showLabels) 76.dp else 64.dp) * s
    var slotWidth = 68.dp * s
    val maxWidth = availableWidth - 24.dp
    if (slotWidth * count + padding * 2 > maxWidth) {
        slotWidth = (maxWidth - padding * 2) / count
    }
    return DockDims(
        width = slotWidth * count + padding * 2,
        height = slotHeight + padding * 2,
        padding = padding,
        slotWidth = slotWidth,
        slotHeight = slotHeight,
        iconSize = minOf(slotWidth * 0.78f, slotHeight * (if (showLabels) 0.52f else 0.74f))
    )
}

/** Dock melayang. Berada di luar pager, jadi tetap terlihat di semua halaman. */
@Composable
fun HomeDock(
    refs: List<ShortcutRef>,
    settings: AppSettings,
    geometry: DockGeometry,
    dims: DockDims,
    appsByKey: Map<String, LaunchableApp>,
    iconLoader: AppIconLoader,
    drag: HomeDragState,
    hoverSlot: Int?,
    actions: HomeActions,
    onDrop: (LaunchableApp, DragOrigin) -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val opacity = settings.dockOpacityPercent / 100f
    val items = refs.mapNotNull { appsByKey[it.key] }.take(settings.dockCount)
    val shape = RoundedCornerShape(28.dp * (geometry.scalePercent / 100f))
    val contentColor = homeContentColor()
    val latestEdit by rememberUpdatedState(onEdit)

    Row(
        modifier = modifier
            .size(width = dims.width, height = dims.height)
            .aetherSurface(shape, opacity)
            .onGloballyPositioned { drag.dockBounds = Rect(it.positionInRoot(), it.size.toSize()) }
            .pointerInput(Unit) {
                detectTapGestures(onLongPress = { latestEdit() })
            }
            .padding(dims.padding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(settings.dockCount) { index ->
            val app = items.getOrNull(index)
            Box(
                modifier = Modifier.size(width = dims.slotWidth, height = dims.slotHeight),
                contentAlignment = Alignment.Center
            ) {
                if (hoverSlot == index) {
                    Box(
                        modifier = Modifier
                            .size(dims.slotWidth * 0.92f)
                            .background(Color.White.copy(alpha = 0.16f), RoundedCornerShape(18.dp))
                            .border(1.5.dp, Color.White.copy(alpha = 0.55f), RoundedCornerShape(18.dp))
                    )
                }
                if (app != null) {
                    AppTile(
                        app = app,
                        iconSize = dims.iconSize,
                        showLabel = settings.showDockLabels,
                        labelSize = 10.sp,
                        iconLoader = iconLoader,
                        menuActions = listOf(
                            MenuAction("Info aplikasi") { actions.onAppInfo(app.ref) },
                            MenuAction("Sembunyikan") { actions.onHide(app.ref) },
                            MenuAction("Hapus dari dock") { actions.onRemoveEverywhere(app.ref) }
                        ),
                        onLaunch = actions.onLaunch,
                        drag = drag,
                        origin = DragOrigin.Dock,
                        onDrop = onDrop,
                        staggerIndex = index
                    )
                } else if (index == items.size) {
                    Box(
                        modifier = Modifier
                            .size(dims.iconSize)
                            .clip(CircleShape)
                            .background(contentColor.copy(alpha = 0.12f))
                            .clickable { actions.onOpenDockPicker() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "+", color = contentColor, fontSize = 22.sp)
                    }
                }
            }
        }
    }
}
