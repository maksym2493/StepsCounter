package com.example.stepcounterwef

import java.io.File

class Data{
    companion object{
        var stat: Stat? = null

        lateinit var lvl: Level
        lateinit var stats: Stats

        fun init(path: File){
            lvl = Level(path)
            stats = Stats(path)
        }
    }
}