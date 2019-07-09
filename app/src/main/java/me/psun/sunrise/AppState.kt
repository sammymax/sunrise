package me.psun.sunrise

import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import java.io.Serializable
import java.util.*

class AppState(
    private val activity : Activity
) {
    var frag_idx : Int = 0
    var static_rgb : Int = Color.BLACK
    var static_cw : Int = 0
    var static_ww : Int = 0
    var sunrise_setState : AlarmSetState = AlarmSetState.NONE
    var sunrise_timeMillis : Long = 0
        get() = field
    var sunrise_soundId : Int? = 0
    var settings_mac : String = ""

    enum class AlarmSetState {
        NONE, PENDING, ACTIVE
    }

    constructor(activity: Activity, prefs : SharedPreferences) : this(activity) {
        prefs.getString("settings.mac", "")?.let{ settings_mac = it}
    }

    fun setSunrise(hour: Int, minute: Int, id: Int?) {
        val c = GregorianCalendar()
        if (c.get(GregorianCalendar.HOUR_OF_DAY) > hour ||
            (c.get(GregorianCalendar.HOUR_OF_DAY) == hour && c.get(GregorianCalendar.MINUTE) >= minute))
            c.add(GregorianCalendar.DATE, 1)
        c.set(GregorianCalendar.HOUR_OF_DAY, hour)
        c.set(GregorianCalendar.MINUTE, minute)
        c.set(GregorianCalendar.SECOND, 0)
        c.set(GregorianCalendar.MILLISECOND, 0)
        sunrise_timeMillis = c.timeInMillis
        sunrise_soundId = id
        sunrise_setState = AlarmSetState.PENDING

        val alarm = activity.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(activity.applicationContext, RingingAlarmActivity::class.java)
        intent.putExtra("SongIdentifier", id ?: -1)
        val pendingIntent = PendingIntent.getActivity(activity.applicationContext, 0, intent, PendingIntent.FLAG_ONE_SHOT)
        alarm.setExact(AlarmManager.RTC_WAKEUP, c.timeInMillis, pendingIntent)
    }

    fun delSunrise() {
        sunrise_setState = AlarmSetState.NONE
    }

    fun getSettingsBundle() : Bundle {
        val b = Bundle()
        b.putString("settings.mac", settings_mac)
        return b
    }
}