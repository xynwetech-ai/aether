package com.aethlauncher.kyn.core.performance

import android.graphics.Bitmap
import android.util.LruCache

// Ukuran cache menyesuaikan memori aplikasi: 1/16 heap, minimal 2 MB.
private fun defaultMaxSizeKb(): Int =
    (Runtime.getRuntime().maxMemory() / 1024 / 16).toInt().coerceAtLeast(2048)

/** Cache ikon aplikasi berbasis LruCache, dihitung dalam KB bitmap. */
class IconCache {

    private val cache = object : LruCache<String, Bitmap>(defaultMaxSizeKb()) {
        override fun sizeOf(key: String, value: Bitmap): Int = value.byteCount / 1024
    }

    fun get(key: String): Bitmap? = cache.get(key)

    fun put(key: String, bitmap: Bitmap) {
        cache.put(key, bitmap)
    }

    fun clear() {
        cache.evictAll()
    }
}
