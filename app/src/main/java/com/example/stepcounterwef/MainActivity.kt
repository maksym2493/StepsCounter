package com.example.stepcounterwef

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.MediaStore
import android.provider.Settings
import android.view.View
import android.view.View.OnClickListener
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.stepcounterwef.Tools.Companion.getBackground
import com.example.stepcounterwef.Tools.Companion.getRandomColor
import com.example.stepcounterwef.Tools.Companion.removeBackground
import com.example.stepcounterwef.Tools.Companion.removeNotification
import com.example.stepcounterwef.Tools.Companion.rewriteDigit
import com.example.stepcounterwef.Tools.Companion.round
import com.example.stepcounterwef.Tools.Companion.setBackground
import com.example.stepcounterwef.Tools.Companion.setFontSettings
import com.example.stepcounterwef.Tools.Companion.setShadow
import com.example.stepcounterwef.Tools.Companion.updateFontSettings
import com.example.stepcounterwef.Tools.Companion.updateView
import com.example.stepcounterwef.databinding.ActivityMainBinding
import java.io.File
import java.io.FileOutputStream

class MainActivity: AppCompatActivity(){
    private var active = true
    private var gettingPermissions = false

    private lateinit var binding: ActivityMainBinding

    companion object{
        var r = false
        private var windowSize: Int? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        windowSize = (windowManager.defaultDisplay.width - 20 * binding.diagram.layoutParams.height / 200f).toInt()

        active = true
        Data.main = this
        Data.init(filesDir)

        start()
        removeNotification(intent)
        try{ cheackActivityRecognitionPermittion() } catch(e: Exception){ Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show() }
    }

    fun cheackActivityRecognitionPermittion(){
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED){
            gettingPermissions = true
            editAlert(
                "Activity Recognition",
                "Для роботи додатка необхідно надати йому дозвіл на підрахунок кроків. Цей дозвіл є обов'язковим. :(",
                "Надати",
                {
                    hideAlert()
                    ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACTIVITY_RECOGNITION), 100)
                }
            )
        } else{
            if(gettingPermissions){ gettingPermissions = false }

            checkNotificationPermission()
        }
    }

    fun checkNotificationPermission(){
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        var channelPermission = false
        val notificationPermission = !notificationManager.areNotificationsEnabled()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val channel = notificationManager.getNotificationChannel("DailyTarget")
            channelPermission = channel != null && channel.importance == NotificationManager.IMPORTANCE_NONE
        }

        if(notificationPermission || channelPermission){
            editAlert(
                "Notification Permission",
                if(notificationPermission){ "Для роботи додатка в фоні необхідно надати йому дозвіл на відправку сповіщень. " } else{ "" } + if(channelPermission){ "Обов'язковим дозволом є 'Денна ціль'." } else{ "" },
                "Надати",
                {
                    hideAlert()

                    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                    intent.putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
                    startActivityForResult(intent, 3)
                }
            )
        } else{
            startServices()
            cheackBateryOptimization()
        }
    }

    fun cheackBateryOptimization(){
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager

        if(!powerManager.isIgnoringBatteryOptimizations(packageName)){
            gettingPermissions = true
            editAlert(
                "Battery Optimization",
                "Для роботи додатка в фоновому режимі необхідний на ігнорування оптимізації батареї.\n\nПісля надання дозволу відкриється вікно, де можна перейти на вкладку контролю запусками. Перевірте, чи виключене автоматичне керування запуском цього додатку.",
                "Надати",
                {
                    hideAlert()
                    gettingPermissions = false

                    val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                    intent.data = Uri.parse("package:$packageName")
                    startActivityForResult(intent, 1)
                },
                "Пізніше",
                { gettingPermissions = false; hideAlert() }
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?){
        if(requestCode == 1){
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager

            if(powerManager.isIgnoringBatteryOptimizations(packageName)){
                intent = Intent(Intent.ACTION_POWER_USAGE_SUMMARY)
                startActivity(intent)
            }
        } else{
            if(requestCode == 2){
                if(resultCode == RESULT_OK){
                    try {
                        var inputStream = contentResolver.openInputStream(data!!.data!!)
                        val outputStream = FileOutputStream(File(filesDir, "Data/background"))

                        inputStream?.use{input ->
                            outputStream.use{output ->
                                input.copyTo(output)
                            }
                        }

                        getBackground()
                        setBackground(binding.backgroundImage)
                        Toast.makeText(this, "Фон оновлений!", Toast.LENGTH_SHORT).show()

                    } catch(e: Exception){ Toast.makeText(this, "Помилка!\n" + e.toString(), Toast.LENGTH_SHORT).show() }
                } else{
                    var text = "Встановлення фону скасоване."
                    if(binding.backgroundImage.drawable != null){ text = "Поточний фон видалений."; removeBackground(binding.backgroundImage) }
                    Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
                }
            } else{
                if(requestCode == 3){ checkNotificationPermission() }
            }
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    fun getPath(uri: Uri): String{
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = contentResolver.query(uri, projection, null, null, null)
        var columnIndex = cursor!!.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)

        cursor!!.moveToFirst()
        val filePath = cursor!!.getString(columnIndex)

        cursor.close()
        return filePath
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray){
        cheackActivityRecognitionPermittion()
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    fun editAlert(title: String, description: String, acceptButton: String, accceptListener: OnClickListener,
    rejectButton: String? = null, rejectListener: OnClickListener? = null){
        with(binding){
            permissionTitle.text = title
            permissionDesctiption.text = description

            acceptPermission.text = acceptButton
            acceptPermission.setOnClickListener(accceptListener)

            if(rejectButton != null){
                if(rejectPermission.visibility == View.INVISIBLE){ rejectPermission.visibility = View.VISIBLE }

                rejectPermission.text = rejectButton
                rejectPermission.setOnClickListener(rejectListener)
            } else{ rejectPermission.visibility = View.INVISIBLE }
        }

        showAlert()
    }

    fun showAlert(){
        with(binding){
            main.visibility = ConstraintLayout.GONE
            requestPermission.visibility = ConstraintLayout.VISIBLE
        }
    }

    fun hideAlert(){
        System.out.println("Hide")
        with(binding){
            main.visibility = ConstraintLayout.VISIBLE
            requestPermission.visibility = ConstraintLayout.GONE
        }
    }

    fun startServices(){
        ContextCompat.startForegroundService(this, Intent(this, StepCounter::class.java))
    }

    fun start(){
        Data.stats.checkUpdate()

        with(binding){
            var expLevel = Data.lvl.get_exp()
            var progress = expLevel[0] / expLevel[1].toFloat()

            if(backgroundImage.drawable == null){ setBackground(backgroundImage) }

            setFontSettings(targetsAndProgress, 1)
            setFontSettings(statsFor24Hours, 1)

            level.text = "Рівень: " +  rewriteDigit(Data.lvl.get_lvl()); setFontSettings(level)
            exp.text = rewriteDigit(expLevel[0]) + " з " + rewriteDigit(expLevel[1]) + " [ " + round(progress * 100) + " % ]"; setFontSettings(exp)

            var width = (progress * windowSize!!).toInt()
            if(width != 0){ levelProgress.layoutParams.width = width; levelProgress.setBackgroundColor(Color.parseColor(getRandomColor())); updateView(levelProgress) } else{ levelProgress.visibility = View.INVISIBLE }

            showTargets()
            drawDiagram()

            if(!openFontSettings.hasOnClickListeners()){
                openFontSettings.setOnClickListener{
                    val minSize = 6f
                    val scaleBarData = 10f
                    val scale = resources.displayMetrics.scaledDensity

                    setFontSettings(fs)
                    setFontSettings(tc, 1)
                    setFontSettings(cr)
                    setFontSettings(cg)
                    setFontSettings(cb)

                    main.visibility = ConstraintLayout.GONE
                    fontSettings.visibility = ConstraintLayout.VISIBLE

                    var color = Data.fontSettings!![0].toInt()
                    var size = Data.fontSettings!![1] - minSize

                    fontR.progress = (color shr 16) and 0xff; updateView(fontR)
                    fontG.progress = (color shr 8) and 0xff; updateView(fontG)
                    fontB.progress = color and 0xff; updateView(fontB)
                    fontSize.progress = (size * scaleBarData).toInt(); updateView(fontSize)

                    setFontSettings(exemple1, 1)
                    setFontSettings(exemple2)

                    if(!fontSize.hasOnClickListeners()){
                        var listener = object: OnSeekBarChangeListener{
                            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean){
                                var size = minSize + fontSize.progress / scaleBarData
                                color = Color.rgb(fontR.progress, fontG.progress, fontB.progress)

                                exemple1.textSize = (size + 2) * scale
                                exemple2.textSize = size * scale

                                setShadow(exemple1, color)
                                setShadow(exemple2, color)

                                exemple1.setTextColor(color)
                                exemple2.setTextColor(color)


                            }

                            override fun onStartTrackingTouch(p0: SeekBar?){}

                            override fun onStopTrackingTouch(p0: SeekBar?){}
                        }

                        fontSize.setOnSeekBarChangeListener(listener)
                        fontR.setOnSeekBarChangeListener(listener)
                        fontG.setOnSeekBarChangeListener(listener)
                        fontB.setOnSeekBarChangeListener(listener)

                        confirmFontSettings.setOnClickListener{
                            Data.fontSettings = arrayOf(
                                Color.rgb(fontR.progress, fontG.progress, fontB.progress).toFloat(),
                                minSize + fontSize.progress / scaleBarData
                            )

                            updateFontSettings()

                            start()
                            main.visibility = ConstraintLayout.VISIBLE
                            fontSettings.visibility = ConstraintLayout.GONE
                        }
                    }
                }

                showStatistic.setOnClickListener{
                    startActivity(Intent(this@MainActivity, Stat::class.java))
                }

                setBackgound.setOnClickListener{
                    val intent = Intent(Intent.ACTION_GET_CONTENT)
                    intent.type = "image/*"

                    startActivityForResult(intent, 2)
                }

                changeTarget.setOnClickListener{
                    with(binding){
                        main.visibility = ConstraintLayout.GONE
                        changeTargetLayout.visibility = ConstraintLayout.VISIBLE

                        setFontSettings(changeTargetHeadline, 1)

                        if(!confirm.hasOnClickListeners()){
                            confirm.setOnClickListener{
                                var end = false
                                if(newTarget.text.toString() == ""){ Toast.makeText(this@MainActivity, "Зміна скасована.", Toast.LENGTH_SHORT).show(); end = true } else{
                                    try{
                                        var target = newTarget.text.toString().toInt()
                                        if(target <= 0){ Toast.makeText(this@MainActivity, "Ціль не може дорівнювати нулю або бути меншою за нього.", Toast.LENGTH_SHORT).show() } else{
                                            if(target > 100000){ Toast.makeText(this@MainActivity, "Ціль не може більшо за 100 000.", Toast.LENGTH_SHORT).show() } else{
                                                if(target % 10 != 0){ Toast.makeText(this@MainActivity, "Ціль повинна націло ділитися на 10.", Toast.LENGTH_SHORT).show() } else{
                                                    if(Data.stats.getTarget(0, 0).toInt() == target){ Toast.makeText(this@MainActivity, "Вказана ціль є поточною.", Toast.LENGTH_SHORT).show() } else{
                                                        end = true
                                                        newTarget.setText("")
                                                        Data.stats.setTarget(target)
                                                        Toast.makeText(this@MainActivity, "Встановлена ціль в " + rewriteDigit(target) + " кроків.", Toast.LENGTH_SHORT).show()

                                                        start()
                                                    }
                                                }
                                            }
                                        }
                                    } catch(e: Exception){ Toast.makeText(this@MainActivity, "Введіть ціле число.", Toast.LENGTH_SHORT).show() }
                                }

                                if(end){
                                    main.visibility = ConstraintLayout.VISIBLE
                                    changeTargetLayout.visibility = ConstraintLayout.GONE
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        active = true
        if(!gettingPermissions){ start() }

        super.onResume()
    }

    override fun onPause() {
        active = false

        super.onPause()
    }

    override fun onDestroy() {
        active = false
        Data.main = null

        super.onDestroy()
    }

    fun isActive(): Boolean{ return active }

    fun drawDiagram(){
        with(binding){
            var counts = Data.stats.get_last_counts()

            var maxValue = 0f
            var totalCount = 0f

            var i = counts.size - 1
            do{ totalCount += counts[i]; if(counts[i] > maxValue){ maxValue = counts[i].toFloat() } } while(--i != -1)

            averageAndMax.text = "Максимум кроків: " + rewriteDigit(maxValue.toInt()) + ".\nСередня кількість: " + round(totalCount / counts.size.toFloat()) + "."; setFontSettings(averageAndMax)

            i = 0
            if(maxValue == 0f){ maxValue = 1f }
            do{
                var view = diagram.getChildAt(i)

                if(counts.size != 0){
                    var height = ((counts.removeAt(0) / maxValue) * diagram.layoutParams.height).toInt()

                    if(height != 0){
                        view.setBackgroundColor(Color.parseColor(getRandomColor()))
                        if(view.visibility == View.INVISIBLE){ view.visibility = View.VISIBLE }
                        if(height < windowSize!!){ view.layoutParams.height = height } else{ view.layoutParams.height = windowSize!! }

                        updateView(view)
                    } else{ view.visibility = View.INVISIBLE }
                } else{ view.visibility = View.INVISIBLE }
            } while(++i != 24)
        }
    }

    fun showTargets(){
        with(binding){
            var targets = arrayOf(
                Data.stats.getTarget(0, 0),
                Data.stats.getTarget(0),
                Data.stats.getTarget()
            )

            var stepsCount = listOf(
                Data.stats.getCount(0, 0),
                Data.stats.getCount(0),
                Data.stats.getCount()
            )

            var i = 2
            var listOfTextViews = listOf(daylyExp, monthlyExp, totalExp)
            var listOfHeadTextViews = listOf(daylyTarget, monthlyTarget, totalTarget)
            var listOfViews = listOf(daylyProgress, monthlyProgress, totalProgress)

            do{
                setFontSettings(listOfHeadTextViews[i])
                listOfTextViews[i].text =  rewriteDigit(stepsCount[i]) + " з " +  rewriteDigit(targets[i].toInt()); setFontSettings(listOfTextViews[i])

                var width = ((stepsCount[i] / targets[i]) * windowSize!!).toInt()
                if(width != 0){
                    if(width < windowSize!!){ listOfViews[i].layoutParams.width = width } else{ listOfViews[i].layoutParams.width = windowSize!! }
                    listOfViews[i].setBackgroundColor(Color.parseColor(getRandomColor()))

                    updateView(listOfViews[i])
                } else{ listOfViews[i].visibility = View.INVISIBLE }
            } while(--i != -1)
        }
    }

    override fun onBackPressed() {
        with(binding){
            if(main.visibility == ConstraintLayout.GONE && requestPermission.visibility == ConstraintLayout.GONE){
                main.visibility = ConstraintLayout.VISIBLE

                if(changeTargetLayout.visibility == ConstraintLayout.VISIBLE){
                    changeTargetLayout.visibility = ConstraintLayout.GONE
                }

                if(fontSettings.visibility == ConstraintLayout.VISIBLE){
                    fontSettings.visibility = ConstraintLayout.GONE
                }

                Toast.makeText(this@MainActivity, "Зміна скасована.", Toast.LENGTH_SHORT).show()

            } else{ super.onBackPressed() }
        }
    }
}