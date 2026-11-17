package com.aethlauncher.kyn.domain

import android.content.pm.ApplicationInfo
import com.aethlauncher.kyn.domain.usecase.AppCategorizer
import com.aethlauncher.kyn.domain.usecase.AppCategory
import org.junit.Assert.assertEquals
import org.junit.Test

class AppCategorizerTest {

    private fun cat(pkg: String, platform: Int = -1, system: Boolean = false) =
        AppCategorizer.categorize(pkg, platform, system)

    @Test
    fun platformCategory_winsFirst() {
        assertEquals(AppCategory.GAMES, cat("com.example.app", ApplicationInfo.CATEGORY_GAME))
        assertEquals(AppCategory.MEDIA, cat("com.example.app", ApplicationInfo.CATEGORY_VIDEO))
        assertEquals(AppCategory.COMMUNICATION, cat("com.example.app", ApplicationInfo.CATEGORY_SOCIAL))
    }

    @Test
    fun keywords_workWhenPlatformCategoryIsMissing() {
        assertEquals(AppCategory.COMMUNICATION, cat("com.whatsapp"))
        assertEquals(AppCategory.INTERNET, cat("com.android.chrome"))
        assertEquals(AppCategory.MEDIA, cat("com.spotify.music"))
        assertEquals(AppCategory.TOOLS, cat("com.google.android.calculator"))
        assertEquals(AppCategory.PRODUCTIVITY, cat("com.google.android.apps.docs"))
    }

    @Test
    fun systemApps_fallToSystem() {
        assertEquals(AppCategory.SYSTEM, cat("com.android.settings", system = true))
        assertEquals(AppCategory.SYSTEM, cat("com.vendor.unknownthing", system = true))
    }

    @Test
    fun unknownUserApp_isOther() {
        assertEquals(AppCategory.OTHER, cat("com.random.thing"))
    }

    @Test
    fun fromId_roundTrips() {
        AppCategory.entries.forEach { assertEquals(it, AppCategory.fromId(it.id)) }
    }
}
