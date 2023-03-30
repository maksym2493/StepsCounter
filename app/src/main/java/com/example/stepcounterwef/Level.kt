package com.example.stepcounterwef

import com.example.stepcounterwef.Tools.Companion.notify
import java.io.File

class Level(var path: File){
    private var lvl: Int? = null
    private var exp: Int? = null
    private var totalExp: Int? = null

    private var f = File(path, "Data/level.txt")

    init{
        if(!f.exists()){ f.writeText("1 0 100") }

        var text = f.readText().split(" ")

        lvl = text[0].toInt()
        exp = text[1].toInt()
        totalExp = text[2].toInt()
    }

    fun write(){ f.writeText(lvl.toString() + " " + exp.toString() + " " + totalExp.toString()) }

    fun add(count: Int){
        exp = exp!! + count
        while(exp!! >= totalExp!!){
            lvl = lvl!! + 1; exp = exp!! - totalExp!!; totalExp = totalExp!! + 100
            notify("NewLevel", "Новий рівень", "Новий рівень!", "Тепер у Вас " + lvl.toString() + "-й рівень!")
        }

        write()
    }

    fun get_lvl(): Int{ return lvl!! }
    fun get_exp(): Array<Int>{ return arrayOf(exp!!, totalExp!!) }
}
