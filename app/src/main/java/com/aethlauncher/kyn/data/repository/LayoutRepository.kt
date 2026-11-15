package com.aethlauncher.kyn.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.aethlauncher.kyn.data.local.HomeLayoutCodec
import com.aethlauncher.kyn.data.models.HomeLayout
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.layoutDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "aether_layout"
)

class LayoutRepository(context: Context) {

    private val dataStore = context.applicationContext.layoutDataStore

    val layout: Flow<HomeLayout> = dataStore.data
        .catch { error ->
            if (error is IOException) emit(emptyPreferences()) else throw error
        }
        .map { prefs -> prefs.toLayout() }

    suspend fun update(transform: (HomeLayout) -> HomeLayout) {
        dataStore.edit { prefs ->
            val next = transform(prefs.toLayout())
            prefs[PAGES] = HomeLayoutCodec.encodePages(next.pages)
            prefs[DOCK] = HomeLayoutCodec.encodeRefs(next.dock)
        }
    }

    private fun Preferences.toLayout(): HomeLayout = HomeLayout(
        pages = HomeLayoutCodec.decodePages(this[PAGES]),
        dock = HomeLayoutCodec.decodeRefs(this[DOCK])
    )

    private companion object {
        val PAGES = stringPreferencesKey("pages")
        val DOCK = stringPreferencesKey("dock")
    }
}
