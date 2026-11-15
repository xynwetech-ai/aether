package com.aethlauncher.kyn

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.aethlauncher.kyn.core.utils.AetherLog
import com.aethlauncher.kyn.ui.AetherRoot

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        AetherLog.i("MainActivity dibuat")

        val container = (application as AetherApp).container
        setContent {
            AetherRoot(container)
        }
    }
}
