package me.psun.sunrise

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.os.PowerManager

class RingingAlarmActivity : Activity() {
    var off : Button? = null
    var mediaPlayer : MediaPlayer? = null
    var wakelock : PowerManager.WakeLock? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val songId = intent.getIntExtra("SongIdentifier", AppState.NO_SOUND_ID)
        if (songId != AppState.NO_SOUND_ID) {
            mediaPlayer = MediaPlayer.create(this, songId)
            mediaPlayer?.isLooping = true
        }

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

        val pm = applicationContext.getSystemService(Context.POWER_SERVICE) as PowerManager
        wakelock = pm.newWakeLock(
            PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.SCREEN_BRIGHT_WAKE_LOCK,
            "sunrise:Alarm"
        )
        wakelock?.acquire()
        mediaPlayer?.start()

        off = findViewById(R.id.alarm_off)
        off?.setOnClickListener{_ ->
            stopAlarm(false)
        }
    }

    fun stopAlarm (snooze: Boolean) {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        wakelock?.release()
        wakelock = null

        val intent = Intent(AppState.ALARM_OFF_ACTION)
        intent.putExtra(AppState.ALARM_OFF_ACTION_SNOOZE, snooze)
        sendBroadcast(intent)
        finishAndRemoveTask()
    }
}