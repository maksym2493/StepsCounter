package com.example.stepcounterwef

import java.io.File
import java.util.*

data class Month(var s: String){
    private var count: Int? = null
    private var days = ArrayList<Day>()

    init {
        var stats = s.split("  ")
        count = stats[0].toInt()

        stats.subList(1, stats.size).forEach{ days.add(Day(it)) }
    }

    override fun toString(): String {
        var text = count.toString() + "  "
        days.forEach { text += it.toString() + "  " }

        return text.substring(0, text.length - 2)
    }

    fun add(c: Int){ days[0].add(c); count = count!! + c }
    fun newDay(){ days.add(0, Day("0 0")) }
    fun newHour(){ days[0].newHour() }

    fun getSize(d: Int? = null): Int{ if(d != null){ return days[d].getSize() } else{ return days.size }}
    fun getCount(d: Int? = null, h: Int? = null): Int{ if(d != null){ return days[d].getCount(h)} else{ return count!! } }
}

data class Day(var s: String){
    private var count: Int? = null
    private var hours = ArrayList<Int>()

    init {
        var stats = s.split(" ")
        count = stats[0].toInt()

        stats.subList(1, stats.size).forEach{ hours.add(it.toInt()) }
    }

    override fun toString(): String {
        var text = count.toString() + " "
        hours.forEach { text += it.toString() + " " }

        return text.substring(0, text.length - 1)
    }

    fun add(c: Int){ count = count!! + c; hours[0] += c }
    fun newHour(){ hours.add(0, 0) }

    fun getSize(): Int{ return hours.size }
    fun getCount(h: Int? = null): Int{ if(h != null){ return hours[h] } else{ return count!! } }
}

//month  day2 hour24 hour23  day1 hour24 hour23\n
//month2  day2 hour24 hour23  day1 hour24 hour23\n

class Stats(var path: File, parent: MainActivity){
    private var time: Long? = null
    var count: Int? = null
    private var target: Int? = null
    private var stats: ArrayList<Month> = ArrayList<Month>()

    private var stepsCount: Int? = null
    private var f = File(path, "data/stats.txt")

    init{
        if(!f.exists()){
            target = 10000
            time = getStartTime()
            count = ((Date().time - time!!) / 3600000).toInt()

            time = time!! + count!! * 3600000

            stats.add(Month("0  0 0"))
            write()
        } else{
            var data = f.readText().split("\n")
            var text = data[0].split(" ")

            time = text[0].toLong()
            count = text[1].toInt()
            target = text[2].toInt()

            data.subList(1, data.size).forEach{ stats.add(Month(it)) }
        }
    }

    fun cheackUpdate(){
        var update_time = false
        var update_stats = false
        var curTime = Date().time

        while((curTime - time!!) >= 3600000){
            count = count!! + 1
            if(!update_stats){ update_stats = true }

            if(count!! == 24){
                count = 0
                if(!update_time){ update_time = true }

                if(Date(time!!).toString().split(" ")[1] != Date(time!! + 3600000).toString().split(" ")[1]){
                    if(stats.size == 12){ stats.removeAt(11) }

                    stats.add(0, Month("0  0 0"))
                } else{ stats[0].newDay() }
            } else{ stats[0].newHour() }

            time = time!! + 3600000
        }

        if(update_time){
            var new_time = getStartTime()
            if(new_time + count!! * 3600000 != time){
                time = curTime - curTime % 3600000
                count = ((new_time - time!!) / 3600000).toInt()
            }
        }

        if(update_stats){ write() }
    }

    fun getStepsCount(c: Int): Int{
        if(stepsCount == null){ stepsCount = c; return 0}
        return stepsCount!! - c
    }

    fun add(c: Int){ stats[0].add(c); stepsCount = stepsCount!! + c; write() }

    fun getCount(m: Int?, d: Int? = null, h: Int? = null): Int{ if(m != null){ return stats[m].getCount(d, h) } else{ var c = 0; var times = stats.size - 1; do{ c += stats[times].getCount() } while(--times != -1); return c } }
    fun getSize(m: Int? = null, d: Int? = null): Int{ if(m != null){ return stats[m].getSize(d) } else{ return stats.size }}

    fun getMaxSize(m: Int? = null, d: Int? = null): Int{
        if(d != null){ return 24 }
        if(m == null){ return 12 }
        if(m != 0){ return getSize(m) }

        var date = Date().toString().split(" ")

        var daysCount = 28
        var curMonth = date[1]
        var t = time!! + (daysCount - date[2].toInt() + 1) * 3600 * 24 * 1000

        while(Date(t).toString().split(" ")[1] == curMonth){ daysCount += 1; t += 3600 * 24 * 1000 }

        return daysCount
    }

    fun getTarget(m: Int? = null, d: Int? = null, h: Int? = null): Float{
        if(h != null){ return target!! / 24f }
        if(d != null){ return target!!.toFloat() }
        if(m != null){ return (getMaxSize(m) * target!!).toFloat() }

        var size = 0
        var times = stats.size - 1
        do{ size += getMaxSize(times) } while(--times != -1)

        return (size * target!!).toFloat()
    }

    fun getTime(m: Int? = null, d: Int? = null, h: Int? = null): Array<Array<String>>{
        var m = m
        var t = time!! - if(d != null){ (3600 * 24 * d) * 1000 } else{ 0 } - if(h != null){ (3600 * h) * 1000 } else{ 0 }
        if(m != null){ while(m != 0){ t -= stats[m].getSize() * (3600 * 24) * 1000; m -= 1 } }

        var date = Date(t).toString().substring(0, 13).split(" ")
        return arrayOf(arrayOf("Monthes", date[1], date[1] + " " + date[2] + "-е"), arrayOf(date[1], date[2] + "-е", date[3] + ":00"))
    }

    fun write(){
        var text = time.toString() + " " + count.toString()+ " " + target.toString() + "\n"
        stats.forEach{ text += it.toString() + "\n" }

        f.writeText(text.substring(0, text.length - 1))
    }

    fun pow(integer: Int, count: Int): Int{
        var res = 1
        var count = count
        while (count != 0){ res *= integer; count -= 1 }

        return res
    }

    fun getStartTime(): Long{
        var i = 2
        var date = Date().time

        Date().toString().split(" ")[3].split(":").forEach{ date -= it.toInt() * pow(60, i--) * 1000 }

        return date
    }
}