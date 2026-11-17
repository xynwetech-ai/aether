package com.aethlauncher.kyn.core.wallpaper

import android.graphics.Bitmap
import kotlin.math.roundToInt

object BitmapBlur {

    /**
     * Membuat salinan kecil yang sudah di-blur. Sengaja kecil: blur murah, dan saat
     * diskalakan naik oleh GPU hasilnya halus seperti kaca buram.
     */
    fun blurredCopy(source: Bitmap, targetWidth: Int, radius: Int): Bitmap {
        val width = targetWidth.coerceAtLeast(16)
        val height = (width * source.height.toFloat() / source.width).roundToInt().coerceAtLeast(16)
        val small = Bitmap.createScaledBitmap(source, width, height, true)

        val pixels = IntArray(width * height)
        small.getPixels(pixels, 0, width, 0, 0, width, height)
        val blurred = BoxBlur.blur(pixels, width, height, radius)

        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        result.setPixels(blurred, 0, width, 0, 0, width, height)
        return result
    }
}
