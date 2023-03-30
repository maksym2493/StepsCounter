package com.example.stepcounterwef

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat

class StartupOnReboot: BroadcastReceiver(){
    override fun onReceive(context: Context, intent: Intent) {
        Data.init(context.filesDir)
        ContextCompat.startForegroundService(context, Intent(context, StepCounter::class.java))
    }
}