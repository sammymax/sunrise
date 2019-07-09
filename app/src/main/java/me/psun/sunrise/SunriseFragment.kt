package me.psun.sunrise

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.android.material.floatingactionbutton.FloatingActionButton

class SunriseFragment(val appState: AppState) : Fragment() {
    private var alarmTimeShower : TextView? = null
    private var alarmAMPM : TextView? = null
    private var editButton : FloatingActionButton? = null
    private var tilSunrise : TextView? = null
    private var noAlarmOverlay : NoAlarmView? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.mode_sunrise, container, false)
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

                    val millisLeft = appState.sunrise_timeMillis - System.currentTimeMillis()
                    val hoursLeft = millisLeft / (60 * 60 * 1000)
                    val minutesLeft = (millisLeft - 60 * 60 * 1000 * hoursLeft) / (60 * 1000)
                    tilSunrise?.text = "${hoursLeft} hours, ${minutesLeft} minutes until sunrise"
                    noAlarmOverlay?.visibility = View.INVISIBLE
                }
            })
            dialog.show(fragmentManager!!, "alarmDialog")
        }
        editButton?.setOnClickListener(showDialog)
        noAlarmOverlay?.setAddAlarmFunc(showDialog)

        return view
    }
}
