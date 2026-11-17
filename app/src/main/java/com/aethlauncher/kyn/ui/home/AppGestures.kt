package com.aethlauncher.kyn.ui.home

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitLongPressOrCancellation
import androidx.compose.foundation.gestures.drag
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputScope

/**
 * Satu detektor untuk ketuk, tekan lama, dan geser setelah tekan lama.
 *
 * Memakai satu detektor (bukan detectTapGestures + detectDragGesturesAfterLongPress) agar
 * lepas jari setelah tekan lama tidak ikut dihitung sebagai ketuk dan membuka aplikasi.
 * [onDragEnd] menerima true jika jari benar-benar bergeser melewati batas slop.
 */
suspend fun PointerInputScope.detectAppGestures(
    onPressChange: (Boolean) -> Unit,
    onTap: () -> Unit,
    onDragStart: (Offset) -> Unit,
    onDrag: (Offset) -> Unit,
    onDragEnd: (moved: Boolean) -> Unit
) {
    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false)
        onPressChange(true)

        val longPress = awaitLongPressOrCancellation(down.id)
        if (longPress == null) {
            val change = currentEvent.changes.firstOrNull { it.id == down.id }
            if (change != null && !change.pressed) onTap()
            onPressChange(false)
            return@awaitEachGesture
        }

        onDragStart(longPress.position)
        var moved = false
        val slop = viewConfiguration.touchSlop
        drag(longPress.id) { change ->
            change.consume()
            onDrag(change.position)
            if ((change.position - longPress.position).getDistance() > slop) moved = true
        }
        onDragEnd(moved)
        onPressChange(false)
    }
}
