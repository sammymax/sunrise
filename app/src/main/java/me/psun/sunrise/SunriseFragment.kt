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

class SunriseFragment(val appState: AppState) : Fragment() {
    private var alarmTimeShower : TextView? = null
    private var alarmAMPM : TextView? = null
    private var delButton : FloatingActionButton? = null
    private var editButton : FloatingActionButton? = null
    private var tilSunrise : TextView? = null
    private var noAlarmOverlay : NoAlarmView? = null

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

        val showDialog = View.OnClickListener{_ ->
            val dialog = AlarmDialogFragment()
            dialog.setAlarmListener(object : AlarmListener{
                override fun onChange(hour: Int, minute: Int, soundId: Int?) {
                    if (hour < 12) alarmAMPM?.text = "AM"
                    else alarmAMPM?.text = "PM"

                    var displayedHour = hour
                    if (hour > 12) displayedHour -= 12
                    if (hour == 0) displayedHour = 12
                    alarmTimeShower?.text = "$displayedHour:${minute.toString().padStart(2, '0')}"

                    appState.setSunrise(hour, minute, soundId)
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
        handler.postDelayed(object : Runnable {
            override fun run() {
                updateViewToState()
                handler.postDelayed(this, msTilNextMinute())
            }
        }, msTilNextMinute())

        return view
    }

    private fun updateViewToState() {
        if (appState.sunrise_setState == AppState.AlarmSetState.NONE) {
            noAlarmOverlay?.visibility = View.VISIBLE
            return
        }
        noAlarmOverlay?.visibility = View.INVISIBLE
        val millisLeft = appState.sunrise_timeMillis - System.currentTimeMillis()
        val hoursLeft = millisLeft / (60 * 60 * 1000)
        val minutesLeft = (millisLeft - 60 * 60 * 1000 * hoursLeft) / (60 * 1000)
        tilSunrise?.text = "${hoursLeft} hours, ${minutesLeft} minutes until sunrise"
    }

    private fun msTilNextMinute(): Long {
        return 60 * 1000 - System.currentTimeMillis() % (60 * 1000)
    }
}
