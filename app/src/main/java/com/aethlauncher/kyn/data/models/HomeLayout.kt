package com.aethlauncher.kyn.data.models

/** Referensi ke satu aktivitas launchable. Disimpan sebagai "package/class". */
data class ShortcutRef(
    val packageName: String,
    val className: String
) {
    val key: String get() = "$packageName/$className"

    companion object {
        fun fromKey(key: String): ShortcutRef? {
            val separator = key.indexOf('/')
            if (separator <= 0 || separator == key.length - 1) return null
            return ShortcutRef(key.substring(0, separator), key.substring(separator + 1))
        }
    }
}

/**
 * Susunan Home Screen. Ikon disimpan berurutan per halaman dan mengisi grid dari kiri
 * atas, jadi mengubah ukuran grid tidak pernah menimpa atau memotong ikon. Ikon yang
 * tidak muat tetap tersimpan dan muncul lagi saat grid diperbesar.
 */
data class HomeLayout(
    val pages: List<List<ShortcutRef>> = listOf(emptyList()),
    val dock: List<ShortcutRef> = emptyList()
)
