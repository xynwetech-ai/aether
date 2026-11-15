package com.aethlauncher.kyn.data

import com.aethlauncher.kyn.data.local.HomeLayoutCodec
import com.aethlauncher.kyn.data.models.ShortcutRef
import org.junit.Assert.assertEquals
import org.junit.Test

class HomeLayoutCodecTest {

    private val a = ShortcutRef("com.example.a", "com.example.a.Main")
    private val b = ShortcutRef("com.example.b", "com.example.b.Main\$Alias")

    @Test
    fun roundTrip_keepsPagesAndOrder() {
        val pages = listOf(listOf(a, b), emptyList(), listOf(b))
        val decoded = HomeLayoutCodec.decodePages(HomeLayoutCodec.encodePages(pages))
        assertEquals(pages, decoded)
    }

    @Test
    fun nullOrEmpty_givesSingleEmptyPage() {
        assertEquals(listOf(emptyList<ShortcutRef>()), HomeLayoutCodec.decodePages(null))
        assertEquals(listOf(emptyList<ShortcutRef>()), HomeLayoutCodec.decodePages(""))
    }

    @Test
    fun corruptedEntries_areSkipped() {
        val decoded = HomeLayoutCodec.decodeRefs("garbage;${a.key};/nopackage;noclass/;")
        assertEquals(listOf(a), decoded)
    }

    @Test
    fun dockRoundTrip() {
        val dock = listOf(b, a)
        assertEquals(dock, HomeLayoutCodec.decodeRefs(HomeLayoutCodec.encodeRefs(dock)))
    }
}
