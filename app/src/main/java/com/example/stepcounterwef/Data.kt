package com.example.stepcounterwef

import java.io.File

class Data{
    companion object{
        var stat: Stat? = null
        var main: MainActivity? = null
        var stepCounter: StepCounter? = null

        lateinit var lvl: Level
        lateinit var stats: Stats

        fun init(path: File){
            var dir = File(path, "Data")
            if (!dir.exists()){ dir.mkdir() }

            lvl = Level(path)
            stats = Stats(path)
        }
    }
}