package com.aethlauncher.kyn.launcher.manager

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import com.aethlauncher.kyn.core.utils.AetherLog

/** Aksi sistem yang tidak punya API resmi atau perlu intent. Semua mengembalikan false saat gagal. */
object SystemActions {

    /**
     * Membuka panel notifikasi. Android tidak menyediakan API publik untuk ini, jadi
     * dicoba lewat reflection dan bisa gagal di sebagian perangkat atau versi Android.
     */
    @SuppressLint("PrivateApi", "WrongConstant")
    fun expandNotifications(context: Context): Boolean = try {
        val service = context.getSystemService("statusbar")
        if (service == null) {
            false
        } else {
            Class.forName("android.app.StatusBarManager")
                .getMethod("expandNotificationsPanel")
                .invoke(service)
            true
        }
    } catch (e: Exception) {
        AetherLog.w("Panel notifikasi tidak bisa dibuka lewat Aether")
        false
    }

    fun openAppInfo(context: Context, packageName: String): Boolean = start(
        context,
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$packageName"))
    )

    fun webSearch(context: Context, query: String): Boolean = start(
        context,
        Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=" + Uri.encode(query)))
    )

    private fun start(context: Context, intent: Intent): Boolean = try {
        context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        true
    } catch (e: ActivityNotFoundException) {
        false
    } catch (e: SecurityException) {
        false
    }
}
