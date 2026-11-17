package com.aethlauncher.kyn.domain.usecase

import android.content.pm.ApplicationInfo

enum class AppCategory(val id: String, val label: String) {
    COMMUNICATION("auto:communication", "Komunikasi"),
    INTERNET("auto:internet", "Internet"),
    GAMES("auto:games", "Game"),
    MEDIA("auto:media", "Media"),
    PRODUCTIVITY("auto:productivity", "Produktivitas"),
    TOOLS("auto:tools", "Alat"),
    SYSTEM("auto:system", "Sistem"),
    OTHER("auto:other", "Lainnya");

    companion object {
        fun fromId(id: String): AppCategory? = entries.firstOrNull { it.id == id }
    }
}

/**
 * Kategori otomatis. Tidak hanya mengandalkan ApplicationInfo.category (sering kosong):
 * jika kosong dipakai kata kunci dari nama package, lalu flag aplikasi sistem.
 */
object AppCategorizer {

    private val communication = listOf(
        "whatsapp", "telegram", "messenger", "message", "sms", "dialer", "contacts", "mail",
        "signal", "discord", "viber", "skype", "zoom", "teams", "meet", "kakao", "jp.naver.line",
        "facebook", "instagram", "twitter", "threads"
    )
    private val internet = listOf(
        "browser", "chrome", "firefox", "opera", "brave", "vivaldi", "duckduckgo", "emmx"
    )
    private val games = listOf(
        ".game", "games", "minecraft", "roblox", "supercell", "mojang", "gameloft",
        "pubg", "moonton", "garena", "freefire"
    )
    private val media = listOf(
        "music", "spotify", "video", "youtube", "netflix", "player", "camera", "gallery",
        "photos", "tiktok", "podcast", "radio", "vlc", "soundcloud"
    )
    private val productivity = listOf(
        "docs", "sheets", "slides", "office", "calendar", "notes", "keep", "drive", "pdf",
        "reader", "onenote", "todo", "evernote", "notion"
    )
    private val tools = listOf(
        "calculator", "clock", "weather", "flashlight", "torch", "files", "filemanager",
        "documentsui", "recorder", "compass", "vpn", "translate", "scanner", "cleaner", "battery"
    )
    private val system = listOf(
        "settings", "systemui", "packageinstaller", "permissioncontroller", "launcher",
        "setupwizard", "wallpaper"
    )

    fun categorize(packageName: String, platformCategory: Int, isSystem: Boolean): AppCategory {
        when (platformCategory) {
            ApplicationInfo.CATEGORY_GAME -> return AppCategory.GAMES
            ApplicationInfo.CATEGORY_AUDIO,
            ApplicationInfo.CATEGORY_VIDEO,
            ApplicationInfo.CATEGORY_IMAGE -> return AppCategory.MEDIA
            ApplicationInfo.CATEGORY_SOCIAL -> return AppCategory.COMMUNICATION
            ApplicationInfo.CATEGORY_NEWS -> return AppCategory.INTERNET
            ApplicationInfo.CATEGORY_MAPS -> return AppCategory.TOOLS
            ApplicationInfo.CATEGORY_PRODUCTIVITY -> return AppCategory.PRODUCTIVITY
        }

        val pkg = packageName.lowercase()
        fun matches(keywords: List<String>) = keywords.any { pkg.contains(it) }

        return when {
            matches(communication) -> AppCategory.COMMUNICATION
            matches(internet) -> AppCategory.INTERNET
            matches(games) -> AppCategory.GAMES
            matches(media) -> AppCategory.MEDIA
            matches(productivity) -> AppCategory.PRODUCTIVITY
            matches(tools) -> AppCategory.TOOLS
            matches(system) -> AppCategory.SYSTEM
            isSystem -> AppCategory.SYSTEM
            else -> AppCategory.OTHER
        }
    }
}
