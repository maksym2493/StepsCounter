package com.example.stepcounterwef

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import java.util.*

class Tools{
    companion object{
        var notificationId = 2

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
            digit.toString().reversed().forEach{ if(count == 3){ res = " " + res; count = 1 } else{ count += 1 }; res = it + res }

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

        fun notify(name: String, title: String, text: String, intent: Intent = Intent(Data.stepCounter, MainActivity::class.java)){
            val manager = Data.stepCounter!!.getSystemService(NotificationManager::class.java)

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                val channel = NotificationChannel(
                    name,
                    name,
                    NotificationManager.IMPORTANCE_DEFAULT
                )

                manager.createNotificationChannel(channel)
            }

            intent.putExtra("notificationId", notificationId)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP

            var notification = NotificationCompat.Builder(Data.stepCounter!!, name)
                .setContentTitle(title)
                .setContentText(text)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(PendingIntent.getActivity(Data.stepCounter, notificationId, intent, PendingIntent.FLAG_UPDATE_CURRENT))
                .build()

            manager.notify(notificationId, notification)

            notificationId += 1
        }
    }
}