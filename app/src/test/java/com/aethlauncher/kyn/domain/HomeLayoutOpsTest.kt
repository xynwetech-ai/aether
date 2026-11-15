package com.aethlauncher.kyn.domain

import com.aethlauncher.kyn.data.models.HomeLayout
import com.aethlauncher.kyn.data.models.ShortcutRef
import com.aethlauncher.kyn.domain.usecase.HomeLayoutOps
import org.junit.Assert.assertEquals
import org.junit.Test

class HomeLayoutOpsTest {

    private val a = ShortcutRef("p.a", "p.a.Main")
    private val b = ShortcutRef("p.b", "p.b.Main")
    private val c = ShortcutRef("p.c", "p.c.Main")

    @Test
    fun addShortcut_ignoresDuplicates() {
        val once = HomeLayoutOps.addShortcut(HomeLayout(), 0, a)
        val twice = HomeLayoutOps.addShortcut(once, 0, a)
        assertEquals(listOf(a), twice.pages[0])
    }

    @Test
    fun addShortcut_invalidPage_isIgnored() {
        val layout = HomeLayout()
        assertEquals(layout, HomeLayoutOps.addShortcut(layout, 3, a))
    }

    @Test
    fun moveShortcut_swapsWithNeighbour() {
        val layout = HomeLayout(pages = listOf(listOf(a, b, c)))
        val moved = HomeLayoutOps.moveShortcut(layout, 0, b, 1)
        assertEquals(listOf(a, c, b), moved.pages[0])
    }

    @Test
    fun moveShortcut_atEdge_isIgnored() {
        val layout = HomeLayout(pages = listOf(listOf(a, b)))
        assertEquals(layout, HomeLayoutOps.moveShortcut(layout, 0, a, -1))
    }

    @Test
    fun removePage_keepsAtLeastOnePage() {
        val layout = HomeLayout()
        assertEquals(layout, HomeLayoutOps.removePage(layout, 0))
    }

    @Test
    fun addPage_respectsLimit() {
        var layout = HomeLayout()
        repeat(HomeLayoutOps.MAX_PAGES + 3) { layout = HomeLayoutOps.addPage(layout) }
        assertEquals(HomeLayoutOps.MAX_PAGES, layout.pages.size)
    }

    @Test
    fun movePage_swapsPages() {
        val layout = HomeLayout(pages = listOf(listOf(a), listOf(b)))
        val moved = HomeLayoutOps.movePage(layout, 0, 1)
        assertEquals(listOf(listOf(b), listOf(a)), moved.pages)
    }

    @Test
    fun defaultPage_followsMovedPage() {
        assertEquals(1, HomeLayoutOps.defaultPageAfterMove(0, 0, 1))
        assertEquals(0, HomeLayoutOps.defaultPageAfterMove(1, 0, 1))
        assertEquals(2, HomeLayoutOps.defaultPageAfterMove(2, 0, 1))
    }

    @Test
    fun defaultPage_afterRemove() {
        assertEquals(1, HomeLayoutOps.defaultPageAfterRemove(2, 0, 2))
        assertEquals(0, HomeLayoutOps.defaultPageAfterRemove(1, 1, 2))
        assertEquals(1, HomeLayoutOps.defaultPageAfterRemove(1, 2, 3))
    }

    @Test
    fun dock_addRemoveMove() {
        var layout = HomeLayoutOps.addDock(HomeLayout(), a)
        layout = HomeLayoutOps.addDock(layout, b)
        layout = HomeLayoutOps.moveDock(layout, a, 1)
        assertEquals(listOf(b, a), layout.dock)
        layout = HomeLayoutOps.removeDock(layout, b)
        assertEquals(listOf(a), layout.dock)
    }
}
