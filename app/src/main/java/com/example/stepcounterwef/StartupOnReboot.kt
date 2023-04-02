package com.example.stepcounterwef

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.PowerManager
import androidx.core.content.ContextCompat

class StartupOnReboot: BroadcastReceiver(){
    override fun onReceive(context: Context, intent: Intent) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        var channelPermission = false
        val notificationPermission = notificationManager.areNotificationsEnabled()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val channel = notificationManager.getNotificationChannel("DailyTarget")
            channelPermission = (channel != null && channel.importance != NotificationManager.IMPORTANCE_NONE) || channel == null
        }
        
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager

        if(!powerManager.isIgnoringBatteryOptimizations(context.packageName) && notificationPermission && channelPermission
           && ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED){
            Data.init(context.filesDir)
            ContextCompat.startForegroundService(context, Intent(context, StepCounter::class.java))
        }
    }
}
