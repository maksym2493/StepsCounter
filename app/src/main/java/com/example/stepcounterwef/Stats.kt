package com.example.stepcounterwef

import android.content.Intent
import com.example.stepcounterwef.Tools.Companion.getMonth
import com.example.stepcounterwef.Tools.Companion.notify
import com.example.stepcounterwef.Tools.Companion.pow
import com.example.stepcounterwef.Tools.Companion.rewriteDigit
import com.example.stepcounterwef.Tools.Companion.round
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

    fun get_last_counts(counts: ArrayList<Int>): ArrayList<Int>{
        days.forEach{ it.get_last_counts(counts); if(counts.size == 24){ return@forEach } }

        return counts
    }
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

    fun get_last_counts(counts: ArrayList<Int>): ArrayList<Int>{
        hours.forEach{ counts.add(it); if(counts.size == 24){ return@forEach } }

        return counts
    }
}

//month  day2 hour24 hour23  day1 hour24 hour23\n
//month2  day2 hour24 hour23  day1 hour24 hour23\n

data class Stats(var path: File){
    private var time: Long? = null
    var count: Int? = null
    private var target: Int? = null
    private var stats: ArrayList<Month> = ArrayList<Month>()

    private var stepsCount: Int? = null
    private var f = File(path, "Data/stats.txt")

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

    fun checkUpdate(): Boolean{
        var newDay = false
        var update_stats = false
        var curTime = Date().time

        while((curTime - time!!) >= 3600000L){
            count = count!! + 1
            time = time!! + 3600000L
            if(!update_stats){ update_stats = true }

            if(count!! == 24){
                count = 0
                if(!newDay){ newDay = true }

                if(Date(time!!).toString().split(" ")[2] == "01"){
                    if(stats.size == 12){ stats.removeAt(11) }

                    stats.add(0, Month("0  0 0"))

                    notify(
                        "NewDay",
                        "Звіт за день",
                        get_notification_title(),
                        get_notification_text(1, 0),
                        get_notification_intent(1, 0)
                    )

                    notify(
                        "NewMonth",
                        "Звіт за місяць",
                        get_notification_title(1),
                        get_notification_text(1),
                        get_notification_intent(1)
                    )
                } else{
                    stats[0].newDay()

                    notify(
                        "NewDay",
                        "Звіт за день",
                        get_notification_title(),
                        get_notification_text(0, 1),
                        get_notification_intent(0, 1)
                    )
                }
            } else{ stats[0].newHour() }
        }

        if(update_stats){ write() }

        return newDay
    }

    fun get_notification_title(type: Int = 0): String{
        var res = "Звіт за "
        var date = Date(time!! - 1000).toString().split(" ")
        if(type == 0){ res += date[2] + "-е" } else{ res += getMonth(date[1]) }

        return res
    }

    fun get_notification_text(m: Int?, d: Int? = null): String{
        var count = getCount(m, d)
        var target = getTarget(m, d).toInt()

        var delta = target - count
        var moduleDelta = delta % 10
        return if(delta <= 0){ "Ціль досягнута!\nЗроблено: " + rewriteDigit(count) + " з " + rewriteDigit(target) + " [ " + round((count / target.toFloat()) * 100) + "% ]" } else{
            "Не вистачило " + rewriteDigit(delta) + "-" + if(moduleDelta == 1){ "го" } else{ if(moduleDelta in arrayOf(2, 3, 4)){ "х" } else{ "ти" } } + " крок" + if(moduleDelta != 1){ "ів" } else{ "а" } + " [ " + round((count / target.toFloat()) * 100) + "% ]"
        }
    }

    fun get_notification_intent(m: Int, d: Int? = null): Intent{
        var intent = Intent(Data.stepCounter, Stat::class.java)

        intent.putExtra("eventTime", getLongTime(m, d))
        if(d == null){ intent.putExtra("d", false) }

        return intent
    }

    fun getByTime(t: Long): Array<Int?>{
        val delta = (time!! - t)
        var args = arrayOf(
            0,
            (delta / (3600L * 24 * 1000)).toInt()
        )

        do{
            if(args[1] >= stats[args[0]].getSize()){ args[1] -= stats[args[0]++].getSize() } else{ break }
        } while(args[0] != stats.size)

        if(args[0] == stats.size){ return arrayOf(null, null) }

        return arrayOf(args[0], args[1])
    }

    fun add(c: Int){ stats[0].add(c); write() }

    fun getCount(m: Int? = null, d: Int? = null, h: Int? = null): Int{ if(m != null){ return stats[m].getCount(d, h) } else{ var c = 0; var times = stats.size - 1; do{ c += stats[times].getCount() } while(--times != -1); return c } }
    fun getSize(m: Int? = null, d: Int? = null): Int{ if(m != null){ return stats[m].getSize(d) } else{ return stats.size }}

    fun getMaxSize(m: Int? = null, d: Int? = null): Int{
        if(d != null){ return 24 }
        if(m == null){ return 12 }
        if(m != 0){ return Date(getLongTime(m, d)).toString().split(" ")[2].toInt() }

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
        if(m != null){
            var size = getSize(m)
            if(m != 0){ return (size * target!!).toFloat() }
            return ((getMaxSize(m) - getSkipedCount(m, size = size)) * target!!).toFloat()
        }

        return (365 * target!!).toFloat()
    }

    fun getSkipedCount(m: Int? = null, d: Int? = null, size: Int? = null): Int{
        var day = Date(getLongTime(m, d)).toString().split(" ")[2].toInt()
        var hour = Date().toString().split(" ")[3].split(":")[0].toInt() + 1

        return if(m != null){ if(d != null){ if(m == 0 && d == 0){ hour } else{ 24 } } else{ day } - if(size != null){ size } else{ getSize(m, d) } } else{ 0 }
    }

    fun getLongTime(m: Int? = null, d: Int? = null, h: Int? = null): Long{
        var m = m
        var d = d
        var t = time!!

        if((m != null && m != 0) || (m != null && m == 0 && d != 0)){ t -= (count!! + if(h != null){ 1 } else{ 0 }) * 3600 * 1000 }

        if(h != null){ t -= h * 3600 * 1000; if(m == 0){ d = d!! - 1 } }
        if(d != null && d > 0){ t -= d * 3600 * 24 * 1000L }
        if(m != null){ while(m != 0){ t -= stats[m - 1].getSize() * 3600 * 24 * 1000L; m -= 1 } }

        return t
    }

    fun getStringTime(m: Int? = null, d: Int? = null, h: Int? = null): Array<Array<String>>{
        var date = Date(getLongTime(m, d, h)).toString().substring(0, 13).split(" ")
        return arrayOf(arrayOf("Місяці", getMonth(date[1]), getMonth(date[1]) + " " + date[2] + "-е"), arrayOf(getMonth(date[1]), date[2] + "-е", date[3] + ":00"))
    }

    fun write(){
        var text = time.toString() + " " + count.toString()+ " " + target.toString() + "\n"
        stats.forEach{ text += it.toString() + "\n" }

        f.writeText(text.substring(0, text.length - 1))
    }

    fun getStartTime(): Long{
        var i = 2
        var date = Date().time

        Date().toString().split(" ")[3].split(":").forEach{ date -= it.toInt() * pow(60, i--) * 1000 }

        return date
    }

    fun get_last_counts(): ArrayList<Int>{
        var counts = ArrayList<Int>()
        stats.forEach{ it.get_last_counts(counts); if(counts.size == 24){ return@forEach } }

        return counts
    }

    fun setTarget(t: Int){ target = t; write() }
}
