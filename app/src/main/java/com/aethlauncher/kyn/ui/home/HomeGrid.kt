package com.aethlauncher.kyn.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import com.aethlauncher.kyn.core.preferences.AppSettings
import com.aethlauncher.kyn.core.theme.pressScale
import com.aethlauncher.kyn.core.theme.staggerIn
import com.aethlauncher.kyn.data.models.ShortcutRef
import com.aethlauncher.kyn.launcher.resolver.AppIconLoader
import com.aethlauncher.kyn.launcher.resolver.LaunchableApp
import com.aethlauncher.kyn.ui.components.AppIcon

private val MaxIconSize = AppIconLoader.ICON_DP.dp

class MenuAction(val label: String, val onClick: () -> Unit)

/**
 * Satu halaman grid. Ukuran sel dan ikon dihitung dari ruang yang tersedia, jadi
 * kombinasi baris/kolom apa pun tidak membuat ikon terpotong atau saling menimpa.
 */
@Composable
fun PageGrid(
    page: Int,
    refs: List<ShortcutRef>,
    appsByKey: Map<String, LaunchableApp>,
    settings: AppSettings,
    iconLoader: AppIconLoader,
    actions: HomeActions,
    drag: HomeDragState,
    isCurrent: Boolean,
    hoverIndex: Int?,
    onDrop: (LaunchableApp, DragOrigin) -> Unit,
    onBackgroundLongPress: () -> Unit,
    onBackgroundDoubleTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val columns = settings.gridColumns
    val rows = settings.gridRows
    val items = remember(refs, appsByKey, columns, rows) {
        refs.mapNotNull { appsByKey[it.key] }.take(columns * rows)
    }
    val latestLongPress by rememberUpdatedState(onBackgroundLongPress)
    val latestDoubleTap by rememberUpdatedState(onBackgroundDoubleTap)

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .onGloballyPositioned {
                if (isCurrent) drag.pageBounds = Rect(it.positionInRoot(), it.size.toSize())
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = { latestDoubleTap() },
                    onLongPress = { latestLongPress() }
                )
            }
    ) {
        val cellWidth = maxWidth / columns
        val cellHeight = maxHeight / rows
        val baseIcon = minOf(
            cellWidth * 0.62f,
            cellHeight * (if (settings.showLabels) 0.5f else 0.66f),
            MaxIconSize
        )
        val iconSize = minOf(baseIcon * (settings.iconScalePercent / 100f), cellWidth * 0.9f)

        if (refs.isEmpty()) {
            Text(
                text = "Halaman kosong\nTekan lama atau ketuk menu di kanan atas, lalu pilih Tambah aplikasi.",
                color = homeContentColor().copy(alpha = 0.8f),
                style = homeTextStyle(),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 32.dp)
            )
        }

        Column(modifier = Modifier.fillMaxSize()) {
            repeat(rows) { row ->
                Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    repeat(columns) { column ->
                        val cellIndex = row * columns + column
                        val app = items.getOrNull(cellIndex)
                        Box(
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                            contentAlignment = Alignment.Center
                        ) {
                            if (hoverIndex == cellIndex) HoverHighlight()
                            if (app != null) {
                                AppTile(
                                    app = app,
                                    iconSize = iconSize,
                                    showLabel = settings.showLabels,
                                    labelSize = settings.labelSizeSp.sp,
                                    iconLoader = iconLoader,
                                    menuActions = listOf(
                                        MenuAction("Info aplikasi") { actions.onAppInfo(app.ref) },
                                        MenuAction("Sembunyikan") { actions.onHide(app.ref) },
                                        MenuAction("Hapus dari Home") { actions.onRemoveEverywhere(app.ref) }
                                    ),
                                    onLaunch = actions.onLaunch,
                                    drag = drag,
                                    origin = DragOrigin.Page(page),
                                    onDrop = onDrop,
                                    staggerIndex = cellIndex
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HoverHighlight(modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(20.dp)
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(4.dp)
            .background(Color.White.copy(alpha = 0.16f), shape)
            .border(1.5.dp, Color.White.copy(alpha = 0.55f), shape)
    )
}

/**
 * Ikon + label dengan efek kenyal saat ditekan. Ketuk membuka aplikasi. Tekan lama lalu
 * geser memindahkan ikon (jika [drag] diberikan); tekan lama lalu lepas tanpa geser
 * membuka menu.
 */
@Composable
fun AppTile(
    app: LaunchableApp,
    iconSize: Dp,
    showLabel: Boolean,
    labelSize: TextUnit,
    iconLoader: AppIconLoader,
    menuActions: List<MenuAction>,
    onLaunch: (LaunchableApp) -> Unit,
    modifier: Modifier = Modifier,
    drag: HomeDragState? = null,
    origin: DragOrigin? = null,
    onDrop: ((LaunchableApp, DragOrigin) -> Unit)? = null,
    staggerIndex: Int = 0
) {
    var menuExpanded by remember { mutableStateOf(false) }
    var pressed by remember { mutableStateOf(false) }
    var coords by remember { mutableStateOf<LayoutCoordinates?>(null) }
    val latestOnLaunch by rememberUpdatedState(onLaunch)
    val latestOnDrop by rememberUpdatedState(onDrop)
    val contentColor = homeContentColor()
    val textStyle = homeTextStyle()

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 2.dp)
                .onGloballyPositioned { coords = it }
                .staggerIn(staggerIndex)
                .pressScale(pressed)
                .graphicsLayer { alpha = if (drag?.app?.ref == app.ref) 0.25f else 1f }
                .pointerInput(app.ref.key, origin) {
                    detectAppGestures(
                        onPressChange = { pressed = it },
                        onTap = { latestOnLaunch(app) },
                        onDragStart = { local ->
                            val c = coords
                            if (drag != null && origin != null && c != null && c.isAttached) {
                                drag.start(app, origin, c.localToRoot(local))
                            }
                        },
                        onDrag = { local ->
                            val c = coords
                            if (drag != null && c != null && c.isAttached) {
                                drag.move(c.localToRoot(local))
                            }
                        },
                        onDragEnd = { moved ->
                            val dropHandler = latestOnDrop
                            if (moved && drag != null && origin != null && dropHandler != null) {
                                dropHandler(app, origin)
                            }
                            drag?.finish()
                            if (!moved && menuActions.isNotEmpty()) menuExpanded = true
                        }
                    )
                },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AppIcon(ref = app.ref, size = iconSize, loader = iconLoader)
            if (showLabel) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = app.label,
                    color = contentColor,
                    fontSize = labelSize,
                    style = textStyle,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
            }
        }

        DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
            menuActions.forEach { action ->
                DropdownMenuItem(
                    text = { Text(action.label) },
                    onClick = {
                        menuExpanded = false
                        action.onClick()
                    }
                )
            }
        }
    }
}
