package com.aethlauncher.kyn.ui.home

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aethlauncher.kyn.core.preferences.AppSettings
import com.aethlauncher.kyn.data.models.ShortcutRef
import com.aethlauncher.kyn.launcher.manager.AppLauncher
import com.aethlauncher.kyn.launcher.resolver.AppIconLoader
import com.aethlauncher.kyn.launcher.resolver.LaunchableApp
import com.aethlauncher.kyn.ui.components.AppIcon

private val MaxIconSize = AppIconLoader.ICON_DP.dp

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
    onLongPressEmpty: () -> Unit,
    modifier: Modifier = Modifier
) {
    val columns = settings.gridColumns
    val rows = settings.gridRows
    val items = remember(refs, appsByKey, columns, rows) {
        refs.mapNotNull { appsByKey[it.key] }.take(columns * rows)
    }
    val latestLongPress by rememberUpdatedState(onLongPressEmpty)

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onLongPress = { latestLongPress() })
            }
    ) {
        val cellWidth = maxWidth / columns
        val cellHeight = maxHeight / rows
        val iconSize = minOf(
            cellWidth * 0.62f,
            cellHeight * (if (settings.showLabels) 0.5f else 0.66f),
            MaxIconSize
        )

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
                        val app = items.getOrNull(row * columns + column)
                        Box(
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                            contentAlignment = Alignment.Center
                        ) {
                            if (app != null) {
                                AppTile(
                                    app = app,
                                    iconSize = iconSize,
                                    showLabel = settings.showLabels,
                                    labelSize = 11.sp,
                                    iconLoader = iconLoader,
                                    onMoveLeft = { actions.onMoveShortcut(page, app.ref, -1) },
                                    onMoveRight = { actions.onMoveShortcut(page, app.ref, 1) },
                                    onRemove = { actions.onRemoveShortcut(page, app.ref) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Ikon + label. Ketuk untuk membuka aplikasi, tekan lama untuk menu geser/hapus. */
@Composable
fun AppTile(
    app: LaunchableApp,
    iconSize: Dp,
    showLabel: Boolean,
    labelSize: TextUnit,
    iconLoader: AppIconLoader,
    onMoveLeft: () -> Unit,
    onMoveRight: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var menuExpanded by remember { mutableStateOf(false) }
    val contentColor = homeContentColor()
    val textStyle = homeTextStyle()

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 2.dp)
                .pointerInput(app.ref.key) {
                    detectTapGestures(
                        onTap = { launchApp(context, app) },
                        onLongPress = { menuExpanded = true }
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
            DropdownMenuItem(
                text = { Text("Geser ke kiri") },
                onClick = {
                    menuExpanded = false
                    onMoveLeft()
                }
            )
            DropdownMenuItem(
                text = { Text("Geser ke kanan") },
                onClick = {
                    menuExpanded = false
                    onMoveRight()
                }
            )
            DropdownMenuItem(
                text = { Text("Hapus dari Home") },
                onClick = {
                    menuExpanded = false
                    onRemove()
                }
            )
        }
    }
}

private fun launchApp(context: Context, app: LaunchableApp) {
    if (!AppLauncher.launch(context, app.ref)) {
        Toast.makeText(context, "Aplikasi tidak bisa dibuka", Toast.LENGTH_SHORT).show()
    }
}
