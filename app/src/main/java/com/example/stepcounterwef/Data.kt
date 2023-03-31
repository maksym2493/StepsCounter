package com.example.stepcounterwef

import android.graphics.drawable.Drawable
import com.example.stepcounterwef.Tools.Companion.getBackground
import java.io.File

class Data{
    companion object{
        var stat: Stat? = null
        var main: MainActivity? = null
        var stepCounter: StepCounter? = null
        var backgroundImage: Drawable? = null

        lateinit var lvl: Level
        lateinit var stats: Stats

        lateinit var path: File

        fun init(path: File){
            var dir = File(path, "Data")
            if (!dir.exists()){ dir.mkdir() }

            this.path = path

            lvl = Level(path)
            stats = Stats(path)
            getBackground()
        }
    }
}