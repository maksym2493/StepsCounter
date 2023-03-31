package com.example.stepcounterwef

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.stepcounterwef.Tools.Companion.getRandomColor
import com.example.stepcounterwef.Tools.Companion.rewriteDigit
import com.example.stepcounterwef.Tools.Companion.round
import com.example.stepcounterwef.Tools.Companion.setBackground
import com.example.stepcounterwef.Tools.Companion.updateView
import com.example.stepcounterwef.databinding.ActivityStatBinding

class Stat: AppCompatActivity() {
    private var m: Int? = null
    private var d: Int? = null

    private var active = true
    private lateinit var binding: ActivityStatBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityStatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        removeNotification()

        Data.stat = this
        start()
    }

    fun removeNotification(){
        if(Tools.removeNotification(intent)){
            val args = Data.stats.getByTime(intent.getLongExtra("eventTime", 0))

            m = args[0]
            d = if(intent.getBooleanExtra("d", true)){ args[1] } else{ null }
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

    override fun onBackPressed(){
        if(d != null){ d = null } else{ if(m != null){ m = null } else{
            if(Data.main != null){ super.onBackPressed() } else{
                finish()
                startActivity(Intent(this, MainActivity::class.java))
            }
            return
        } }

        start()
    }

    override fun onDestroy() {
        Data.stat = null
        super.onDestroy()
    }


    fun isActive(): Boolean{ return active }

    fun start(lM: Int? = m, lD: Int? = d) {
        m = lM
        d = lD
        Data.stats.checkUpdate()

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

        var size = Data.stats.getSize(m, d)
        var maxSize = Data.stats.getMaxSize(m, d)
        var skipedCount = Data.stats.getSkipedCount(m, d)
        var parentSize = if (d != null) {
            Data.stats.getSize(m)
        } else {
            Data.stats.getSize()
        } - 1

        with(binding) {
            var screenWidth = (windowManager.defaultDisplay.width - 20 * diagram.layoutParams.height / 200f).toInt()

            if(backgroundImage.drawable == null){ setBackground(backgroundImage) }

            var i = 0
            var count = 31
            var maxValue = 0f

            args[index] = 0
            while (i++ < size) {
                var value = Data.stats.getCount(args[0]!!, args[1], args[2])

                args[index] = args[index]!! + 1
                if (value > maxValue) {
                    maxValue = value.toFloat()
                }
            }

            args[index] = size - 1
            var target = Data.stats.getTarget(m, d)
            var date = Data.stats.getStringTime(m, d)

            headline.text = date[0][index]
            if (m == null) {
                left.visibility = View.GONE; right.visibility = View.GONE
            } else {
                if ((d == null && m == 0) || (d != null && d == 0)) {
                    right.visibility = View.GONE
                } else {
                    right.setOnClickListener(
                        Listener(
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
                        Listener(
                            arrayListOf<Int?>(m, d),
                            index,
                            1
                        )
                    ); left.text = "<-"; if (left.visibility == View.GONE) {
                        left.visibility = View.VISIBLE
                    }
                }
            }

            var value = Data.stats.getCount(m, d)
            var percent = value / target
            var width = screenWidth!! * percent

            if(width >= 1){
                progressBar.setBackgroundColor(Color.parseColor(getRandomColor()))
                if(progressBar.visibility == View.INVISIBLE){ progressBar.visibility = View.VISIBLE }
                progressBar.layoutParams.width = if(percent < 1){ width.toInt() } else{ screenWidth!! }

                updateView(progressBar)
            } else{ progressBar.visibility = View.INVISIBLE }

            progressValue.text = "Прогрес: " + round(percent * 100) + " %"
            averageAndMax.text = "Максимум кроків: " + rewriteDigit(maxValue.toInt()) + ".\nСередня кількість: " + round(value / size.toFloat()) + "."

            if(maxValue == 0f){ maxValue = 1f }

            do{
                var view = diagram.getChildAt(count)
                var constraintLayout = findViewById<ConstraintLayout>(buttons.getChildAt(count).id)

                var progressView = constraintLayout.getChildAt(1)
                var button = findViewById<TextView>(constraintLayout.getChildAt(0).id)

                if (skipedCount == 0 && args[index]!! > -1) {
                    value = (diagram.layoutParams.height * (Data.stats.getCount(args[0], args[1], args[2]) / maxValue)).toInt()
                    if (value != 0) {
                        view.layoutParams.height = value
                        if (view.visibility == View.GONE || view.visibility == View.INVISIBLE) { view.visibility = View.VISIBLE }

                        updateView(view)

                    } else { view.visibility = View.INVISIBLE }

                    value = (screenWidth!! * (Data.stats.getCount(args[0], args[1], args[2]) / Data.stats.getTarget(args[0], args[1], args[2]))).toInt()
                    if (value != 0) {
                        progressView.layoutParams.width = value
                        if (progressView.visibility == View.GONE || progressView.visibility == View.INVISIBLE) { progressView.visibility = View.VISIBLE }

                        updateView(progressView)

                    } else { progressView.visibility = View.INVISIBLE }

                    button.text = Data.stats.getStringTime(args[0], args[1], args[2])[1][index] + " — " + rewriteDigit(Data.stats.getCount(args[0]!!, args[1], args[2]))
                    if(index != 2){ constraintLayout.setOnClickListener(Listener(arrayListOf(args[0], args[1]))) } else{ constraintLayout.setOnClickListener{} }

                    if(constraintLayout.visibility == Button.GONE) {constraintLayout.visibility = ConstraintLayout.VISIBLE }

                    args[index] = args[index]!! - 1
                }

                if (maxSize-- > 0) {
                    if (skipedCount == 0 && args[index] != -2) {
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

                if(skipedCount != 0){ skipedCount -= 1 }
            } while (--count != -1)
        }
    }
}

class Listener(var args: ArrayList<Int?>, var index: Int? = null, var add: Int? = null): View.OnClickListener{
    override fun onClick(p0: View?) {
        with(Data.stat!!){
            if(index  != null){
                index = index!! - 1
                args[index!!] = args[index!!]!! + add!!
            }

            start(args[0], args[1])
        }
    }
}