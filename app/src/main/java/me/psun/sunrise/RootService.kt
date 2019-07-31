package me.psun.sunrise

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import androidx.preference.PreferenceManager
import me.psun.sunrise.colorio.ColorListener
import me.psun.sunrise.colorio.H801ColorListener
import me.psun.sunrise.colorio.MultiColorListener
import java.util.*
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToLong

class RootService: Service() {
    private val myBinder = LocalBinder()
    override fun onBind(intent: Intent?): IBinder? {
        return myBinder
    }

    override fun onCreate() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        prefs.getString("settings.mac", "")?.let{ settings_mac = it }
        sunrise_pending = prefs.getBoolean("sunrise.pending", false)
        sunrise_timeMillis = prefs.getLong("sunrise.timeMillis", 0)
        sunrise_spinnerIdx = prefs.getInt("sunrise.spinnerIdx", 0)


        songToIdentifier = songDict.mapValues { (_, value) ->
            resources.getIdentifier(value, "raw", packageName)
        }
        alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val sunriseUpdate = intent?.extras?.getBoolean(SUNRISE_UPDATE, false)
        if (sunriseUpdate == true)
            updateSunriseBrightness()
        return START_NOT_STICKY
    }

    inner class LocalBinder : Binder() {
        fun getService(): RootService {
            return this@RootService
        }
    }
////////////////////////////////////////////////////////////////////////////////
    private var songToIdentifier : Map<String, Int> = mapOf()
    private var alarmManager: AlarmManager? = null
    private val colorListener = MultiColorListener(H801ColorListener{settings_mac})

    var fragIdx : Int = 0
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
        val colors = listOf(0xFF0000, 0x00FF00, 0xFFA500, 0x0000FF)
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

    fun addColorListener(cl: ColorListener) {
        colorListener.addListener(cl)
    }

    fun staticSetRGB(rgb: Int) {
        bpm_handler.removeCallbacks(bpmRunnable)
        static_rgb = rgb
        colorListener.setRGB(static_rgb, ColorSetSource.STATIC)
        colorListener.setCW(static_cw, ColorSetSource.STATIC)
        colorListener.setWW(static_ww, ColorSetSource.STATIC)
    }

    fun staticSetCW(cw: Int) {
        bpm_handler.removeCallbacks(bpmRunnable)
        static_cw = cw
        colorListener.setRGB(static_rgb, ColorSetSource.STATIC)
        colorListener.setCW(static_cw, ColorSetSource.STATIC)
        colorListener.setWW(static_ww, ColorSetSource.STATIC)
    }

    fun staticSetWW(ww: Int) {
        bpm_handler.removeCallbacks(bpmRunnable)
        static_ww = ww
        colorListener.setRGB(static_rgb, ColorSetSource.STATIC)
        colorListener.setCW(static_cw, ColorSetSource.STATIC)
        colorListener.setWW(static_ww, ColorSetSource.STATIC)
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
        alarmManager?.setExact(AlarmManager.RTC_WAKEUP, sunrise_timeMillis, pendingIntent)
        updateSunriseBrightness()
    }

    fun delSunrise() {
        sunrise_pending = false
        val pendingIntent = createPendingAlarmIntent()
        alarmManager?.cancel(pendingIntent)
        val sunriseIntent = createSunriseIntent()
        alarmManager?.cancel(sunriseIntent)
    }

    fun snoozeAlarm() {
        // snooze for 10 minutes
        sunrise_timeMillis = System.currentTimeMillis() + 3 * 60 * 100
        sunrise_pending = true
        val pendingIntent = createPendingAlarmIntent()
        alarmManager?.setExact(AlarmManager.RTC_WAKEUP, sunrise_timeMillis, pendingIntent)
    }

    private fun getSongIdentifier(): Int {
        return when(sunrise_spinnerIdx) {
            0 -> NO_SOUND_ID
            1 -> songToIdentifier[songDict.keys.random()]
            else -> songToIdentifier[songNames[sunrise_spinnerIdx - 2]]
        }!!
    }

    private fun createPendingAlarmIntent(): PendingIntent {
        val intent = Intent(applicationContext, RingingAlarmActivity::class.java).apply {
            putExtra(ALARM_ON, true)
            putExtra("SongIdentifier", getSongIdentifier())
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        return PendingIntent.getActivity(applicationContext, 0, intent, PendingIntent.FLAG_ONE_SHOT)
    }

    private fun createSunriseIntent(): PendingIntent {
        val intent = Intent(applicationContext, RootService::class.java).apply {
            putExtra(SUNRISE_UPDATE, true)
        }
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            PendingIntent.getForegroundService(applicationContext, 0, intent, 0)
        } else {
            PendingIntent.getService(applicationContext, 0, intent, 0)
        }
    }

    private fun updateSunriseBrightness() {
        if (!sunrise_pending) return
        val currentMs = GregorianCalendar().timeInMillis
        val progress = 1.0 - (sunrise_timeMillis - currentMs).toDouble() / SUNRISE_MS
        if (progress > 0) {
            val finalRGB = 0x250900
            val finalWW = 30
            bpm_handler.removeCallbacks(bpmRunnable)
            if (progress < 0.8) {
                val scaledProgress = (progress * 1.25).pow(4.0)
                colorListener.setCW(0, ColorSetSource.SUNRISE)
                colorListener.setWW(0, ColorSetSource.SUNRISE)
                val r = ((finalRGB shr 16) * scaledProgress).toInt()
                val g = (((finalRGB shr 8) and 255) * scaledProgress).toInt()
                val b = ((finalRGB and 255) * scaledProgress).toInt()
                colorListener.setRGB((r shl 16) or (g shl 8) or b, ColorSetSource.SUNRISE)
            } else {
                val curWarm = min(finalWW, ((progress - 0.8) * 5 * finalWW).toInt())
                colorListener.setRGB(finalRGB, ColorSetSource.SUNRISE)
                colorListener.setCW(0, ColorSetSource.SUNRISE)
                colorListener.setWW(curWarm, ColorSetSource.SUNRISE)
            }
        }

        if (progress > 1.0) return
        val sunriseIntent = createSunriseIntent()
        val nextUpdateTime = max(sunrise_timeMillis - SUNRISE_MS, currentMs + 5 * 1000)
        alarmManager?.setExact(AlarmManager.RTC_WAKEUP, nextUpdateTime, sunriseIntent)
    }

    private fun applyWrite(key: String, value: Any) {
        with (PreferenceManager.getDefaultSharedPreferences(this).edit()) {
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
        const val SUNRISE_MS = 15 * 60 * 1000
        const val NO_SOUND_ID = -1234567
        const val SUNRISE_UPDATE = "sunrise.update"
        const val ALARM_ON = "alarm.show"
        const val ALARM_OFF_ACTION_OFF = "alarm.off"
        const val ALARM_OFF_ACTION_SNOOZE = "alarm.snooze"
    }
}