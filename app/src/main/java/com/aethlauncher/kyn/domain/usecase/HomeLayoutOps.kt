package com.aethlauncher.kyn.domain.usecase

import com.aethlauncher.kyn.data.models.HomeLayout
import com.aethlauncher.kyn.data.models.ShortcutRef

/** Operasi murni pada layout (tanpa Android) supaya mudah diuji unit test. */
object HomeLayoutOps {
    const val MAX_PAGES = 6

    fun addShortcut(layout: HomeLayout, page: Int, ref: ShortcutRef): HomeLayout {
        if (page !in layout.pages.indices) return layout
        val current = layout.pages[page]
        if (ref in current) return layout
        return layout.copy(pages = layout.pages.replaceAt(page, current + ref))
    }

    fun removeShortcut(layout: HomeLayout, page: Int, ref: ShortcutRef): HomeLayout {
        if (page !in layout.pages.indices) return layout
        return layout.copy(pages = layout.pages.replaceAt(page, layout.pages[page] - ref))
    }

    fun moveShortcut(layout: HomeLayout, page: Int, ref: ShortcutRef, delta: Int): HomeLayout {
        if (page !in layout.pages.indices) return layout
        return layout.copy(pages = layout.pages.replaceAt(page, layout.pages[page].moved(ref, delta)))
    }

    fun addDock(layout: HomeLayout, ref: ShortcutRef): HomeLayout {
        if (ref in layout.dock) return layout
        return layout.copy(dock = layout.dock + ref)
    }

    fun removeDock(layout: HomeLayout, ref: ShortcutRef): HomeLayout =
        layout.copy(dock = layout.dock - ref)

    fun moveDock(layout: HomeLayout, ref: ShortcutRef, delta: Int): HomeLayout =
        layout.copy(dock = layout.dock.moved(ref, delta))

    fun addPage(layout: HomeLayout): HomeLayout {
        if (layout.pages.size >= MAX_PAGES) return layout
        return layout.copy(pages = layout.pages + listOf(emptyList()))
    }

    fun removePage(layout: HomeLayout, index: Int): HomeLayout {
        if (layout.pages.size <= 1 || index !in layout.pages.indices) return layout
        return layout.copy(pages = layout.pages.filterIndexed { i, _ -> i != index })
    }

    fun movePage(layout: HomeLayout, index: Int, delta: Int): HomeLayout {
        val target = index + delta
        if (index !in layout.pages.indices || target !in layout.pages.indices) return layout
        val pages = layout.pages.toMutableList()
        pages[index] = layout.pages[target]
        pages[target] = layout.pages[index]
        return layout.copy(pages = pages)
    }

    fun defaultPageAfterRemove(defaultPage: Int, removedIndex: Int, newSize: Int): Int {
        val adjusted = when {
            removedIndex < defaultPage -> defaultPage - 1
            removedIndex == defaultPage -> 0
            else -> defaultPage
        }
        return adjusted.coerceIn(0, (newSize - 1).coerceAtLeast(0))
    }

    /** Dipakai setelah movePage berhasil menukar halaman [index] dengan tetangganya. */
    fun defaultPageAfterMove(defaultPage: Int, index: Int, delta: Int): Int = when (defaultPage) {
        index -> index + delta
        index + delta -> index
        else -> defaultPage
    }

    private fun <T> List<T>.replaceAt(index: Int, value: T): List<T> =
        mapIndexed { i, item -> if (i == index) value else item }

    private fun <T> List<T>.moved(item: T, delta: Int): List<T> {
        val from = indexOf(item)
        if (from < 0) return this
        val to = from + delta
        if (to !in indices) return this
        val result = toMutableList()
        result[from] = this[to]
        result[to] = this[from]
        return result
    }
}
