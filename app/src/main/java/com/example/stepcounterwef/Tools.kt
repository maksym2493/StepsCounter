package com.example.stepcounterwef

import java.util.*

class Tools{
    companion object{
        fun getRandomColor(): String{
            var color = "#"
            var random = Random()
            var s = "0123456789ABCDEF"

            var times = 6
            do{ color += s[random.nextInt(16)] } while(--times != 0)

            return color
        }

        fun rewriteDigit(digit: Int): String{
            var res = ""
            var count = 0
            digit.toString().reversed().forEach{ if(count == 3){ res = " " + res; count = 0 } else{ count += 1 }; res = it + res }

            return res
        }

        fun pow(integer: Int, count: Int): Int{
            var res = 1
            var count = count
            while (count != 0){ res *= integer; count -= 1 }

            return res
        }

        fun round(x: Float, y: Int = 2): String{
            var y = pow(10, y).toFloat()
            var x = x * y

            var add = 0
            (x - x.toInt()).toString().substring(2).forEach{ if(it != '4'){ return@forEach }; if(it == '5'){ add = 1; return@forEach } }

            return ((x.toInt() + add) / y).toString()
        }
    }
}