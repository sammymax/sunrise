package me.psun.sunrise

import android.animation.AnimatorInflater
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.os.PowerManager
import android.widget.TableLayout
import android.widget.TableRow
import kotlin.random.Random

class RingingAlarmActivity : Activity() {
    var mediaPlayer : MediaPlayer? = null
    var wakelock : PowerManager.WakeLock? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window.decorView.apply {
            systemUiVisibility = View.SYSTEM_UI_FLAG_IMMERSIVE or
                    // layout stuff
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    // actual look
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setTurnScreenOn(true)
            setShowWhenLocked(true)
        } else {
            window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
            window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
        }
        setContentView(R.layout.ringing_alarm)

        val grid = findViewById<TableLayout>(R.id.ringing_grid)
        val numButtons = grid.childCount * (grid.getChildAt(0) as TableRow).childCount
        val numSnooze = numButtons / 2
        val idxToSnooze = (0 until numButtons).shuffled().take(numSnooze).toSet()
        var idx = 0
        for (i in 0 until grid.childCount) {
            val row = grid.getChildAt(i) as TableRow
            for (j in 0 until row.childCount) {
                val button = row.getChildAt(j) as Button
                if (idx in idxToSnooze) {
                    button.text = "Snooze"
                    button.setBackgroundColor(0xFFFFFFFF.toInt())
                    button.setTextColor(0xFF000000.toInt())
                    button.setOnClickListener{_ ->
                        stopAlarm(true)
                    }
                    AnimatorInflater.loadAnimator(this, R.animator.alarm_flash_snooze).apply{
                        startDelay = Random.nextInt(2000).toLong()
                        duration = Random.nextInt(1800, 2800).toLong()
                        setTarget(button)
                        start()
                    }
                } else {
                    button.text = "Dismiss"
                    button.setBackgroundColor(0xFF1A9185.toInt())
                    button.setOnClickListener{_ ->
                        stopAlarm(false)
                    }
                    AnimatorInflater.loadAnimator(this, R.animator.alarm_flash_dismiss).apply{
                        startDelay = Random.nextInt(2000).toLong()
                        duration = Random.nextInt(1800, 2800).toLong()
                        setTarget(button)
                        start()
                    }
                }
                idx++
            }
        }
        val songId = intent.getIntExtra("SongIdentifier", RootService.NO_SOUND_ID)
        if (songId != RootService.NO_SOUND_ID) {
            mediaPlayer = MediaPlayer.create(this, songId)
            mediaPlayer?.isLooping = true
        }

        val pm = applicationContext.getSystemService(Context.POWER_SERVICE) as PowerManager
        wakelock = pm.newWakeLock(
            PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.SCREEN_BRIGHT_WAKE_LOCK,
            "sunrise:Alarm"
        )
        wakelock?.acquire()
        mediaPlayer?.start()
    }

    private fun stopAlarm (snooze: Boolean) {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        wakelock?.release()
        wakelock = null

        val intent = Intent(this, RootActivity::class.java).apply {
            putExtra(RootService.ALARM_OFF_ACTION_OFF, true)
            putExtra(RootService.ALARM_OFF_ACTION_SNOOZE, snooze)
            addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        }
        startActivity(intent)
        finish()
    }
}