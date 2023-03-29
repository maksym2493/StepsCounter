package com.example.stepcounterwef

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.stepcounterwef.Tools.Companion.getRandomColor
import com.example.stepcounterwef.Tools.Companion.rewriteDigit
import com.example.stepcounterwef.Tools.Companion.round
import com.example.stepcounterwef.databinding.ActivityMainBinding
import java.lang.System.exit
import java.util.*

class MainActivity: AppCompatActivity(){
    private var active = true

    private lateinit var binding: ActivityMainBinding

    companion object{
        var r = false
        private var windowSize: Int? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Data.main = this
        Data.init(filesDir)

        active = true
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) { ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACTIVITY_RECOGNITION), 100) } else{ startService(Intent(this, StepCounter::class.java)); start() }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) { startService(Intent(this, StepCounter::class.java)); start() } else{ finish(); exit(0) }
    }

    fun start(){
        Data.stats.cheackUpdate()
        binding = ActivityMainBinding.inflate(layoutInflater)
        windowSize = (windowManager.defaultDisplay.width - 20 * binding.diagram.layoutParams.height / 200f).toInt()

        setContentView(binding.root)

        with(binding){
            var expLevel = Data.lvl.get_exp()
            var progress = expLevel[0] / expLevel[1].toFloat()

            level.text = "Рівень: " +  rewriteDigit(Data.lvl.get_lvl())
            exp.text = rewriteDigit(expLevel[0]) + " з " + rewriteDigit(expLevel[1]) + " [ " + round(progress * 100) + " % ]"

            var width = (progress * windowSize!!).toInt()
            if(width != 0){ levelProgress.layoutParams.width = width; levelProgress.setBackgroundColor(Color.parseColor(getRandomColor())) } else{ levelProgress.visibility = View.INVISIBLE }

            showTargets()
            drawDiagram()

            showStatistic.setOnClickListener{
                startActivity(Intent(this@MainActivity, Stat::class.java))
            }

            if(!changeTarget.hasOnClickListeners()){
                changeTarget.setOnClickListener{
                    with(binding){
                        main.visibility = ConstraintLayout.GONE
                        changeTargetLayout.visibility = ConstraintLayout.VISIBLE

                        if(!confirm.hasOnClickListeners()){
                            confirm.setOnClickListener{
                                var end = false
                                if(newTarget.text.toString() == ""){ Toast.makeText(this@MainActivity, "Зміна скасована.", Toast.LENGTH_SHORT).show(); end = true } else{
                                    try{
                                        var target = newTarget.text.toString().toInt()
                                        if(target <= 0){ Toast.makeText(this@MainActivity, "Ціль не може дорівнювати нулю або бути меншою за нього.", Toast.LENGTH_SHORT).show() } else{
                                            if(target > 100000){ Toast.makeText(this@MainActivity, "Ціль не може більшо за 100 000.", Toast.LENGTH_SHORT).show() } else{
                                                if(target % 10 != 0){ Toast.makeText(this@MainActivity, "Ціль повинна націло ділитися на 10.", Toast.LENGTH_SHORT).show() } else{
                                                    if(Data.stats.getTarget(0, 0).toInt() == target){ Toast.makeText(this@MainActivity, "Вказана ціль є поточною.", Toast.LENGTH_SHORT).show() } else{
                                                        end = true
                                                        newTarget.setText("")
                                                        Data.stats.setTarget(target)
                                                        Toast.makeText(this@MainActivity, "Встановлена ціль в " + rewriteDigit(target) + " кроків.", Toast.LENGTH_SHORT).show()

                                                        start()
                                                    }
                                                }
                                            }
                                        }
                                    } catch(e: Exception){ Toast.makeText(this@MainActivity, "Введіть ціле число.", Toast.LENGTH_SHORT).show() }
                                }

                                if(end){
                                    main.visibility = ConstraintLayout.VISIBLE
                                    changeTargetLayout.visibility = ConstraintLayout.GONE
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        active = true
        start()

        super.onResume()
    }

    override fun onPause() {
        active = false

        super.onPause()
    }

    override fun onDestroy() {
        active = false
        Data.main = null

        super.onDestroy()
    }

    fun getDelay(): Long{ return (60 - Date().toString().split(" ")[3].split(":")[2].toLong()) * 1000 }

    fun isActive(): Boolean{ return active }

    fun drawDiagram(){
        with(binding){
            var counts = Data.stats.get_last_counts()

            var maxValue = 0f
            var totalCount = 0f

            var i = counts.size - 1
            do{ totalCount += counts[i]; if(counts[i] > maxValue){ maxValue = counts[i].toFloat() } } while(--i != -1)

            averageAndMax.text = "Максимум кроків: " + maxValue.toInt().toString() + ".\nСередня кількість: " + round(totalCount / counts.size.toFloat()) + "."

            i = 0
            if(maxValue == 0f){ maxValue = 1f }
            do{
                var view = diagram.getChildAt(i)

                if(counts.size != 0){
                    var height = ((counts.removeAt(0) / maxValue) * diagram.layoutParams.height).toInt()

                    if(height != 0){
                        view.setBackgroundColor(Color.parseColor(getRandomColor()))
                        if(view.visibility == View.INVISIBLE){ view.visibility = View.VISIBLE }
                        if(height < windowSize!!){ view.layoutParams.height = height } else{ view.layoutParams.height = windowSize!! }
                    } else{ view.visibility = View.INVISIBLE }
                } else{ view.visibility = View.INVISIBLE }
            } while(++i != 24)
        }
    }

    fun showTargets(){
        with(binding){
            var targets = arrayOf(
                Data.stats.getTarget(0, 0),
                Data.stats.getTarget(0),
                Data.stats.getTarget()
            )

            var stepsCount = listOf(
                Data.stats.getCount(0, 0),
                Data.stats.getCount(0),
                Data.stats.getCount()
            )

            var i = 2
            var listOfTextViews = listOf(daylyExp, monthlyExp, totalExp)
            var listOfViews = listOf(daylyProgress, monthlyProgress, totalProgress)

            do{
                listOfTextViews[i].text =  rewriteDigit(stepsCount[i]) + " з " +  rewriteDigit(targets[i].toInt())

                var width = ((stepsCount[i] / targets[i]) * windowSize!!).toInt()
                if(width != 0){
                    if(width < windowSize!!){ listOfViews[i].layoutParams.width = width } else{ listOfViews[i].layoutParams.width = windowSize!! }
                    listOfViews[i].setBackgroundColor(Color.parseColor(getRandomColor()))
                } else{ listOfViews[i].visibility = View.INVISIBLE }
            } while(--i != -1)
        }
    }

    override fun onBackPressed() {
        with(binding){
            if(main.visibility == ConstraintLayout.GONE){
                main.visibility = ConstraintLayout.VISIBLE
                changeTargetLayout.visibility = ConstraintLayout.GONE

                Toast.makeText(this@MainActivity, "Зміна скасована.", Toast.LENGTH_SHORT).show()
            } else{ super.onBackPressed() }
        }
    }
}