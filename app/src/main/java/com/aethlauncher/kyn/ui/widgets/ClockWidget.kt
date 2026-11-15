package com.aethlauncher.kyn.ui.widgets

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.text.format.DateFormat as AndroidDateFormat
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import java.util.Date

/**
 * Jam digital. Diperbarui lewat broadcast sistem (tick per menit), tanpa polling
 * dan tanpa animasi yang berjalan terus.
 */
@Composable
fun ClockWidget(
    showDate: Boolean,
    color: Color,
    textStyle: TextStyle,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }

    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(receiverContext: Context?, intent: Intent?) {
                now = System.currentTimeMillis()
            }
        }
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_TIME_TICK)
            addAction(Intent.ACTION_TIME_CHANGED)
            addAction(Intent.ACTION_TIMEZONE_CHANGED)
        }
        ContextCompat.registerReceiver(context, receiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
        now = System.currentTimeMillis()
        onDispose { context.unregisterReceiver(receiver) }
    }

    val date = Date(now)
    val timeText = AndroidDateFormat.getTimeFormat(context).format(date)
    val dateText = AndroidDateFormat.getLongDateFormat(context).format(date)

    Column(modifier = modifier) {
        Text(
            text = timeText,
            color = color,
            fontSize = 56.sp,
            fontWeight = FontWeight.Light,
            style = textStyle
        )
        if (showDate) {
            Text(
                text = dateText,
                color = color.copy(alpha = 0.9f),
                fontSize = 16.sp,
                style = textStyle
            )
        }
    }
}
