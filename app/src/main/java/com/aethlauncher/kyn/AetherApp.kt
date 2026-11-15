package com.aethlauncher.kyn

import android.app.Application
import com.aethlauncher.kyn.core.AppContainer
import com.aethlauncher.kyn.core.utils.AetherLog

class AetherApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        container.crashRecoveryManager.onApplicationCreate()
        AetherLog.i("Application dimulai")
    }
}
