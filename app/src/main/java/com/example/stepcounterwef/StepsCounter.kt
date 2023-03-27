package com.example.stepcounterwef

import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.IBinder
import android.widget.Toast

class StepsCounter: Service(){
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

    private var stepsCountCur = 0
    private var stepsCountPrev = 0

    companion object{
        lateinit var stats: Stats
        lateinit var context: MainActivity
    }

    private val stepSensorListener = object: SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            if (event?.sensor?.type == Sensor.TYPE_STEP_COUNTER) {
                stepsCountCur = event.values[0].toInt()
                if(stepsCountPrev == 0){ stepsCountPrev = stepsCountCur } else{
                    stats.add(stepsCountCur - stepsCountPrev)
                    stepsCountPrev = stepsCountCur
                }

                Toast.makeText(context, stepsCountCur.toString(), Toast.LENGTH_SHORT).show()
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
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }
}