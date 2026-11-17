package com.aethlauncher.kyn.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aethlauncher.kyn.core.preferences.AppSettings
import com.aethlauncher.kyn.core.preferences.GestureAction
import com.aethlauncher.kyn.core.recovery.EmergencyExitResult
import com.aethlauncher.kyn.core.theme.AetherMotion
import com.aethlauncher.kyn.core.theme.LocalRenderPolicy
import com.aethlauncher.kyn.core.theme.LocalRootSize
import com.aethlauncher.kyn.core.theme.LocalVisualStyle
import com.aethlauncher.kyn.core.theme.ThemeStyle
import com.aethlauncher.kyn.core.theme.VisualStyle
import com.aethlauncher.kyn.core.theme.WallpaperBackground
import com.aethlauncher.kyn.core.theme.aetherSurface
import com.aethlauncher.kyn.data.models.HomeLayout
import com.aethlauncher.kyn.data.models.ShortcutRef
import com.aethlauncher.kyn.launcher.resolver.AppIconLoader
import com.aethlauncher.kyn.launcher.resolver.LaunchableApp
import com.aethlauncher.kyn.ui.components.AppIcon
import com.aethlauncher.kyn.ui.components.EmergencyExitButton
import com.aethlauncher.kyn.ui.widgets.ClockWidget
import kotlin.math.absoluteValue
import kotlin.math.roundToInt
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

/** Semua aksi yang bisa dipicu dari Home Screen. */
class HomeActions(
    val onOpenSettings: () -> Unit,
    val onOpenDrawer: () -> Unit,
    val onOpenPicker: (page: Int) -> Unit,
    val onOpenDockPicker: () -> Unit,
    val onLaunch: (LaunchableApp) -> Unit,
    val onDropOnPage: (ref: ShortcutRef, page: Int, visibleIndex: Int) -> Unit,
    val onDropOnDock: (ref: ShortcutRef, visibleIndex: Int, capacity: Int) -> Unit,
    val onRemoveEverywhere: (ref: ShortcutRef) -> Unit,
    val onAppInfo: (ref: ShortcutRef) -> Unit,
    val onHide: (ref: ShortcutRef) -> Unit,
    val onGesture: (GestureAction) -> Unit,
    val onSetDockGeometry: (x: Int, y: Int, scale: Int) -> Unit,
    val onEmergencyExit: () -> EmergencyExitResult
)

@Composable
fun HomeScreen(
    settings: AppSettings,
    layout: HomeLayout,
    appsByKey: Map<String, LaunchableApp>,
    iconLoader: AppIconLoader,
    actions: HomeActions,
    modifier: Modifier = Modifier
) {
    val visual = LocalVisualStyle.current
    val policy = LocalRenderPolicy.current
    val rootSize = LocalRootSize.current
    val contentColor = homeContentColor()
    val pageCount = layout.pages.size
    val pagerState = rememberPagerState(
        initialPage = settings.defaultPage.coerceIn(0, maxOf(0, pageCount - 1))
    ) { pageCount }

    val drag = remember { HomeDragState() }
    var menuOpen by remember { mutableStateOf(false) }
    var editingDock by remember { mutableStateOf(false) }
    var dockDraft by remember { mutableStateOf<DockGeometry?>(null) }
    val geometry = dockDraft
        ?: DockGeometry(settings.dockXPercent, settings.dockYPercent, settings.dockScalePercent)

    val currentPage = pagerState.currentPage
    val removeZoneBottom = rootSize.height * 0.09f
    val latestActions by rememberUpdatedState(actions)
    val latestSettings by rememberUpdatedState(settings)

    // derivedStateOf: HomeScreen hanya recompose saat target berubah, bukan tiap gerakan jari.
    val hover by remember(currentPage, settings.gridColumns, settings.gridRows, settings.dockCount, removeZoneBottom) {
        derivedStateOf {
            resolveHover(
                drag, currentPage, settings.gridColumns, settings.gridRows,
                settings.dockCount, removeZoneBottom
            )
        }
    }

    val handleDrop: (LaunchableApp, DragOrigin) -> Unit = { app, _ ->
        val target = resolveHover(
            drag, currentPage, settings.gridColumns, settings.gridRows,
            settings.dockCount, removeZoneBottom
        )
        when (target) {
            is HoverTarget.PageCell -> actions.onDropOnPage(app.ref, target.page, target.index)
            is HoverTarget.DockSlot -> actions.onDropOnDock(app.ref, target.index, settings.dockCount)
            HoverTarget.Remove -> actions.onRemoveEverywhere(app.ref)
            null -> Unit
        }
    }

    // Tahan ikon di tepi kiri/kanan untuk pindah halaman saat menyeret.
    val dragging = drag.active
    LaunchedEffect(dragging) {
        if (!dragging) return@LaunchedEffect
        var edgeSince = 0L
        while (isActive) {
            val width = rootSize.width.toFloat()
            val x = drag.pointer.x
            val direction = when {
                x < width * 0.07f -> -1
                x > width * 0.93f -> 1
                else -> 0
            }
            val now = android.os.SystemClock.uptimeMillis()
            if (direction == 0) {
                edgeSince = 0L
            } else if (edgeSince == 0L) {
                edgeSince = now
            } else if (now - edgeSince > 550L) {
                val target = (pagerState.currentPage + direction).coerceIn(0, pageCount - 1)
                if (target != pagerState.currentPage) pagerState.animateScrollToPage(target)
                edgeSince = android.os.SystemClock.uptimeMillis() + 250L
            }
            delay(60)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        HomeBackdrop(visual)

        BoxWithConstraints(modifier = Modifier.fillMaxSize().systemBarsPadding()) {
            val dims = dockDimensions(
                settings.dockCount, geometry.scalePercent, settings.showDockLabels, maxWidth
            )
            val freeX = (maxWidth - dims.width).coerceAtLeast(0.dp)
            val freeY = (maxHeight - dims.height).coerceAtLeast(0.dp)
            val dockX by animateDpAsState(
                targetValue = freeX * (geometry.xPercent / 100f),
                animationSpec = AetherMotion.soft(policy.animationsEnabled),
                label = "dockX"
            )
            val dockY by animateDpAsState(
                targetValue = freeY * (geometry.yPercent / 100f),
                animationSpec = AetherMotion.soft(policy.animationsEnabled),
                label = "dockY"
            )

            Column(modifier = Modifier.fillMaxSize().padding(bottom = dims.height + 12.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Text(
                        text = "\u22EE",
                        color = contentColor,
                        style = homeTextStyle(),
                        fontSize = 22.sp,
                        modifier = Modifier
                            .clickable { menuOpen = true }
                            .padding(horizontal = 20.dp, vertical = 10.dp)
                    )
                }

                if (settings.showClock && policy.widgetsEnabled) {
                    ClockWidget(
                        showDate = settings.showDate,
                        color = contentColor,
                        textStyle = homeTextStyle(),
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .pointerInput(Unit) {
                            var total = 0f
                            detectVerticalDragGestures(
                                onDragStart = { total = 0f },
                                onDragEnd = {
                                    val threshold = 90.dp.toPx()
                                    if (total < -threshold) {
                                        latestActions.onGesture(latestSettings.swipeUpAction)
                                    } else if (total > threshold) {
                                        latestActions.onGesture(latestSettings.swipeDownAction)
                                    }
                                },
                                onDragCancel = { total = 0f },
                                onVerticalDrag = { _, amount -> total += amount }
                            )
                        }
                ) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize(),
                        beyondViewportPageCount = pageCount,
                        flingBehavior = PagerDefaults.flingBehavior(
                            state = pagerState,
                            snapAnimationSpec = spring(dampingRatio = 0.78f, stiffness = 400f)
                        )
                    ) { page ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer {
                                    if (policy.animationsEnabled) {
                                        val offset = ((pagerState.currentPage - page) +
                                            pagerState.currentPageOffsetFraction)
                                            .absoluteValue.coerceIn(0f, 1f)
                                        alpha = 1f - offset * 0.5f
                                        val s = 1f - offset * 0.08f
                                        scaleX = s
                                        scaleY = s
                                    }
                                }
                        ) {
                            PageGrid(
                                page = page,
                                refs = layout.pages.getOrElse(page) { emptyList() },
                                appsByKey = appsByKey,
                                settings = settings,
                                iconLoader = iconLoader,
                                actions = actions,
                                drag = drag,
                                isCurrent = page == currentPage,
                                hoverIndex = (hover as? HoverTarget.PageCell)
                                    ?.takeIf { it.page == page }?.index,
                                onDrop = handleDrop,
                                onBackgroundLongPress = { menuOpen = true },
                                onBackgroundDoubleTap = {
                                    actions.onGesture(settings.doubleTapAction)
                                }
                            )
                        }
                    }
                }

                if (pageCount > 1) {
                    PageIndicator(
                        count = pageCount,
                        current = currentPage,
                        color = contentColor
                    )
                }
            }

            HomeDock(
                refs = layout.dock,
                settings = settings,
                geometry = geometry,
                dims = dims,
                appsByKey = appsByKey,
                iconLoader = iconLoader,
                drag = drag,
                hoverSlot = (hover as? HoverTarget.DockSlot)?.index,
                actions = actions,
                onDrop = handleDrop,
                onEdit = {
                    dockDraft = geometry
                    editingDock = true
                },
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .offset(x = dockX, y = -dockY)
            )
        }

        if (drag.active) {
            RemoveZone(highlight = hover == HoverTarget.Remove)
        }
        DragGhost(drag = drag, iconLoader = iconLoader)

        Box(modifier = Modifier.align(Alignment.Center)) {
            AnimatedVisibility(
                visible = editingDock,
                enter = scaleIn(spring(dampingRatio = 0.55f, stiffness = 380f), initialScale = 0.6f) + fadeIn(),
                exit = scaleOut(targetScale = 0.8f) + fadeOut()
            ) {
                DockEditorPopup(
                    geometry = geometry,
                    onChange = { dockDraft = it },
                    onCommit = { actions.onSetDockGeometry(it.xPercent, it.yPercent, it.scalePercent) },
                    onReset = {
                        val reset = DockGeometry(50, 0, 100)
                        dockDraft = reset
                        actions.onSetDockGeometry(reset.xPercent, reset.yPercent, reset.scalePercent)
                    },
                    onClose = {
                        editingDock = false
                        dockDraft = null
                    }
                )
            }
        }

        if (menuOpen) {
            AlertDialog(
                onDismissRequest = { menuOpen = false },
                title = { Text("Menu Home") },
                text = {
                    Column {
                        TextButton(onClick = {
                            menuOpen = false
                            actions.onOpenPicker(pagerState.currentPage)
                        }) { Text("Tambah aplikasi ke halaman ini") }
                        TextButton(onClick = {
                            menuOpen = false
                            actions.onOpenDrawer()
                        }) { Text("Buka App Drawer") }
                        TextButton(onClick = {
                            menuOpen = false
                            dockDraft = geometry
                            editingDock = true
                        }) { Text("Atur Dock") }
                        TextButton(onClick = {
                            menuOpen = false
                            actions.onOpenSettings()
                        }) { Text("Pengaturan") }
                        Spacer(Modifier.height(8.dp))
                        EmergencyExitButton(onOpen = actions.onEmergencyExit)
                    }
                },
                confirmButton = {
                    TextButton(onClick = { menuOpen = false }) { Text("Tutup") }
                }
            )
        }
    }
}

@Composable
private fun PageIndicator(count: Int, current: Int, color: Color, modifier: Modifier = Modifier) {
    val animations = LocalRenderPolicy.current.animationsEnabled
    Row(
        modifier = modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(count) { index ->
            val active = index == current
            val size = animateFloatAsState(
                targetValue = if (active) 9f else 6f,
                animationSpec = AetherMotion.bouncy(animations),
                label = "dot"
            )
            Box(
                modifier = Modifier
                    .padding(3.dp)
                    .size(size.value.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = if (active) 1f else 0.35f))
            )
        }
    }
}

/** Ikon yang sedang diseret, mengikuti jari dengan efek "membesar" yang kenyal. */
@Composable
private fun DragGhost(drag: HomeDragState, iconLoader: AppIconLoader) {
    val app = drag.app ?: return
    val iconSize = 60.dp
    val sizePx = with(androidx.compose.ui.platform.LocalDensity.current) { iconSize.toPx() }
    val animations = LocalRenderPolicy.current.animationsEnabled
    val scale = animateFloatAsState(
        targetValue = 1.2f,
        animationSpec = AetherMotion.bouncy(animations),
        label = "ghost"
    )

    Box(
        modifier = Modifier
            .offset { IntOffset((drag.pointer.x - sizePx / 2).roundToInt(), (drag.pointer.y - sizePx / 2).roundToInt()) }
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
            }
    ) {
        AppIcon(ref = app.ref, size = iconSize, loader = iconLoader)
    }
}

@Composable
private fun BoxScope.RemoveZone(highlight: Boolean) {
    val animations = LocalRenderPolicy.current.animationsEnabled
    val scale = animateFloatAsState(
        targetValue = if (highlight) 1.12f else 1f,
        animationSpec = AetherMotion.bouncy(animations),
        label = "removeZone"
    )
    Box(
        modifier = Modifier
            .align(Alignment.TopCenter)
            .statusBarsPadding()
            .padding(top = 12.dp)
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
            }
            .aetherSurface(RoundedCornerShape(24.dp), if (highlight) 1f else 0.7f)
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        Text(
            text = "Lepas di sini untuk menghapus",
            color = homeContentColor(),
            fontSize = 13.sp
        )
    }
}

/**
 * Background Home. Wallpaper Aether tampil tajam di belakang (panel kaca memakai versi
 * blur-nya). Dengan wallpaper sistem, wallpaper terlihat lewat tema window. AMOLED memakai
 * hitam murni; Safe Mode memakai warna tema standar.
 */
@Composable
private fun HomeBackdrop(visual: VisualStyle) {
    if (visual.safeMode) {
        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background))
    } else if (visual.style == ThemeStyle.AMOLED_DARK) {
        Box(Modifier.fillMaxSize().background(Color.Black))
    } else {
        WallpaperBackground()
        val scrim = if (visual.style == ThemeStyle.LIQUID_GLASS) 0.14f else 0.06f
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    listOf(
                        Color.Black.copy(alpha = scrim + 0.06f),
                        Color.Transparent,
                        Color.Black.copy(alpha = scrim + 0.10f)
                    )
                )
            )
        )
    }
}
