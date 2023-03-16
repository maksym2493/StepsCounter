package com.example.stepcounterwef

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.lang.System.exit

class MainActivity : AppCompatActivity(), SensorEventListener {
    private lateinit var stats: Stats
    private lateinit var level: Level
    private lateinit var counter: TextView
    private var sensorManager: SensorManager? = null

    // variable gives the running status
    private var running = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var dir = File(filesDir, "Data")
        if(!dir.exists()){ dir.mkdir() }

        level = Level(filesDir)
        stats = Stats(filesDir)

        Stat.data1 = stats
        Stat.data2 = arrayListOf(null, null)
        startActivity(Intent(this, Stat::class.java))
        return

        counter = findViewById<TextView>(R.id.counter)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) { ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACTIVITY_RECOGNITION), 100) } else{ start() }

        start()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) { start() } else{ finish(); exit(0) }
    }

    fun start(){
        var dir = File(filesDir, "Data")
        if(!dir.exists()){ dir.mkdir() }

        level = Level(filesDir)
        stats = Stats(filesDir)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensorManager?.registerListener(this, sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER), SensorManager.SENSOR_DELAY_UI)

        counter.text = stats.getCount(0, 0).toString()
        stats.write()
        Toast.makeText(this, "Step counter has been activated", Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        running = true
    }

    override fun onPause() {
        super.onPause()
        running = false
    }

    override fun onSensorChanged(event: SensorEvent?) {
        var stepsCount = stats.getStepsCount(event!!.values[0].toInt())

        //stats.cheackUpdate()
        stats.add(stepsCount)
        level.add(stepsCount)
        if (running) { counter.text = stats.getCount(0, 0).toString() }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        Toast.makeText(this, "onAccuracyChanged: Sensor: $sensor; accuracy: $accuracy", Toast.LENGTH_SHORT)
    }
}