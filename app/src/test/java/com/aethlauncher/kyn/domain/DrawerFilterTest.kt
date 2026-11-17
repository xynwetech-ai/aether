package com.aethlauncher.kyn.domain

import com.aethlauncher.kyn.core.preferences.DrawerSort
import com.aethlauncher.kyn.data.models.ShortcutRef
import com.aethlauncher.kyn.domain.usecase.DrawerFilter
import com.aethlauncher.kyn.launcher.resolver.LaunchableApp
import org.junit.Assert.assertEquals
import org.junit.Test

class DrawerFilterTest {

    private fun app(label: String, pkg: String, installTime: Long = 0L) =
        LaunchableApp(label, ShortcutRef(pkg, "$pkg.Main"), installTime)

    private val apps = listOf(
        app("Kamera", "p.camera", 30),
        app("Chrome", "p.chrome", 10),
        app("WhatsApp", "p.wa", 20),
        app("Game Seru", "p.game", 40)
    )

    private val categoryOf: (LaunchableApp) -> String = { if (it.ref.packageName == "p.game") "auto:games" else "auto:other" }

    @Test
    fun hiddenApps_neverAppear_evenInSearch() {
        val hidden = setOf(apps[1].ref.key)
        val all = DrawerFilter.filter(apps, hidden, "", null, categoryOf, DrawerSort.ALPHABETICAL)
        val searched = DrawerFilter.filter(apps, hidden, "chrome", null, categoryOf, DrawerSort.ALPHABETICAL)
        assertEquals(3, all.size)
        assertEquals(emptyList<LaunchableApp>(), searched)
    }

    @Test
    fun search_isCaseInsensitiveAndPartial() {
        val result = DrawerFilter.filter(apps, emptySet(), "APP", null, categoryOf, DrawerSort.ALPHABETICAL)
        assertEquals(listOf("WhatsApp"), result.map { it.label })
    }

    @Test
    fun category_filtersWhenNoQuery_butSearchIgnoresCategory() {
        val inCategory = DrawerFilter.filter(apps, emptySet(), "", "auto:games", categoryOf, DrawerSort.ALPHABETICAL)
        val searching = DrawerFilter.filter(apps, emptySet(), "kamera", "auto:games", categoryOf, DrawerSort.ALPHABETICAL)
        assertEquals(listOf("Game Seru"), inCategory.map { it.label })
        assertEquals(listOf("Kamera"), searching.map { it.label })
    }

    @Test
    fun sorting_alphabeticalAndRecent() {
        val az = DrawerFilter.filter(apps, emptySet(), "", null, categoryOf, DrawerSort.ALPHABETICAL)
        val recent = DrawerFilter.filter(apps, emptySet(), "", null, categoryOf, DrawerSort.RECENTLY_INSTALLED)
        assertEquals(listOf("Chrome", "Game Seru", "Kamera", "WhatsApp"), az.map { it.label })
        assertEquals(listOf("Game Seru", "Kamera", "WhatsApp", "Chrome"), recent.map { it.label })
    }
}
