package com.example.stepcounterwef

import java.io.File

class Level(var path: File){
    private var lvl: Int? = null
    private var exp: Int? = null
    private var totalExp: Int? = null

    private var f = File(path, "data/level.txt")

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
        if(exp!! >= totalExp!!){ lvl = lvl!! + 1; exp = exp!! - totalExp!!}

        write()
    }

    fun get_lvl(): Int{ return lvl!! }
    fun get_exp(): Array<Int>{ return arrayOf(exp!!, totalExp!!) }

}
