package com.aethlauncher.kyn.ui.drawer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aethlauncher.kyn.core.AppContainer
import com.aethlauncher.kyn.core.preferences.CustomCategory
import com.aethlauncher.kyn.core.preferences.DrawerSort
import com.aethlauncher.kyn.domain.usecase.AppCategorizer
import com.aethlauncher.kyn.domain.usecase.AppCategory
import com.aethlauncher.kyn.domain.usecase.DrawerFilter
import com.aethlauncher.kyn.launcher.resolver.LaunchableApp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Satu pilihan kategori. [id] null berarti "Semua". */
data class CategoryOption(val id: String?, val label: String)

data class DrawerUiState(
    val apps: List<LaunchableApp> = emptyList(),
    val categories: List<CategoryOption> = listOf(CategoryOption(null, "Semua")),
    val moveTargets: List<CategoryOption> = emptyList(),
    val selected: String? = null,
    val query: String = "",
    val loaded: Boolean = false,
    val columns: Int = 4,
    val sort: DrawerSort = DrawerSort.ALPHABETICAL
)

class DrawerViewModel(private val container: AppContainer) : ViewModel() {

    private val query = MutableStateFlow("")
    private val selected = MutableStateFlow<String?>(null)

    val state: StateFlow<DrawerUiState> = combine(
        container.appsStore.apps,
        container.appsRepository.prefs,
        container.settingsRepository.settings,
        query,
        selected
    ) { apps, prefs, settings, q, sel ->
        val validCustom: List<CustomCategory> = prefs.customCategories
        val categoryOf: (LaunchableApp) -> String = { app ->
            val override = prefs.categoryOverrides[app.ref.key]
            if (override != null && (override.startsWith("auto:") || validCustom.any { it.id == override })) {
                override
            } else {
                AppCategorizer.categorize(app.ref.packageName, app.platformCategory, app.isSystem).id
            }
        }

        val visible = apps.filter { it.ref.key !in prefs.hidden }
        val autoPresent = AppCategory.entries.filter { category ->
            visible.any { categoryOf(it) == category.id }
        }
        val options = listOf(CategoryOption(null, "Semua")) +
            autoPresent.map { CategoryOption(it.id, it.label) } +
            validCustom.map { CategoryOption(it.id, it.name) }
        val effectiveSelected = sel?.takeIf { id -> options.any { it.id == id } }

        DrawerUiState(
            apps = DrawerFilter.filter(apps, prefs.hidden, q, effectiveSelected, categoryOf, settings.drawerSort),
            categories = options,
            moveTargets = AppCategory.entries.map { CategoryOption(it.id, it.label) } +
                validCustom.map { CategoryOption(it.id, it.name) },
            selected = effectiveSelected,
            query = q,
            loaded = container.appsStore.loaded.value || apps.isNotEmpty(),
            columns = settings.drawerColumns,
            sort = settings.drawerSort
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DrawerUiState())

    fun setQuery(value: String) {
        query.value = value
    }

    fun selectCategory(id: String?) {
        selected.value = id
    }

    /** [categoryId] null mengembalikan aplikasi ke kategori otomatis. */
    fun moveToCategory(app: LaunchableApp, categoryId: String?) {
        viewModelScope.launch {
            container.appsRepository.update { prefs ->
                val overrides = prefs.categoryOverrides.toMutableMap()
                if (categoryId == null) overrides.remove(app.ref.key) else overrides[app.ref.key] = categoryId
                prefs.copy(categoryOverrides = overrides)
            }
        }
    }

    fun hide(app: LaunchableApp) {
        viewModelScope.launch {
            container.appsRepository.update { it.copy(hidden = it.hidden + app.ref.key) }
        }
    }
}
