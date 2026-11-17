package com.aethlauncher.kyn.domain.usecase

import com.aethlauncher.kyn.core.preferences.DrawerSort
import com.aethlauncher.kyn.launcher.resolver.LaunchableApp

/** Penyaringan dan pengurutan App Drawer. Murni, tanpa Android, mudah diuji. */
object DrawerFilter {

    /**
     * Aplikasi tersembunyi tidak pernah muncul, termasuk di hasil pencarian.
     * Saat pencarian aktif, kategori diabaikan supaya semua aplikasi bisa ditemukan.
     */
    fun filter(
        apps: List<LaunchableApp>,
        hidden: Set<String>,
        query: String,
        categoryId: String?,
        categoryOf: (LaunchableApp) -> String,
        sort: DrawerSort
    ): List<LaunchableApp> {
        val q = query.trim()
        val useCategory = categoryId != null && q.isEmpty()
        val filtered = apps.filter { app ->
            app.ref.key !in hidden &&
                (!useCategory || categoryOf(app) == categoryId) &&
                (q.isEmpty() || app.label.contains(q, ignoreCase = true))
        }
        return when (sort) {
            DrawerSort.ALPHABETICAL -> filtered.sortedBy { it.label.lowercase() }
            DrawerSort.RECENTLY_INSTALLED -> filtered.sortedWith(
                compareByDescending<LaunchableApp> { it.installTime }.thenBy { it.label.lowercase() }
            )
        }
    }
}
