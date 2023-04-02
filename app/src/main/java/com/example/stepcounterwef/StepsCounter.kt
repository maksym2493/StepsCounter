package com.example.stepcounterwef

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.os.PowerManager.WakeLock
import androidx.core.app.NotificationCompat
import com.example.stepcounterwef.Tools.Companion.notify
import com.example.stepcounterwef.Tools.Companion.pow
import com.example.stepcounterwef.Tools.Companion.rewriteDigit
import com.example.stepcounterwef.Tools.Companion.round
import java.io.File
import java.lang.Thread.sleep
import java.util.*

class StepCounter: Service(), SensorEventListener, Runnable{
    private var active = false
    private var progress: Int? = null
    private var stepsCountCur: Int? = null
    private var stepsCountPrev: Int? = null

    lateinit private var f: File
    lateinit private var notification: NotificationCompat.Builder

    lateinit private var stepCounterSensor: Sensor
    lateinit private var sensorManager: SensorManager

    lateinit private var wakeLock: WakeLock

    override fun onCreate() {
        super.onCreate()

        Data.stepCounter = this

        read()

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        var powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "StepCounter::WakeLockTag")

        wakeLock.acquire()
        createNotification()
    }

    override fun run(){
        while(active){
            if(Data.stats.checkUpdate()){ updateNotification() }

            var endTime = System.currentTimeMillis() + getDelay()
            while(endTime > System.currentTimeMillis() && active){ sleep(500) }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if(!active){ active = true; Thread(this).start(); sensorManager.registerListener(this, stepCounterSensor, SensorManager.SENSOR_DELAY_NORMAL) }

        return START_STICKY
    }

    override fun onDestroy() {
        active = false
        Data.stepCounter = null
        sensorManager.unregisterListener(this, stepCounterSensor)

        wakeLock.release()

        super.onDestroy()
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    fun createNotification(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val channel = NotificationChannel(
                "DailyTarget",
                "Денна ціль",
                NotificationManager.IMPORTANCE_DEFAULT
            )

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        var intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP

        notification = NotificationCompat.Builder(this, "DailyTarget")
            .setSound(null)
            .setOngoing(true)
            .setContentTitle("Денна ціль")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_IMMUTABLE))

        updateNotification()
    }

    fun updateNotification(){
        val count = Data.stats.getCount(0, 0)
        val target = Data.stats.getTarget(0, 0)
        val percent = (count / target) * 100

        checkProgress(percent)
        notification.setContentText("Прогрес: " + rewriteDigit(count) + " з " + rewriteDigit(target.toInt()) + " [ " + round(percent) + "% ]")

        startForeground(1, notification.build())
    }

    fun checkProgress(percent: Float){
        var update = false
        if(progress == null){ progress = 1 }

        val newProgress = (percent / 25).toInt() + 1; if(newProgress < progress!!){ progress = newProgress; update = true }

        while(progress != 5 && percent >= progress!! * 25){
            if(progress != 4){ notify("ProgressNotifications", "Денний прогрес", "Нова мітка!", "Пройдено " + (progress!! * 25).toString() + "% денної цілі!") } else{
                notify("ProgressNotifications", "Денний прогрес", "Нова мітка!", "Досягнена денна ціль!")
            }

            progress = progress!! + 1; if(!update){ update = true }
        }

        if(update){ write() }
    }

    override fun onSensorChanged(event: SensorEvent) {
        stepsCountCur = event.values[0].toInt()
        if(stepsCountPrev == null){ stepsCountPrev = stepsCountCur; write() } else{
            if(stepsCountCur!! < stepsCountPrev!!){ stepsCountPrev = 0 }

            var delta = stepsCountCur!! - stepsCountPrev!!

            if(delta != 0){
                stepsCountPrev = stepsCountCur

                Data.lvl.add(delta)
                Data.stats.add(delta)

                updateNotification()

                write()

                if(Data.main != null){
                    if(Data.stat != null){ if(Data.stat!!.isActive()){ Data.stat!!.start() } } else{
                        if(Data.main!!.isActive()){ Data.main!!.start() }
                    }
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Do nothing here
    }

    fun read(){
        f = File(filesDir, "Data/service.txt")
        if(f.exists()){
            val args = f.readText().split(" ")

            stepsCountCur = args[0].toInt()
            stepsCountPrev = args[0].toInt()
            progress = args[1].toInt()
        }
    }

    fun write(){ f.writeText(stepsCountCur.toString() + " " + progress.toString()) }

    fun getDelay(): Long{
        var i = 1
        var delay = 0L
        Date().toString().split(" ")[3].split(":").subList(1, 3).forEach{ delay += it.toLong() * pow(60, i--) * 1000 }

        var maxValue = (60 * 30 * 1000L)
        return maxValue - delay % maxValue
    }
}
