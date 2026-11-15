package com.aethlauncher.kyn.launcher.resolver

import android.content.ComponentName
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import com.aethlauncher.kyn.core.performance.IconCache
import com.aethlauncher.kyn.core.utils.AetherLog
import com.aethlauncher.kyn.data.models.ShortcutRef
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Memuat ikon di luar main thread. Ikon rusak jatuh ke ikon default sistem. */
class AppIconLoader(
    context: Context,
    private val cache: IconCache
) {
    private val appContext = context.applicationContext
    private val sizePx = (ICON_DP * appContext.resources.displayMetrics.density)
        .toInt().coerceAtLeast(48)

    fun cached(ref: ShortcutRef): Bitmap? = cache.get(ref.key)

    suspend fun load(ref: ShortcutRef): Bitmap {
        cache.get(ref.key)?.let { return it }
        return withContext(Dispatchers.IO) {
            cache.get(ref.key) ?: render(ref).also { cache.put(ref.key, it) }
        }
    }

    private fun render(ref: ShortcutRef): Bitmap {
        val pm = appContext.packageManager
        return try {
            toBitmap(pm.getActivityIcon(ComponentName(ref.packageName, ref.className)))
        } catch (e: Exception) {
            AetherLog.w("Ikon gagal dimuat, memakai ikon default: ${ref.packageName}")
            toBitmap(pm.defaultActivityIcon)
        }
    }

    private fun toBitmap(drawable: Drawable): Bitmap {
        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        drawable.setBounds(0, 0, sizePx, sizePx)
        drawable.draw(Canvas(bitmap))
        return bitmap
    }

    companion object {
        /** Ukuran ikon maksimum yang dirender di UI (dp). */
        const val ICON_DP = 60
    }
}
