package com.example.stepcounterwef

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import com.example.stepcounterwef.databinding.ActivityStatBinding
import java.util.*

class Stat: AppCompatActivity() {
    private lateinit var binding: ActivityStatBinding

    companion object{
        var m: Int? = null
        var d: Int? = null

        lateinit var stats: Stats
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStatBinding.inflate(layoutInflater)

        setContentView(binding.root)

        var size = stats.getSize(m, d)
        var maxSize = stats.getMaxSize(m, d)

        var args = arrayOf(m, d, null)
        var index = if(d != null){ 2 } else{ if(m != null){ 1 } else{ 0 } }

        with(binding){
            var random = Random()

            var i = 0
            var maxValue = 1f
            var count = diagram.childCount - 1

            args[index] = 0
            while(i < size){
                var value = stats!!.getCount(args[0]!!, args[1], args[2])

                i += 1
                args[index] = args[index]!! + 1
                if(value > maxValue){ maxValue = value.toFloat() }
            }

            args[index] = size - 1
            do{
                var view = diagram.getChildAt(count)
                var button = findViewById<Button>(buttons.getChildAt(count).id)

                if(args[index] != -1){
                    var value = stats!!.getCount(args[0]!!, args[1], args[2])

                    button.text = Stat.stats.getTime(args[0]!!, args[1], args[2]) + " — " + value.toString()

                    view.setOnClickListener(Listener(this@Stat, args[0], args[1], args[2]))
                    view.layoutParams.height = (diagram.layoutParams.height * (value / maxValue)).toInt() + 1

                    args[index] = args[index]!! - 1
                }

                if(maxSize-- != 0){
                    view.setBackgroundColor(Color.parseColor(random.nextInt(0xffffff).toString()))
                    if(args[index] == -2){ view.layoutParams.height = 1 }
                } else{
                    view.visibility = View.GONE
                    button.visibility = Button.GONE
                }
            }while(--count != -1)
        }
    }
}

class Listener(val stat: Stat, val arg1: Int? = null, val arg2: Int? = null, val arg3: Int? = null): View.OnClickListener{
    override fun onClick(p0: View?) {
        Toast.makeText(stat, Stat.stats.getTime(arg1!!, arg2, arg3), Toast.LENGTH_SHORT).show()
    }
}