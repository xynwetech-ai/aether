package com.aethlauncher.kyn.core.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

/**
 * Wallpaper dalam dua lapis: [sharp] untuk background Home dan [blurred] (salinan kecil
 * yang sudah di-blur) untuk panel kaca. Keduanya di-crop dengan aturan yang sama, jadi
 * isi panel kaca selalu sejajar dengan wallpaper di belakangnya.
 */
@Immutable
class WallpaperLayers(val sharp: ImageBitmap, val blurred: ImageBitmap)

val LocalWallpaper = staticCompositionLocalOf<WallpaperLayers?> { null }

/** Ukuran root Compose (px), dipakai untuk menyejajarkan wallpaper di dalam panel kaca. */
val LocalRootSize = compositionLocalOf { IntSize.Zero }

private val SaturationFilter = ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(1.3f) })

/** Menggambar [image] memenuhi [target] dengan aturan center-crop. */
internal fun DrawScope.drawCropped(
    image: ImageBitmap,
    target: IntSize,
    alpha: Float = 1f,
    colorFilter: ColorFilter? = null
) {
    if (target.width <= 0 || target.height <= 0) return
    val scale = maxOf(target.width.toFloat() / image.width, target.height.toFloat() / image.height)
    val srcW = minOf(image.width, (target.width / scale).roundToInt().coerceAtLeast(1))
    val srcH = minOf(image.height, (target.height / scale).roundToInt().coerceAtLeast(1))
    drawImage(
        image = image,
        srcOffset = IntOffset((image.width - srcW) / 2, (image.height - srcH) / 2),
        srcSize = IntSize(srcW, srcH),
        dstSize = target,
        alpha = alpha,
        colorFilter = colorFilter,
        filterQuality = FilterQuality.Medium
    )
}

/**
 * Panel liquid glass: isi panel adalah wallpaper yang sudah di-blur (diambil dari posisi
 * panel di layar), diberi sedikit saturasi, tint gelap, kilau diagonal, dan garis tepi
 * bergradien. Jika blur tidak tersedia, jatuh ke panel translusen biasa.
 */
@Composable
fun Modifier.liquidGlass(shape: Shape, strength: Float = 1f): Modifier {
    val wallpaper = LocalWallpaper.current
    val rootSize = LocalRootSize.current
    val policy = LocalRenderPolicy.current
    val blurred = if (policy.blurEnabled) wallpaper?.blurred else null
    var origin by remember { mutableStateOf(Offset.Zero) }
    val s = strength.coerceIn(0f, 1.2f)

    return this
        .clip(shape)
        .onGloballyPositioned { origin = it.positionInRoot() }
        .drawBehind {
            if (blurred != null && rootSize.width > 0) {
                translate(left = -origin.x, top = -origin.y) {
                    drawCropped(
                        image = blurred,
                        target = rootSize,
                        alpha = (0.4f + 0.6f * s).coerceAtMost(1f),
                        colorFilter = SaturationFilter
                    )
                }
            }
            val darken = if (blurred != null) 0.20f else 0.34f
            drawRect(Color.Black.copy(alpha = darken * s))
            drawRect(
                Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.26f * s),
                        Color.White.copy(alpha = 0.04f * s),
                        Color.Transparent
                    ),
                    start = Offset.Zero,
                    end = Offset(size.width, size.height)
                )
            )
            val outline = shape.createOutline(size, layoutDirection, this)
            drawOutline(
                outline = outline,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.70f * s),
                        Color.White.copy(alpha = 0.10f * s),
                        Color.White.copy(alpha = 0.38f * s)
                    ),
                    start = Offset.Zero,
                    end = Offset(size.width, size.height)
                ),
                style = Stroke(width = 2.dp.toPx())
            )
        }
}

/** Permukaan panel yang mengikuti gaya tema aktif. */
@Composable
fun Modifier.aetherSurface(shape: Shape, opacity: Float = 1f): Modifier {
    val visual = LocalVisualStyle.current
    if (visual.safeMode) {
        return this.background(MaterialTheme.colorScheme.surfaceVariant, shape)
    }
    return when (visual.style) {
        ThemeStyle.LIQUID_GLASS -> this.liquidGlass(shape, opacity)
        ThemeStyle.ULTRA_MINIMAL -> this.background(Color.Black.copy(alpha = 0.30f * opacity), shape)
        ThemeStyle.AMOLED_DARK -> this.background(Color(0xFF121212).copy(alpha = opacity.coerceIn(0.5f, 1f)), shape)
    }
}

/** Wallpaper tajam memenuhi layar. Kosong jika memakai wallpaper sistem. */
@Composable
fun WallpaperBackground(modifier: Modifier = Modifier) {
    val wallpaper = LocalWallpaper.current
    Box(
        modifier = modifier
            .fillMaxSize()
            .drawBehind {
                if (wallpaper != null) {
                    drawCropped(
                        image = wallpaper.sharp,
                        target = IntSize(size.width.roundToInt(), size.height.roundToInt())
                    )
                }
            }
    )
}
