package com.aethlauncher.kyn.domain

import com.aethlauncher.kyn.data.models.HomeLayout
import com.aethlauncher.kyn.data.models.ShortcutRef
import com.aethlauncher.kyn.domain.usecase.HomeLayoutOps
import org.junit.Assert.assertEquals
import org.junit.Test

class HomeLayoutDropTest {

    private val a = ShortcutRef("p.a", "p.a.Main")
    private val b = ShortcutRef("p.b", "p.b.Main")
    private val c = ShortcutRef("p.c", "p.c.Main")

    @Test
    fun placeOnPage_reordersWithinPage() {
        val layout = HomeLayout(pages = listOf(listOf(a, b, c)))
        // a diseret ke indeks 2 (daftar tanpa a = [b, c], jadi indeks 2 = paling akhir)
        val result = HomeLayoutOps.placeOnPage(layout, a, 0, 2)
        assertEquals(listOf(b, c, a), result.pages[0])
    }

    @Test
    fun placeOnPage_movesBetweenPages_andNeverDuplicates() {
        val layout = HomeLayout(pages = listOf(listOf(a, b), listOf(c)))
        val result = HomeLayoutOps.placeOnPage(layout, a, 1, 0)
        assertEquals(listOf(b), result.pages[0])
        assertEquals(listOf(a, c), result.pages[1])
    }

    @Test
    fun placeOnPage_fromDock_removesFromDock() {
        val layout = HomeLayout(pages = listOf(listOf(b)), dock = listOf(a))
        val result = HomeLayoutOps.placeOnPage(layout, a, 0, 5)
        assertEquals(emptyList<ShortcutRef>(), result.dock)
        assertEquals(listOf(b, a), result.pages[0])
    }

    @Test
    fun placeOnDock_movesFromPage() {
        val layout = HomeLayout(pages = listOf(listOf(a, b)), dock = listOf(c))
        val result = HomeLayoutOps.placeOnDock(layout, a, 0, capacity = 4, visibleDockCountAfterRemoval = 1)
        assertEquals(listOf(a, c), result.dock)
        assertEquals(listOf(b), result.pages[0])
    }

    @Test
    fun placeOnDock_isRejectedWhenFull() {
        val layout = HomeLayout(pages = listOf(listOf(a)), dock = listOf(b, c))
        val result = HomeLayoutOps.placeOnDock(layout, a, 0, capacity = 2, visibleDockCountAfterRemoval = 2)
        assertEquals(layout, result)
    }

    @Test
    fun placeOnDock_reorderInsideFullDockIsAllowed() {
        val layout = HomeLayout(dock = listOf(a, b))
        // setelah a dilepas dari dock, tersisa 1 item, jadi muat lagi
        val result = HomeLayoutOps.placeOnDock(layout, a, 1, capacity = 2, visibleDockCountAfterRemoval = 1)
        assertEquals(listOf(b, a), result.dock)
    }

    @Test
    fun removeEverywhere_clearsPagesAndDock() {
        val layout = HomeLayout(pages = listOf(listOf(a), listOf(a, b)), dock = listOf(a))
        val result = HomeLayoutOps.removeEverywhere(layout, a)
        assertEquals(listOf(emptyList<ShortcutRef>(), listOf(b)), result.pages)
        assertEquals(emptyList<ShortcutRef>(), result.dock)
    }
}
