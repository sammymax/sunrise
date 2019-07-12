package me.psun.sunrise

import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.*

class SunriseFragment(val appState: AppState) : Fragment() {
    private var alarmTimeShower : TextView? = null
    private var alarmAMPM : TextView? = null
    private var delButton : FloatingActionButton? = null
    private var editButton : FloatingActionButton? = null
    private var tilSunrise : TextView? = null
    private var noAlarmOverlay : NoAlarmView? = null
    private var isAlarmSound : TextView? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.mode_sunrise, container, false)
        delButton = view.findViewById(R.id.alarm_delete)
        editButton = view.findViewById(R.id.alarm_edit)
        alarmTimeShower = view.findViewById(R.id.alarm_time)
        alarmAMPM = view.findViewById(R.id.alarm_am_pm)
        tilSunrise = view.findViewById(R.id.til_sunrise)
        noAlarmOverlay = view.findViewById(R.id.no_alarm_overlay)
        isAlarmSound = view.findViewById(R.id.is_alarm_sound)

        if (appState.sunrise_pending) updateAlarmTime()

        val showDialog = View.OnClickListener{_ ->
            // show current time + 1 hour if adding alarm, existing alarm time if editing
            val suggestedAlarmMillis = if (appState.sunrise_pending) appState.sunrise_timeMillis else System.currentTimeMillis() + 60 * 60 * 1000
            val hourMinute = getHourMinuteFromTimeStamp(suggestedAlarmMillis)
            val dialog = AlarmDialogFragment(hourMinute.first, hourMinute.second, appState.sunrise_spinnerIdx)
            dialog.setAlarmListener(object : AlarmListener{
                override fun onChange(hour: Int, minute: Int, spinnerIdx: Int) {
                    appState.setSunrise(hour, minute, spinnerIdx)
                    updateAlarmTime()
                    updateViewToState()
                }
            })
            dialog.show(fragmentManager!!, "alarmDialog")
        }
        editButton?.setOnClickListener(showDialog)
        noAlarmOverlay?.setAddAlarmFunc(showDialog)
        delButton?.setOnClickListener { _ ->
            AlertDialog.Builder(context).setMessage("Delete this alarm?")
                .setPositiveButton("Yes") {_, _ ->
                    appState.delSunrise()
                    updateViewToState()
                }
                .setNegativeButton("No", null)
                .show()
        }
        val handler = Handler()
        // this runnable handles initial set up as well as keeping time til sunrise accurate
        handler.post(object : Runnable {
            override fun run() {
                updateViewToState()
                handler.postDelayed(this, msTilNextMinute())
            }
        })

        return view
    }

    fun updateViewToState() {
        if (!appState.sunrise_pending) {
            noAlarmOverlay?.visibility = View.VISIBLE
            return
        }
        if (appState.sunrise_spinnerIdx == 0)
            isAlarmSound?.text = "off"
        else
            isAlarmSound?.text = "on"

        noAlarmOverlay?.visibility = View.INVISIBLE
        val millisLeft = appState.sunrise_timeMillis - System.currentTimeMillis()
        val hoursLeft = millisLeft / (60 * 60 * 1000)
        val minutesLeft = (millisLeft - 60 * 60 * 1000 * hoursLeft) / (60 * 1000)
        tilSunrise?.text = "${hoursLeft} hours, ${minutesLeft} minutes until sunrise"
    }

    fun updateAlarmTime() {
        val hourMinutePair = getHourMinuteFromTimeStamp(appState.sunrise_timeMillis)
        val hour = hourMinutePair.first
        val minute = hourMinutePair.second

        if (hour < 12) alarmAMPM?.text = "AM"
        else alarmAMPM?.text = "PM"

        var displayedHour = hour
        if (hour > 12) displayedHour -= 12
        if (hour == 0) displayedHour = 12
        alarmTimeShower?.text = "$displayedHour:${minute.toString().padStart(2, '0')}"
    }

    private fun msTilNextMinute(): Long {
        return 60 * 1000 - System.currentTimeMillis() % (60 * 1000)
    }

    private fun getHourMinuteFromTimeStamp(ts: Long): Pair<Int, Int> {
        val c = Calendar.getInstance()
        c.timeInMillis = ts
        return Pair(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE))
    }
}
