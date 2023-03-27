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
import com.example.stepcounterwef.databinding.ActivityMainBinding
import java.io.File
import java.lang.System.exit

class MainActivity: AppCompatActivity() {
    private lateinit var lvl: Level
    private lateinit var stats: Stats

    private lateinit var binding: ActivityMainBinding

    companion object{
        var r = false
        var stat: Stat? = null
        private var windowSize: Int? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        r = true
        var dir = File(filesDir, "data")
        if (!dir.exists()) {
            dir.mkdir()
        }

        lvl = Level(filesDir)
        stats = Stats(filesDir)

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) { ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACTIVITY_RECOGNITION), 100) } else{
            StepsCounter.lvl = lvl
            StepsCounter.stats = stats
            StepsCounter.context = this
            startService(Intent(this, StepsCounter::class.java))

            start()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            StepsCounter.lvl = lvl
            StepsCounter.stats = stats
            StepsCounter.context = this
            startService(Intent(this, StepsCounter::class.java))

            start()
        } else{ finish(); exit(0) }
    }

    fun start(){
        binding = ActivityMainBinding.inflate(layoutInflater)
        windowSize = (windowManager.defaultDisplay.width - 20 * binding.diagram.layoutParams.height / 200f).toInt()

        setContentView(binding.root)

        with(binding){
            var expLevel = lvl.get_exp()
            var progress = expLevel[0] / expLevel[1].toFloat()

            level.text = "Рівень: " +  rewriteDigit(lvl.get_lvl())
            exp.text = rewriteDigit(expLevel[0]) + " з " + rewriteDigit(expLevel[1]) + " [ " + Stats.round(progress * 100) + " % ]"

            var width = (progress * windowSize!!).toInt()
            if(width != 0){ levelProgress.layoutParams.width = width; levelProgress.setBackgroundColor(Color.parseColor(Stat.getRandomColor())) } else{ levelProgress.visibility = View.INVISIBLE }

            showTargets()
            drawDiagram()

            showStatistic.setOnClickListener{
                Stat.data1 = stats
                Stat.data2 = arrayListOf(null, null)

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
                                                    if(stats.getTarget(0, 0).toInt() == target){ Toast.makeText(this@MainActivity, "Вказана ціль є поточною.", Toast.LENGTH_SHORT).show() } else{
                                                        end = true
                                                        newTarget.setText("")
                                                        stats.setTarget(target)
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
        super.onResume()
        r = true
        start()
    }

    override fun onPause() {
        super.onPause()
        r = false
    }

    fun drawDiagram(){
        with(binding){
            var counts = stats.get_last_counts()

            var maxValue = 0f
            var totalCount = 0f

            var i = counts.size - 1
            do{ totalCount += counts[i]; if(counts[i] > maxValue){ maxValue = counts[i].toFloat() } } while(--i != -1)

            averageAndMax.text = "Максимум кроків: " + maxValue.toInt().toString() + ".\nСередня кількість: " + Stats.round(totalCount / counts.size.toFloat()) + "."

            i = 0
            if(maxValue == 0f){ maxValue = 1f }
            do{
                var view = diagram.getChildAt(i)

                if(counts.size != 0){
                    var height = ((counts.removeAt(0) / maxValue) * diagram.layoutParams.height).toInt()

                    if(height != 0){
                        view.setBackgroundColor(Color.parseColor(Stat.getRandomColor()))
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
                stats.getTarget(0, 0),
                stats.getTarget(0),
                stats.getTarget()
            )

            var stepsCount = listOf(
                stats.getCount(0, 0),
                stats.getCount(0),
                stats.getCount()
            )

            var i = 2
            var listOfTextViews = listOf(daylyExp, monthlyExp, totalExp)
            var listOfViews = listOf(daylyProgress, monthlyProgress, totalProgress)

            do{
                listOfTextViews[i].text =  rewriteDigit(stepsCount[i]) + " з " +  rewriteDigit(targets[i].toInt())

                var width = ((stepsCount[i] / targets[i]) * windowSize!!).toInt()
                if(width != 0){
                    if(width < windowSize!!){ listOfViews[i].layoutParams.width = width } else{ listOfViews[i].layoutParams.width = windowSize!! }
                    listOfViews[i].setBackgroundColor(Color.parseColor(Stat.getRandomColor()))
                } else{ listOfViews[i].visibility = View.INVISIBLE }
            } while(--i != -1)
        }
    }

    fun rewriteDigit(digit: Int): String{
        var res = ""
        var count = 0
        digit.toString().reversed().forEach{ if(count == 3){ res = " " + res; count = 0 } else{ count += 1 }; res = it + res }

        return res
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