package com.example.stepcounterwef

import android.content.Context
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import com.example.stepcounterwef.databinding.ActivityStatBinding
import kotlinx.coroutines.NonDisposableHandle.parent
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.max

class Stat: AppCompatActivity() {
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
        binding = ActivityStatBinding.inflate(layoutInflater)

        setContentView(binding.root); start(data1, data2[0], data2[1])
    }

    fun start(lS: Stats, lM: Int?, lD: Int?){
        m = lM
        d = lD

        stats = lS

        var args = arrayOf(m, d, null)
        var index = if(d != null){ 2 } else{ if(m != null){ 1 } else{ 0 } }

        var size = stats.getSize(m, d)
        var maxSize = stats.getMaxSize(m, d)
        var parentSize = if(d != null){ stats.getSize(m) } else{ stats.getSize() } - 1

        with(binding){
            var i = 0
            var maxValue = 1f
            var count = diagram.childCount - 1

            args[index] = 0
            while(i++ < size){
                var value = stats!!.getCount(args[0]!!, args[1], args[2])

                args[index] = args[index]!! + 1
                if(value > maxValue){ maxValue = value.toFloat() }
            }

            args[index] = size - 1
            var target = stats.getTarget(m, d)
            maxValue = if(target > maxValue * 2){ maxValue * 1.25f } else{ if(target > maxValue){target} else{ maxValue } }

            var date = stats.getTime(args[0]!!, args[1], args[2])
            headline.text = date.substring(0, date.lastIndexOf(" "))
            if(m == null){ left.visibility = View.GONE; right.visibility = View.GONE } else{
                if((d == null && m == 0) || (d != null && d == 0)){ left.visibility = View.GONE } else{ left.setOnClickListener(Listener2(this@Stat, stats, arrayListOf<Int?>(m, d), index, -1)); left.text = "<-"; if(left.visibility == View.GONE){ left.visibility = View.VISIBLE } }
                if((d == null && m == parentSize) || (d != null && d == parentSize)){ right.visibility = View.GONE } else{ right.setOnClickListener(Listener2(this@Stat, stats, arrayListOf<Int?>(m, d), index, 1)); right.text = "->"; if(right.visibility == View.GONE){ right.visibility = View.VISIBLE } }
            }

            do{
                var view = diagram.getChildAt(count)
                var button = findViewById<Button>(buttons.getChildAt(31 - count).id)

                if(args[index]!! > -1){
                    var arg = args[index]!!
                    var value = stats!!.getCount(args[0]!!, args[1], args[2])

                    view.setOnClickListener(Listener(this@Stat, stats, args[0], args[1], args[2]))

                    args[index] = size - arg - 1
                    button.text = stats.getTime(args[0]!!, args[1], args[2]) + " — " + value.toString()

                    view.layoutParams.height = (diagram.layoutParams.height * (value / maxValue)).toInt() + 1

                    if(button.visibility == Button.GONE){ button.visibility == Button.VISIBLE }
                    if(view.visibility == View.GONE || view.visibility == View.INVISIBLE) { view.visibility = View.VISIBLE }

                    args[index] = arg - 1
                }

                if(maxSize-- > 0){
                    if(args[index] != -2){ view.setBackgroundColor(Color.parseColor(getRandomColor())); if(args[index] == -1){ args[index] = -2 } } else{ view.visibility = View.INVISIBLE; button.visibility = Button.GONE }
                } else{ view.visibility = View.GONE; button.visibility = Button.GONE }
            }while(--count != -1)
        }
    }

    fun getRandomColor(): String{
        var color = "#"
        var random = Random()
        var s = "1234567890ABCDEF"

        var times = 6
        do{ color += s[random.nextInt(16)] } while(--times != 0)

        return color
    }
}

class Listener(val stat: Stat, var stats: Stats, val arg1: Int? = null, val arg2: Int? = null, val arg3: Int? = null): View.OnClickListener{
    override fun onClick(p0: View?) {
        Toast.makeText(stat, stats.getTime(arg1!!, arg2, arg3), Toast.LENGTH_SHORT).show()
    }
}

class Listener2(var parent: Stat, var arg1: Stats, var arg2: ArrayList<Int?>, var index: Int, var add: Int): View.OnClickListener{
    override fun onClick(p0: View?) {
        index -= 1
        arg2[index] = arg2[index]!! + add

        parent.start(arg1, arg2[0], arg2[1])
    }
}