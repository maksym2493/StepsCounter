package com.example.stepcounterwef

import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import android.widget.Toast
import java.io.File
import java.util.*

class StepsCounter: Service(){
    private var f = File(context.filesDir, "data/service.txt")
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

    private var stepsCountCur = 0
    private var stepsCountPrev = 0

    companion object{
        lateinit var lvl: Level
        lateinit var stats: Stats
        lateinit var context: MainActivity
    }

    init{
        if(f.exists()){
            val args = f.readText().split(" ")

            stepsCountCur = args[0].toInt()
            stepsCountPrev = args[1].toInt()
        }
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
                        File(externalCacheDir, "log.txt").writeText(Date().toString())
                        if(MainActivity.r){ context.start()} else{ if(Stat.r){ MainActivity.stat!!.update() } }
                    }
                }
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            // Do nothing here
        }
    }

    override fun onCreate() {
        super.onCreate()

        Toast.makeText(context, "Service was started!", Toast.LENGTH_SHORT).show()
        sensorManager.registerListener(stepSensorListener, stepCounterSensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(stepSensorListener, stepCounterSensor)

        Toast.makeText(context, "Service was destroyed!", Toast.LENGTH_SHORT).show()
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){ startForegroundService(Intent(this, StepsCounter::class.java)) }
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }
}