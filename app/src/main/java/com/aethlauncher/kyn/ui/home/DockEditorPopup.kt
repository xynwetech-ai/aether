package com.aethlauncher.kyn.ui.home

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aethlauncher.kyn.core.preferences.AppSettings
import com.aethlauncher.kyn.core.theme.aetherSurface
import kotlin.math.roundToInt

/**
 * Popup kecil untuk mengatur posisi (X, Y) dan ukuran dock. Bisa digeser lewat header
 * dan ditutup. Perubahan langsung terlihat ([onChange]) dan disimpan saat slider dilepas ([onCommit]).
 */
@Composable
fun DockEditorPopup(
    geometry: DockGeometry,
    onChange: (DockGeometry) -> Unit,
    onCommit: (DockGeometry) -> Unit,
    onReset: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var offset by remember { mutableStateOf(Offset.Zero) }
    val contentColor = homeContentColor()

    Column(
        modifier = modifier
            .offset { IntOffset(offset.x.roundToInt(), offset.y.roundToInt()) }
            .width(300.dp)
            .aetherSurface(RoundedCornerShape(24.dp), 1f)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        offset += dragAmount
                    }
                },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "Atur Dock  (geser di sini)", color = contentColor, fontSize = 14.sp)
            TextButton(onClick = onClose) { Text("Tutup", color = contentColor) }
        }

        DockSliderRow(
            label = "Posisi X",
            value = geometry.xPercent,
            range = 0..100,
            color = contentColor,
            onChange = { onChange(geometry.copy(xPercent = it)) },
            onFinished = { onCommit(geometry) }
        )
        DockSliderRow(
            label = "Posisi Y",
            value = geometry.yPercent,
            range = 0..100,
            color = contentColor,
            onChange = { onChange(geometry.copy(yPercent = it)) },
            onFinished = { onCommit(geometry) }
        )
        DockSliderRow(
            label = "Ukuran",
            value = geometry.scalePercent,
            range = AppSettings.MIN_SCALE..AppSettings.MAX_SCALE,
            color = contentColor,
            onChange = { onChange(geometry.copy(scalePercent = it)) },
            onFinished = { onCommit(geometry) }
        )

        TextButton(onClick = onReset, modifier = Modifier.align(Alignment.End)) {
            Text("Reset", color = contentColor)
        }
    }
}

@Composable
private fun DockSliderRow(
    label: String,
    value: Int,
    range: IntRange,
    color: androidx.compose.ui.graphics.Color,
    onChange: (Int) -> Unit,
    onFinished: () -> Unit
) {
    var local by remember(value) { mutableFloatStateOf(value.toFloat()) }
    Text(text = "$label: ${local.roundToInt()}%", color = color, fontSize = 13.sp)
    Slider(
        value = local,
        onValueChange = {
            local = it
            onChange(it.roundToInt())
        },
        onValueChangeFinished = onFinished,
        valueRange = range.first.toFloat()..range.last.toFloat(),
        colors = SliderDefaults.colors(
            thumbColor = color,
            activeTrackColor = color,
            inactiveTrackColor = color.copy(alpha = 0.3f)
        ),
        modifier = Modifier.padding(bottom = 4.dp)
    )
}
