package com.example.stepcounterwef

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class StartupOnReboot: BroadcastReceiver(){
    override fun onReceive(context: Context, intent: Intent) {
        Data.init(context.filesDir)
        context.startService(Intent(context, StepCounter::class.java))
    }
}