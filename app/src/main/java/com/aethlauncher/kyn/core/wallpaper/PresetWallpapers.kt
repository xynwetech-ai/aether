package com.aethlauncher.kyn.core.wallpaper

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader

/** Wallpaper bawaan yang digambar lewat kode, jadi efek kaca langsung terlihat tanpa izin apa pun. */
object PresetWallpapers {

    val names = listOf("Aurora", "Senja", "Samudra", "Grafit")

    private class Blob(val x: Float, val y: Float, val radius: Float, val color: Int)

    private class Preset(val top: Int, val bottom: Int, val blobs: List<Blob>)

    private val presets = listOf(
        Preset(
            0xFF0B1026.toInt(), 0xFF123A56.toInt(),
            listOf(
                Blob(0.20f, 0.22f, 0.75f, 0xFF2EE6A6.toInt()),
                Blob(0.88f, 0.50f, 0.65f, 0xFF7B5CFF.toInt()),
                Blob(0.25f, 0.88f, 0.80f, 0xFF1E90FF.toInt())
            )
        ),
        Preset(
            0xFF1B0B2E.toInt(), 0xFF3A1230.toInt(),
            listOf(
                Blob(0.80f, 0.20f, 0.70f, 0xFFFF7E5F.toInt()),
                Blob(0.15f, 0.55f, 0.60f, 0xFFFEB47B.toInt()),
                Blob(0.70f, 0.90f, 0.80f, 0xFFB24592.toInt())
            )
        ),
        Preset(
            0xFF021B2B.toInt(), 0xFF05445E.toInt(),
            listOf(
                Blob(0.30f, 0.25f, 0.70f, 0xFF00C9FF.toInt()),
                Blob(0.90f, 0.60f, 0.60f, 0xFF0072FF.toInt()),
                Blob(0.20f, 0.90f, 0.70f, 0xFF00F5A0.toInt())
            )
        ),
        Preset(
            0xFF0E0F12.toInt(), 0xFF1B1E26.toInt(),
            listOf(
                Blob(0.25f, 0.30f, 0.70f, 0xFF4A5568.toInt()),
                Blob(0.85f, 0.65f, 0.60f, 0xFF2D3748.toInt()),
                Blob(0.30f, 0.90f, 0.70f, 0xFF5A67D8.toInt())
            )
        )
    )

    fun render(index: Int, width: Int, height: Int): Bitmap {
        val preset = presets[index.coerceIn(0, presets.lastIndex)]
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val w = width.toFloat()
        val h = height.toFloat()

        paint.shader = LinearGradient(0f, 0f, 0f, h, preset.top, preset.bottom, Shader.TileMode.CLAMP)
        canvas.drawRect(0f, 0f, w, h, paint)

        for (blob in preset.blobs) {
            val rgb = blob.color and 0x00FFFFFF
            paint.shader = RadialGradient(
                blob.x * w,
                blob.y * h,
                blob.radius * maxOf(w, h),
                (0xB0 shl 24) or rgb,
                rgb,
                Shader.TileMode.CLAMP
            )
            canvas.drawRect(0f, 0f, w, h, paint)
        }
        return bitmap
    }
}
