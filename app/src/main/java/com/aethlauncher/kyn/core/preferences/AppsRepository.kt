package com.aethlauncher.kyn.core.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

data class CustomCategory(val id: String, val name: String)

/** Preferensi per aplikasi. Semua key berformat "package/class". */
data class AppPrefs(
    val hidden: Set<String> = emptySet(),
    val locked: Set<String> = emptySet(),
    val categoryOverrides: Map<String, String> = emptyMap(),
    val customCategories: List<CustomCategory> = emptyList()
)

/** Format teks untuk kategori dan override. Baris rusak dilewati. */
object AppPrefsCodec {
    private const val MAX_NAME = 24

    fun encodeCategories(list: List<CustomCategory>): String =
        list.joinToString("\n") { "${it.id}\t${cleanName(it.name)}" }

    fun decodeCategories(raw: String?): List<CustomCategory> =
        raw.orEmpty().split("\n").mapNotNull { line ->
            val parts = line.split("\t")
            if (parts.size == 2 && parts[0].isNotBlank() && parts[1].isNotBlank()) {
                CustomCategory(parts[0], parts[1])
            } else {
                null
            }
        }

    fun encodeOverrides(map: Map<String, String>): String =
        map.entries.joinToString("\n") { "${it.key}\t${it.value}" }

    fun decodeOverrides(raw: String?): Map<String, String> =
        raw.orEmpty().split("\n").mapNotNull { line ->
            val parts = line.split("\t")
            if (parts.size == 2 && parts[0].isNotBlank() && parts[1].isNotBlank()) parts[0] to parts[1] else null
        }.toMap()

    fun cleanName(name: String): String =
        name.replace('\n', ' ').replace('\t', ' ').trim().take(MAX_NAME)
}

private val Context.appsDataStore: DataStore<Preferences> by preferencesDataStore(name = "aether_apps")

class AppsRepository(context: Context) {

    private val dataStore = context.applicationContext.appsDataStore

    val prefs: Flow<AppPrefs> = dataStore.data
        .catch { error ->
            if (error is IOException) emit(emptyPreferences()) else throw error
        }
        .map { p ->
            AppPrefs(
                hidden = p[HIDDEN] ?: emptySet(),
                locked = p[LOCKED] ?: emptySet(),
                categoryOverrides = AppPrefsCodec.decodeOverrides(p[OVERRIDES]),
                customCategories = AppPrefsCodec.decodeCategories(p[CATEGORIES])
            )
        }

    suspend fun update(transform: (AppPrefs) -> AppPrefs) {
        dataStore.edit { p ->
            val current = AppPrefs(
                hidden = p[HIDDEN] ?: emptySet(),
                locked = p[LOCKED] ?: emptySet(),
                categoryOverrides = AppPrefsCodec.decodeOverrides(p[OVERRIDES]),
                customCategories = AppPrefsCodec.decodeCategories(p[CATEGORIES])
            )
            val next = transform(current)
            p[HIDDEN] = next.hidden
            p[LOCKED] = next.locked
            p[OVERRIDES] = AppPrefsCodec.encodeOverrides(next.categoryOverrides)
            p[CATEGORIES] = AppPrefsCodec.encodeCategories(next.customCategories)
        }
    }

    suspend fun reset() {
        dataStore.edit { it.clear() }
    }

    private companion object {
        val HIDDEN = stringSetPreferencesKey("hidden_apps")
        val LOCKED = stringSetPreferencesKey("locked_apps")
        val OVERRIDES = stringPreferencesKey("category_overrides")
        val CATEGORIES = stringPreferencesKey("custom_categories")
    }
}
