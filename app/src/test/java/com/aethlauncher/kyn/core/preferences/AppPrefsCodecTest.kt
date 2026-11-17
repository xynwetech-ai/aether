package com.aethlauncher.kyn.core.preferences

import org.junit.Assert.assertEquals
import org.junit.Test

class AppPrefsCodecTest {

    @Test
    fun categories_roundTrip() {
        val list = listOf(CustomCategory("c1", "Kerja"), CustomCategory("c2", "Hiburan"))
        assertEquals(list, AppPrefsCodec.decodeCategories(AppPrefsCodec.encodeCategories(list)))
    }

    @Test
    fun overrides_roundTrip() {
        val map = mapOf("a/b" to "c1", "x/y" to "auto:games")
        assertEquals(map, AppPrefsCodec.decodeOverrides(AppPrefsCodec.encodeOverrides(map)))
    }

    @Test
    fun corruptedLines_areSkipped() {
        val decoded = AppPrefsCodec.decodeCategories("c1\tKerja\nrusak\n\t\nc2\t")
        assertEquals(listOf(CustomCategory("c1", "Kerja")), decoded)
    }

    @Test
    fun cleanName_removesControlCharactersAndLimitsLength() {
        assertEquals("a b", AppPrefsCodec.cleanName("a\tb\n"))
        assertEquals(24, AppPrefsCodec.cleanName("x".repeat(60)).length)
    }

    @Test
    fun nullInput_givesEmpty() {
        assertEquals(emptyList<CustomCategory>(), AppPrefsCodec.decodeCategories(null))
        assertEquals(emptyMap<String, String>(), AppPrefsCodec.decodeOverrides(null))
    }
}
