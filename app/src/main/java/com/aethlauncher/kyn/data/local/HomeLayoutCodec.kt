package com.aethlauncher.kyn.data.local

import com.aethlauncher.kyn.data.models.ShortcutRef

/**
 * Format teks sederhana untuk layout. Nama package dan class Android tidak pernah
 * memuat karakter pemisah ('|', ';', '/' di luar pemisah package dan class).
 * Entri yang rusak dilewati supaya data korup tidak menggagalkan startup.
 */
object HomeLayoutCodec {
    private const val PAGE_SEPARATOR = "|"
    private const val REF_SEPARATOR = ";"

    fun encodePages(pages: List<List<ShortcutRef>>): String =
        pages.joinToString(PAGE_SEPARATOR) { encodeRefs(it) }

    fun decodePages(raw: String?): List<List<ShortcutRef>> {
        if (raw == null) return listOf(emptyList())
        return raw.split(PAGE_SEPARATOR).map { decodeRefs(it) }
            .ifEmpty { listOf(emptyList()) }
    }

    fun encodeRefs(refs: List<ShortcutRef>): String =
        refs.joinToString(REF_SEPARATOR) { it.key }

    fun decodeRefs(raw: String?): List<ShortcutRef> =
        raw.orEmpty().split(REF_SEPARATOR).mapNotNull { ShortcutRef.fromKey(it) }
}
