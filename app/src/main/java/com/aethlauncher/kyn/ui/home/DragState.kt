package com.aethlauncher.kyn.ui.home

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import com.aethlauncher.kyn.launcher.resolver.LaunchableApp

sealed interface DragOrigin {
    data class Page(val page: Int) : DragOrigin
    data object Dock : DragOrigin
}

sealed interface HoverTarget {
    data class PageCell(val page: Int, val index: Int) : HoverTarget
    data class DockSlot(val index: Int) : HoverTarget
    data object Remove : HoverTarget
}

/** Status drag-and-drop di Home. Semua koordinat dalam sistem koordinat root Compose. */
@Stable
class HomeDragState {
    var app by mutableStateOf<LaunchableApp?>(null)
        private set
    var origin by mutableStateOf<DragOrigin?>(null)
        private set
    var pointer by mutableStateOf(Offset.Zero)
        private set

    /** Area grid halaman yang sedang tampil dan area dock, diisi oleh layout masing-masing. */
    var pageBounds by mutableStateOf<Rect?>(null)
    var dockBounds by mutableStateOf<Rect?>(null)

    val active: Boolean get() = app != null

    fun start(app: LaunchableApp, origin: DragOrigin, rootPointer: Offset) {
        this.app = app
        this.origin = origin
        this.pointer = rootPointer
    }

    fun move(rootPointer: Offset) {
        pointer = rootPointer
    }

    fun finish() {
        app = null
        origin = null
    }
}

/** Menentukan target lepas dari posisi jari. Zona hapus ada di bagian atas layar. */
fun resolveHover(
    state: HomeDragState,
    currentPage: Int,
    columns: Int,
    rows: Int,
    dockSlots: Int,
    removeZoneBottom: Float
): HoverTarget? {
    if (!state.active) return null
    val p = state.pointer
    if (p.y < removeZoneBottom) return HoverTarget.Remove

    val dock = state.dockBounds
    if (dock != null && dock.contains(p) && dockSlots > 0) {
        val slotWidth = dock.width / dockSlots
        val index = ((p.x - dock.left) / slotWidth).toInt().coerceIn(0, dockSlots - 1)
        return HoverTarget.DockSlot(index)
    }

    val page = state.pageBounds
    if (page != null && page.contains(p) && columns > 0 && rows > 0) {
        val column = ((p.x - page.left) / (page.width / columns)).toInt().coerceIn(0, columns - 1)
        val row = ((p.y - page.top) / (page.height / rows)).toInt().coerceIn(0, rows - 1)
        return HoverTarget.PageCell(currentPage, row * columns + column)
    }
    return null
}
