package com.example.stepcounterwef

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.NotificationCompat
import java.io.File
import java.io.FileInputStream
import java.util.*

class Tools{
    companion object{
        var notificationId = 3
        val monthes = arrayOf(
            arrayOf("Jan", "Січень"),
            arrayOf("Feb", "Лютий"),
            arrayOf("Mar", "Березень"),
            arrayOf("Apr", "Квітень"),
            arrayOf("May", "Травень"),
            arrayOf("Jun", "Червень"),
            arrayOf("Jul", "Липень"),
            arrayOf("Aug", "Серпень"),
            arrayOf("Sep", "Вересень"),
            arrayOf("Oct", "Жовтень"),
            arrayOf("Nov", "Листопад"),
            arrayOf("Dec", "Грудень")
        )

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
            (x - x.toInt()).toString().substring(2).forEach{ if(it.digitToInt() >= 5){ add = 1; return@forEach }; if(it != '4'){ return@forEach } }

            x = (x.toInt() + add) / y
            return (rewriteDigit(x.toInt()) + "." + ((x - x.toInt()) * y).toInt().toString()).trimEnd('0').trimEnd('.')
        }

        fun notify(id: String, name: String, title: String, text: String, intent: Intent = Intent(Data.stepCounter, MainActivity::class.java)){
            val manager = Data.stepCounter!!.getSystemService(NotificationManager::class.java)

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                val channel = NotificationChannel(
                    id,
                    name,
                    NotificationManager.IMPORTANCE_DEFAULT
                )

                manager.createNotificationChannel(channel)
            }

            intent.putExtra("notificationId", notificationId)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP

            var notification = NotificationCompat.Builder(Data.stepCounter!!, id)
                .setContentTitle(title)
                .setContentText(text)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(PendingIntent.getActivity(Data.stepCounter, notificationId, intent, PendingIntent.FLAG_IMMUTABLE))
                .build()

            manager.notify(notificationId, notification)

            notificationId += 1
        }

        fun removeNotification(intent: Intent): Boolean{
            val notificationId = intent.getIntExtra("notificationId", 0)
            if(notificationId != 0){
                val notificationManager = Data.stepCounter!!.getSystemService(NotificationManager::class.java)

                for(notification in notificationManager.activeNotifications){
                    if(notification.id == notificationId){
                        notificationManager.cancel(notificationId)
                        return true
                    }
                }
            }

            return false
        }

        fun updateView(view: View){
            view.requestLayout()
            view.invalidate()
        }

        fun getMonth(name: String): String{
            for(month in monthes){ if(month[0] == name){ return month[1] } }
            return ""
        }

        fun getFontSettings(){
            var file = File(Data.path, "Data/fontSettings")
            if(file.exists()){
                var args = file.readText().split(" ")
                Data.fontSettings = arrayOf(args[0].toFloat(), args[1].toFloat())
            } else{
                var random = Random()
                Data.fontSettings = arrayOf(
                    Color.rgb(random.nextInt(255), random.nextInt(255), random.nextInt(255)).toFloat(),
                    6f + 4 * random.nextInt(101) / 100
                )

                updateFontSettings()
            }
        }

        fun setFontSettings(textView: TextView, type: Int = 0){
            var context = if(Data.main != null){ Data.main!! } else{ Data.stat!! }

            setShadow(textView)
            textView.setTextColor(Data.fontSettings!![0].toInt())
            textView.textSize = (Data.fontSettings!![1] + 2 * type) * context.resources.displayMetrics.scaledDensity
        }

        fun setShadow(textView: TextView, color: Int = Data.fontSettings!![0].toInt()){
            textView.setShadowLayer(1f, 2f, 2f, getShadowColor(color))
        }

        fun getShadowColor(color: Int): Int{
            if((0.2162 * ((color shr 16) and 0xff) + 0.7152 * ((color shr 8) and 0xff) + 0.0722 * (color and 0xff)) > 128){ return Color.BLACK } else{ return Color.WHITE }
        }

        fun updateFontSettings(){
            File(Data.path, "Data/fontSettings").writeText(Data.fontSettings!![0].toString() + " " + Data.fontSettings!![1].toString())
        }

        fun getBackground(){
            var file = File(Data.path, "Data/background")
            if(file.exists()){
                var inputStream = FileInputStream(file)
                Data.backgroundImage = Drawable.createFromStream(inputStream,null)
            } else{ Data.backgroundImage = null }
        }

        fun setBackground(backgroundImage: ImageView){ backgroundImage.setImageDrawable(Data.backgroundImage) }
        fun removeBackground(backgroundImage: ImageView){ File(Data.path, "Data/background").delete(); getBackground(); setBackground(backgroundImage) }
    }
}
