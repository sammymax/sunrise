package me.psun.sunrise

import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import me.psun.sunrise.colorio.ColorListener
import java.util.*
import kotlin.math.roundToLong

class AppState(
    private val activity : Activity,
    private val colorListener : ColorListener
) {
    private val songToIdentifier : Map<String, Int>
    private val alarmManager: AlarmManager

    var frag_idx : Int = 0
    private var static_rgb : Int = Color.BLACK
    private var static_cw : Int = 0
    private var static_ww : Int = 0
    private var bpm_msPerBeat : Long = 1000000
    private var bpm_syncMillis : Long = 0
    private val bpm_handler = Handler()
    private var bpm_beatCount = 0
    private var bpm_subBeatCount = 0
    private var bpm_subdivide = 1
    var sunrise_pending : Boolean = false
        private set(value) {
            field = value
            applyWrite("sunrise.pending", value)
        }
    var sunrise_timeMillis : Long = 0
        private set(value) {
            field = value
            applyWrite("sunrise.timeMillis", value)
        }
    var sunrise_spinnerIdx : Int = 0
        private set(value) {
            field = value
            applyWrite("sunrise.spinnerIdx", value)
        }
    var settings_mac : String = ""
        set(value) {
            field = value
            applyWrite("settings.mac", value)
        }

    private val bpmRunnable = object : Runnable {
        val colors = listOf(0xC8B4BA, 0xF3DDB3, 0xC1CD97, 0xE18D96)
        override fun run() {
            if (++bpm_subBeatCount == bpm_subdivide) {
                bpm_beatCount++
                bpm_subBeatCount = 0
            }
            colorListener.setRGB(colors[bpm_beatCount and 3], ColorSetSource.BPM)

            val msTilNextBeat = bpm_msPerBeat - (System.currentTimeMillis() % bpm_msPerBeat)
            bpm_handler.postDelayed(this, msTilNextBeat)
        }
    }

    enum class ColorSetSource {
        STATIC, BPM, SUNRISE
    }

    constructor(activity: Activity, colorListener: ColorListener, prefs : SharedPreferences) : this(activity, colorListener) {
        prefs.getString("settings.mac", "")?.let{ settings_mac = it }
        sunrise_pending = prefs.getBoolean("sunrise.pending", false)
        sunrise_timeMillis = prefs.getLong("sunrise.timeMillis", 0)
        sunrise_spinnerIdx = prefs.getInt("sunrise.spinnerIdx", 0)
    }

    init {
        songToIdentifier = songDict.mapValues { (_, value) ->
            activity.resources.getIdentifier(value, "raw", activity.packageName)
        }
        alarmManager = activity.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    }

    fun staticSetRGB(rgb: Int) {
        static_rgb = rgb
        colorListener.setRGB(static_rgb, ColorSetSource.STATIC)
        colorListener.setCW(static_cw, ColorSetSource.STATIC)
        colorListener.setWW(static_ww, ColorSetSource.STATIC)
    }

    fun staticSetCW(cw: Int) {
        static_cw = cw
    }

    fun staticSetWW(ww: Int) {
        static_ww = ww
    }

    fun bpmChange(bpm: Double) {
        if (bpm_msPerBeat > 1e4) bpm_syncMillis = System.currentTimeMillis()
        bpm_msPerBeat = (60000.0 / bpm).roundToLong()
        bpm_handler.removeCallbacks(bpmRunnable)
        bpm_handler.postDelayed(bpmRunnable, bpm_msPerBeat)
    }

    fun bpmSync() {
        bpm_syncMillis = System.currentTimeMillis()
        bpm_handler.removeCallbacks(bpmRunnable)
        bpm_handler.postDelayed(bpmRunnable, bpm_msPerBeat)
    }

    fun setSunrise(hour: Int, minute: Int, idx: Int) {
        val c = GregorianCalendar()
        if (c.get(GregorianCalendar.HOUR_OF_DAY) > hour ||
            (c.get(GregorianCalendar.HOUR_OF_DAY) == hour && c.get(GregorianCalendar.MINUTE) >= minute))
            c.add(GregorianCalendar.DATE, 1)
        c.set(GregorianCalendar.HOUR_OF_DAY, hour)
        c.set(GregorianCalendar.MINUTE, minute)
        c.set(GregorianCalendar.SECOND, 0)
        c.set(GregorianCalendar.MILLISECOND, 0)
        sunrise_timeMillis = c.timeInMillis
        sunrise_spinnerIdx = idx
        sunrise_pending = true

        val pendingIntent = createPendingAlarmIntent()
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, sunrise_timeMillis, pendingIntent)
    }

    fun delSunrise() {
        sunrise_pending = false
        val pendingIntent = createPendingAlarmIntent()
        alarmManager.cancel(pendingIntent)
    }

    fun snoozeAlarm() {
        // snooze for 10 minutes
        sunrise_timeMillis = System.currentTimeMillis() + 3 * 60 * 100
        sunrise_pending = true
        val pendingIntent = createPendingAlarmIntent()
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, sunrise_timeMillis, pendingIntent)
    }

    fun getSettingsBundle() : Bundle {
        val b = Bundle()
        b.putString("settings.mac", settings_mac)
        return b
    }

    private fun getSongIdentifier(): Int {
        return when(sunrise_spinnerIdx) {
            0 -> NO_SOUND_ID
            1 -> songToIdentifier[songDict.keys.random()]
            else -> songToIdentifier[songNames[sunrise_spinnerIdx - 2]]
        }!!
    }

    private fun createPendingAlarmIntent(): PendingIntent {
        val intent = Intent(activity.applicationContext, RingingAlarmActivity::class.java).apply {
            putExtra(ALARM_ON, true)
            putExtra("SongIdentifier", getSongIdentifier())
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        return PendingIntent.getActivity(activity.applicationContext, 0, intent, PendingIntent.FLAG_ONE_SHOT)
    }

    private fun applyWrite(key: String, value: Any) {
        with (activity.getPreferences(Context.MODE_PRIVATE).edit()) {
            when (value) {
                is Boolean -> putBoolean(key, value)
                is Int -> putInt(key, value)
                is Float -> putFloat(key, value)
                is Long -> putLong(key, value)
                is String -> putString(key, value)
            }
            apply()
        }
    }

    companion object {
        val songDict = mapOf(
            "Martin Garrix - Poison" to "poison",
            "Swedish House Mafia & Knife Party - Antidote" to "antidote",
            "Blasterjaxx - Big Bird (DVLM Tomorrowland 2016 Edit)" to "big_bird",
            "Kanye West - Mercy (RL Grime and Salva Remix)" to "mercy",
            "Sheck Wes - Mo Bamba" to "mo_bamba",
            "DVLM & Martin Garrix - Tremor" to "tremor",
            "Android - Full of Wonder" to "full_of_wonder",
            "Android - Gentle Breeze" to "gentle_breeze",
            "Android - Icicles" to "icicles",
            "Android - Sunshower" to "sunshower"
        )
        val songNames = songDict.keys.toList()
        const val NO_SOUND_ID = -1234567
        const val ALARM_ON = "alarm.show"
        const val ALARM_OFF_ACTION_OFF = "alarm.off"
        const val ALARM_OFF_ACTION_SNOOZE = "alarm.snooze"
    }
}