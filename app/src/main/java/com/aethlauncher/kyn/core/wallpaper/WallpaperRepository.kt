package com.aethlauncher.kyn.core.wallpaper

import android.annotation.SuppressLint
import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.net.Uri
import androidx.compose.ui.graphics.asImageBitmap
import com.aethlauncher.kyn.core.theme.WallpaperLayers
import com.aethlauncher.kyn.core.theme.WallpaperMode
import com.aethlauncher.kyn.core.utils.AetherLog
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Menyiapkan wallpaper Aether (preset atau gambar sendiri) beserta salinan blur-nya. */
class WallpaperRepository(context: Context) {

    private val appContext = context.applicationContext
    private val file = File(appContext.filesDir, "wallpaper.jpg")

    /** Return null jika memakai wallpaper sistem. */
    suspend fun load(mode: WallpaperMode, preset: Int, blurWidth: Int, blurRadius: Int): WallpaperLayers? =
        withContext(Dispatchers.Default) {
            val sharp: Bitmap = when (mode) {
                WallpaperMode.SYSTEM -> return@withContext null
                WallpaperMode.PRESET -> PresetWallpapers.render(preset, PRESET_W, PRESET_H)
                WallpaperMode.CUSTOM -> decodeCustom() ?: PresetWallpapers.render(0, PRESET_W, PRESET_H)
            }
            val blurred = BitmapBlur.blurredCopy(sharp, blurWidth, blurRadius)
            WallpaperLayers(sharp.asImageBitmap(), blurred.asImageBitmap())
        }

    /** Menyalin gambar pilihan pengguna ke penyimpanan internal (diperkecil). */
    suspend fun importFrom(uri: Uri): Boolean = withContext(Dispatchers.IO) {
        try {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            appContext.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
            val options = BitmapFactory.Options().apply {
                inSampleSize = sampleSizeFor(bounds.outWidth, bounds.outHeight)
            }
            val bitmap = appContext.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, options)
            } ?: return@withContext false
            save(bitmap)
            true
        } catch (e: Exception) {
            AetherLog.w("Gagal mengimpor wallpaper", e)
            false
        }
    }

    /**
     * Mencoba menyalin wallpaper sistem. Banyak versi Android melarang aplikasi biasa
     * membacanya; kalau gagal, pengguna diminta memilih gambar sendiri.
     */
    @SuppressLint("MissingPermission")
    suspend fun copySystemWallpaper(): Boolean = withContext(Dispatchers.IO) {
        try {
            val drawable = WallpaperManager.getInstance(appContext).drawable
                ?: return@withContext false
            val iw = drawable.intrinsicWidth
            val ih = drawable.intrinsicHeight
            if (iw <= 0 || ih <= 0) return@withContext false
            val scale = minOf(1f, MAX_SIDE.toFloat() / maxOf(iw, ih))
            val w = (iw * scale).toInt().coerceAtLeast(1)
            val h = (ih * scale).toInt().coerceAtLeast(1)
            val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            drawable.setBounds(0, 0, w, h)
            drawable.draw(Canvas(bitmap))
            save(bitmap)
            true
        } catch (e: Exception) {
            AetherLog.w("Wallpaper sistem tidak bisa dibaca", e)
            false
        }
    }

    private fun decodeCustom(): Bitmap? {
        if (!file.exists()) return null
        return try {
            BitmapFactory.decodeFile(file.absolutePath)
        } catch (e: Exception) {
            AetherLog.w("Wallpaper custom rusak, memakai preset", e)
            null
        }
    }

    private fun save(bitmap: Bitmap) {
        FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it) }
    }

    private fun sampleSizeFor(width: Int, height: Int): Int {
        var sample = 1
        var longest = maxOf(width, height)
        while (longest / 2 >= MAX_SIDE) {
            longest /= 2
            sample *= 2
        }
        return sample
    }

    private companion object {
        const val PRESET_W = 720
        const val PRESET_H = 1560
        const val MAX_SIDE = 1600
    }
}
