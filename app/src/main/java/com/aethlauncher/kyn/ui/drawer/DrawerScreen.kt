package com.aethlauncher.kyn.ui.drawer

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aethlauncher.kyn.core.theme.LocalRenderPolicy
import com.aethlauncher.kyn.core.theme.LocalVisualStyle
import com.aethlauncher.kyn.core.theme.LocalWallpaper
import com.aethlauncher.kyn.core.theme.ThemeStyle
import com.aethlauncher.kyn.core.theme.aetherSurface
import com.aethlauncher.kyn.launcher.resolver.AppIconLoader
import com.aethlauncher.kyn.launcher.resolver.LaunchableApp
import com.aethlauncher.kyn.ui.home.AppTile
import com.aethlauncher.kyn.ui.home.MenuAction
import com.aethlauncher.kyn.ui.home.homeContentColor
import kotlinx.coroutines.delay

/** App Drawer: panel kaca penuh dengan pencarian instan, kategori, dan grid aplikasi. */
@Composable
fun DrawerScreen(
    state: DrawerUiState,
    iconLoader: AppIconLoader,
    focusSearch: Boolean,
    onQueryChange: (String) -> Unit,
    onSelectCategory: (String?) -> Unit,
    onLaunch: (LaunchableApp) -> Unit,
    onAddToHome: (LaunchableApp) -> Unit,
    onAddToDock: (LaunchableApp) -> Unit,
    onHide: (LaunchableApp) -> Unit,
    onAppInfo: (LaunchableApp) -> Unit,
    onMoveCategory: (LaunchableApp, String?) -> Unit,
    onWebSearch: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val visual = LocalVisualStyle.current
    val policy = LocalRenderPolicy.current
    val contentColor = homeContentColor()
    val shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    val focusRequester = remember { FocusRequester() }
    var movingApp by remember { mutableStateOf<LaunchableApp?>(null) }

    // Tanpa wallpaper yang bisa di-blur, panel dibuat lebih pekat agar Home di belakangnya tidak mengganggu.
    val glassAvailable = LocalWallpaper.current != null && policy.blurEnabled
    val needsScrim = !visual.safeMode && visual.style != ThemeStyle.AMOLED_DARK && !glassAvailable

    LaunchedEffect(focusSearch) {
        if (focusSearch) {
            delay(200)
            focusRequester.requestFocus()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(top = 8.dp)
            .clip(shape)
            .background(if (needsScrim) Color.Black.copy(alpha = 0.55f) else Color.Transparent, shape)
            .aetherSurface(shape, 1f)
    ) {
        // Handle: seret ke bawah untuk menutup.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .pointerInput(Unit) {
                    var total = 0f
                    detectVerticalDragGestures(
                        onDragStart = { total = 0f },
                        onDragEnd = { if (total > 80.dp.toPx()) onClose() },
                        onDragCancel = { total = 0f },
                        onVerticalDrag = { _, amount -> total += amount }
                    )
                }
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(width = 44.dp, height = 5.dp)
                    .clip(CircleShape)
                    .background(contentColor.copy(alpha = 0.5f))
            )
        }

        TextField(
            value = state.query,
            onValueChange = onQueryChange,
            placeholder = { Text("Cari aplikasi", color = contentColor.copy(alpha = 0.6f)) },
            singleLine = true,
            shape = RoundedCornerShape(24.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White.copy(alpha = 0.14f),
                unfocusedContainerColor = Color.White.copy(alpha = 0.10f),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                focusedTextColor = contentColor,
                unfocusedTextColor = contentColor,
                cursorColor = contentColor
            ),
            trailingIcon = {
                if (state.query.isNotEmpty()) {
                    TextButton(onClick = { onQueryChange("") }) { Text("Hapus", color = contentColor) }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .focusRequester(focusRequester)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            state.categories.forEach { option ->
                FilterChip(
                    selected = state.selected == option.id,
                    onClick = { onSelectCategory(option.id) },
                    label = { Text(option.label) },
                    colors = FilterChipDefaults.filterChipColors(
                        labelColor = contentColor,
                        selectedLabelColor = contentColor,
                        selectedContainerColor = Color.White.copy(alpha = 0.25f)
                    )
                )
            }
        }

        when {
            !state.loaded -> Text(
                text = "Memuat aplikasi...",
                color = contentColor.copy(alpha = 0.7f),
                modifier = Modifier.padding(24.dp)
            )

            state.apps.isEmpty() -> Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "Tidak ada aplikasi yang cocok", color = contentColor.copy(alpha = 0.8f))
                if (state.query.isNotBlank()) {
                    TextButton(onClick = { onWebSearch(state.query.trim()) }) {
                        Text("Cari \"${state.query.trim()}\" di web", color = contentColor)
                    }
                }
            }

            else -> LazyVerticalGrid(
                columns = GridCells.Fixed(state.columns),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(state.apps, key = { it.ref.key }) { app ->
                    Box(modifier = Modifier.height(96.dp)) {
                        AppTile(
                            app = app,
                            iconSize = 52.dp,
                            showLabel = true,
                            labelSize = 11.sp,
                            iconLoader = iconLoader,
                            menuActions = listOf(
                                MenuAction("Tambah ke Home") { onAddToHome(app) },
                                MenuAction("Tambah ke Dock") { onAddToDock(app) },
                                MenuAction("Pindah kategori") { movingApp = app },
                                MenuAction("Sembunyikan") { onHide(app) },
                                MenuAction("Info aplikasi") { onAppInfo(app) }
                            ),
                            onLaunch = onLaunch
                        )
                    }
                }
            }
        }
    }

    val moving = movingApp
    if (moving != null) {
        AlertDialog(
            onDismissRequest = { movingApp = null },
            title = { Text("Pindah ${moving.label} ke") },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = {
                        onMoveCategory(moving, null)
                        movingApp = null
                    }) { Text("Otomatis (reset)") }
                    state.moveTargets.forEach { target ->
                        TextButton(onClick = {
                            onMoveCategory(moving, target.id)
                            movingApp = null
                        }) { Text(target.label) }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { movingApp = null }) { Text("Batal") } }
        )
    }
}
