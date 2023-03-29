package com.example.stepcounterwef

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.stepcounterwef.Tools.Companion.round
import java.io.File

class StepCounter: Service(), SensorEventListener{
    private var active = false
    private var stepsCountCur: Int? = null
    private var stepsCountPrev: Int? = null

    lateinit private var f: File
    lateinit var notification: Notification
    lateinit private var stepCounterSensor: Sensor
    lateinit private var sensorManager: SensorManager

    override fun onCreate() {
        read()

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(1, createNotification())
        if(!active){ active = true; sensorManager.registerListener(this, stepCounterSensor, SensorManager.SENSOR_DELAY_NORMAL) }

        return START_STICKY
    }

    override fun onDestroy() {
        active = false
        sensorManager.unregisterListener(this, stepCounterSensor)
        super.onDestroy()
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    fun createNotification(): Notification{
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val channel = NotificationChannel(
                "StepCounterChannel",
                "StepCounterChannelName",
                NotificationManager.IMPORTANCE_DEFAULT
            )

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        val count = Data.stats.getCount(0, 0)
        val target = Data.stats.getTarget(0, 0)

        notification = NotificationCompat.Builder(this, "StepCounterChannel")
            .setContentTitle("Денна ціль")
            .setContentText("Пройдено крорків: " + count.toString() + " з " + target.toInt().toString() + " [ " + round((count / target) * 100) +"% ]")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()

        return notification
    }

    override fun onSensorChanged(event: SensorEvent) {
        stepsCountCur = event.values[0].toInt()
        if(stepsCountPrev == null){ stepsCountPrev = stepsCountCur; write() } else{
            if(stepsCountCur!! < stepsCountPrev!!){ stepsCountPrev = 0 }

            var delta = stepsCountCur!! - stepsCountPrev!!

            if(delta != 0){
                stepsCountPrev = stepsCountCur

                Data.stats.cheackUpdate()

                Data.lvl.add(delta)
                Data.stats.add(delta)

                startForeground(1, createNotification())

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
            stepsCountPrev = args[1].toInt()
        }
    }

    fun write(){ f.writeText(stepsCountCur.toString() + " " + stepsCountPrev.toString()) }
}