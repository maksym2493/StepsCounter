package com.example.stepcounterwef

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat

class StartupOnReboot: BroadcastReceiver(){
    override fun onReceive(context: Context, intent: Intent) {
        if(ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED){
            Data.init(context.filesDir)
            ContextCompat.startForegroundService(context, Intent(context, StepCounter::class.java))
        }
    }
}
