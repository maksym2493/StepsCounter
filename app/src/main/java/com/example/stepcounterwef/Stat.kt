package com.example.stepcounterwef

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.stepcounterwef.databinding.ActivityStatBinding
import java.util.*

class Stat: AppCompatActivity() {
    private var r = true
    private var e = false
    private var m: Int? = null
    private var d: Int? = null

    private lateinit var stats: Stats
    private lateinit var binding: ActivityStatBinding

    companion object{
        lateinit var data1: Stats
        var data2 = ArrayList<Int?>()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        MainActivity.stat = this
        start(data1, data2[0], data2[1])
    }

    override fun onResume() {
        super.onResume()
        r = true
    }

    override fun onPause() {
        super.onPause()
        r = false
    }

    fun update(){ start(stats, m, d) }
    fun running(): Boolean{ return r }

    fun changeE(){ e = !e }
    fun getE(): Boolean{ return e }

    fun start(lS: Stats, lM: Int?, lD: Int?) {
        if(lM == null && lD == null && e == false){ e = true }

        binding = ActivityStatBinding.inflate(layoutInflater)
        setContentView(binding.root);

        m = lM
        d = lD

        stats = lS

        var args = arrayOf(m, d, null)
        var index = if (d != null) {
            2
        } else {
            if (m != null) {
                1
            } else {
                0
            }
        }

        var size = stats.getSize(m, d)
        var maxSize = stats.getMaxSize(m, d)
        var parentSize = if (d != null) {
            stats.getSize(m)
        } else {
            stats.getSize()
        } - 1

        with(binding) {
            var screenWidth = (windowManager.defaultDisplay.width - 20 * diagram.layoutParams.height / 200f).toInt()

            var i = 0
            var count = 31
            var maxValue = 0f

            args[index] = 0
            while (i++ < size) {
                var value = stats.getCount(args[0]!!, args[1], args[2])

                args[index] = args[index]!! + 1
                if (value > maxValue) {
                    maxValue = value.toFloat()
                }
            }

            args[index] = size - 1
            var target = stats.getTarget(m, d)

            var date = stats.getTime(args[0]!!, args[1], args[2])
            headline.text = date[0][index]
            if (m == null) {
                left.visibility = View.GONE; right.visibility = View.GONE
            } else {
                if ((d == null && m == 0) || (d != null && d == 0)) {
                    right.visibility = View.GONE
                } else {
                    right.setOnClickListener(
                        Listener2(
                            this@Stat,
                            stats,
                            arrayListOf<Int?>(m, d),
                            index,
                            -1
                        )
                    ); right.text = "->"; if (right.visibility == View.GONE) {
                        right.visibility = View.VISIBLE
                    }
                }
                if ((d == null && m == parentSize) || (d != null && d == parentSize)) {
                    left.visibility = View.GONE
                } else {
                    left.setOnClickListener(
                        Listener2(
                            this@Stat,
                            stats,
                            arrayListOf<Int?>(m, d),
                            index,
                            1
                        )
                    ); left.text = "<-"; if (left.visibility == View.GONE) {
                        left.visibility = View.VISIBLE
                    }
                }
            }

            var value = stats.getCount(m, d)
            var percent = value / target
            var width = screenWidth!! * percent

            if(width >= 1){
                progressBar.setBackgroundColor(Color.parseColor(getRandomColor()))
                progressBar.layoutParams.width = if(percent < 1){ width.toInt() } else{ screenWidth!! }
            } else{ progressBar.visibility = View.INVISIBLE }

            progressValue.text = "Прогрес: " + round(percent * 100) + " %"
            averageAndMax.text = "Максимум кроків: " + maxValue.toInt().toString() + ".\nСередня кількість: " + round(value / size.toFloat()) + "."

            diagramsBack.text = "Назад"
            diagramsBack.setOnClickListener(Listener2(this@Stat, stats, if(d != null){ arrayListOf(m, null) } else{ arrayListOf(null, null) }))

            if(maxValue == 0f){ maxValue = 1f }

            do {
                var view = diagram.getChildAt(count)
                var constraintLayout = findViewById<ConstraintLayout>(buttons.getChildAt(count).id)

                var progressView = constraintLayout.getChildAt(1)
                var button = findViewById<TextView>(constraintLayout.getChildAt(0).id)

                if (args[index]!! > -1) {
                    value = (diagram.layoutParams.height * (stats.getCount(args[0], args[1], args[2]) / maxValue)).toInt()
                    if (value != 0) {
                        view.layoutParams.height = value
                        view.setOnClickListener(Listener(this@Stat, stats, args[0], args[1], args[2]))
                        if (view.visibility == View.GONE || view.visibility == View.INVISIBLE) { view.visibility = View.VISIBLE }

                    } else { view.visibility = View.INVISIBLE }

                    value = (screenWidth!! * (stats.getCount(args[0], args[1], args[2]) / stats.getTarget(args[0], args[1], args[2]))).toInt()
                    if (value != 0) {
                        progressView.layoutParams.width = value
                        if (progressView.visibility == View.GONE || progressView.visibility == View.INVISIBLE) { progressView.visibility = View.VISIBLE }

                    } else { progressView.visibility = View.INVISIBLE }

                    button.text = stats.getTime(args[0], args[1], args[2])[1][index] + " — " + stats.getCount(args[0]!!, args[1], args[2]).toString()
                    if(index != 2){ constraintLayout.setOnClickListener(Listener2(this@Stat, stats, arrayListOf(args[0], args[1]))) }

                    if(constraintLayout.visibility == Button.GONE) {constraintLayout.visibility = ConstraintLayout.VISIBLE }

                    args[index] = args[index]!! - 1
                }

                if (maxSize-- > 0) {
                    if (args[index] != -2) {
                        var color = getRandomColor()

                        view.setBackgroundColor(Color.parseColor(color))
                        progressView.setBackgroundColor(Color.parseColor(color))

                        if (args[index] == -1) { args[index] = -2 }
                    } else {
                        view.visibility = View.INVISIBLE; constraintLayout.visibility = View.GONE
                    }
                } else {
                    view.visibility = View.GONE; constraintLayout.visibility = View.GONE
                }
            } while (--count != -1)
        }
    }

    fun getRandomColor(): String{
        var color = "#"
        var random = Random()
        var s = "0123456789ABCDEF"

        var times = 6
        do{ color += s[random.nextInt(16)] } while(--times != 0)

        return color
    }

    fun round(x: Float, y: Int = 2): String{
        var y = stats.pow(10, y).toFloat()
        var x = x * y

        var add = 0
        (x - x.toInt()).toString().substring(2).forEach{ if(it != '4'){ return@forEach }; if(it == '5'){ add = 1; return@forEach } }

        return ((x.toInt() + add) / y).toString()
    }
}

class Listener(val stat: Stat, var stats: Stats, val arg1: Int? = null, val arg2: Int? = null, val arg3: Int? = null): View.OnClickListener{
    override fun onClick(p0: View?) {
        //Toast.makeText(stat, stats.getTime(arg1!!, arg2, arg3)[1], Toast.LENGTH_SHORT).show()
    }
}

class Listener2(var parent: Stat, var arg1: Stats, var arg2: ArrayList<Int?>, var index: Int? = null, var add: Int? = null): View.OnClickListener{
    override fun onClick(p0: View?) {
        if(index != null){
            index = index!! - 1
            arg2[index!!] = arg2[index!!]!! + add!!
        }

        if(arg2[0] != null || arg2[0] != null){ if(parent.getE()){ parent.changeE() }; parent.start(arg1, arg2[0], arg2[1]) } else{ if(parent.getE()){ parent.finish() } else{ parent.changeE(); parent.start(arg1, arg2[0], arg2[1]) } }
    }
}