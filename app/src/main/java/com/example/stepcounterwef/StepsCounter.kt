package com.example.stepcounterwef

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.IBinder
import java.io.File

class StepsCounter(): Service(){
    lateinit private var f: File
    lateinit private var stepCounterSensor: Sensor
    lateinit private var sensorManager: SensorManager

    lateinit private var alarmManager: AlarmManager
    lateinit private var alarmIntent: PendingIntent

    lateinit private var lvl: Level
    lateinit private var stats: Stats

    private var stepsCountCur = 0
    private var stepsCountPrev = 0

    private var background = false

    constructor(background: Boolean = false): this(){ this.background = background }

    override fun onCreate() {
        super.onCreate()

        lvl = Level(filesDir)
        stats = Stats(filesDir)

        f = File(filesDir, "data/service.txt")
        if(f.exists()){
            val args = f.readText().split(" ")

            stepsCountCur = args[0].toInt()
            stepsCountPrev = args[1].toInt()
        }

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        var intent = Intent(this, StepsCounter::class.java)
        alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmIntent = PendingIntent.getService(this, 0, intent, 0)

        startService(intent)
    }

    fun write(){ f.writeText(stepsCountCur.toString() + " " + stepsCountPrev.toString()) }

    private val stepSensorListener = object: SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            if (event?.sensor?.type == Sensor.TYPE_STEP_COUNTER) {
                stepsCountCur = event.values[0].toInt()
                if(stepsCountPrev == 0){ stepsCountPrev = stepsCountCur; write() } else{
                    if(stepsCountCur < stepsCountPrev){ stepsCountPrev = 0 }

                    var delta = stepsCountCur - stepsCountPrev

                    if(delta != 0){
                        lvl.add(delta)
                        stats.add(delta)
                        stats.cheackUpdate()
                        stepsCountPrev = stepsCountCur

                        write()
                    }
                }
            }

            if(background){ sensorManager.unregisterListener(this, stepCounterSensor) }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            // Do nothing here
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int{
        if(background){ sheduleNext() }
        sensorManager.registerListener(stepSensorListener, stepCounterSensor, SensorManager.SENSOR_DELAY_NORMAL)

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        System.out.println("Service was destroyed!")
        sensorManager.unregisterListener(stepSensorListener, stepCounterSensor)
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    fun startInBackground(){
        sheduleNext()
        background = true
        sensorManager.unregisterListener(stepSensorListener, stepCounterSensor)
    }

    fun stopInBackGround(){
        background = false
        alarmManager.cancel(alarmIntent)
        sensorManager.registerListener(stepSensorListener, stepCounterSensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    fun sheduleNext(){
        val interval = 10 * 1000
        val triggerAtMillis = System.currentTimeMillis() + interval

        alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtMillis, alarmIntent)
    }
}