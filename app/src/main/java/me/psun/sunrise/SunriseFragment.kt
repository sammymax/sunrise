package me.psun.sunrise

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Spinner
import android.widget.ArrayAdapter
import android.widget.TimePicker
import java.util.*
import android.content.Context.ALARM_SERVICE
import androidx.core.content.ContextCompat.getSystemService
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent


class SunriseFragment : Fragment() {
    private var spinner : Spinner? = null
    private var timePicker : TimePicker? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.mode_sunrise, container, false)
        spinner = view.findViewById(R.id.alarm_spinner)
        timePicker = view.findViewById(R.id.timePicker)

        timePicker?.setOnTimeChangedListener{_, hour, minute ->
            Log.e("times", "yoooo $hour:$minute")
            val c = GregorianCalendar()
            Log.e("times", "yooo2 ${c.get(GregorianCalendar.HOUR_OF_DAY)}:${c.get(GregorianCalendar.MINUTE)}")
            Log.e("times", c.timeInMillis.toString())
            if (c.get(GregorianCalendar.HOUR_OF_DAY) > hour || (c.get(GregorianCalendar.HOUR_OF_DAY) == hour && c.get(GregorianCalendar.MINUTE) >= minute))
                c.add(GregorianCalendar.DATE, 1)
            c.set(GregorianCalendar.HOUR_OF_DAY, hour)
            c.set(GregorianCalendar.MINUTE, minute)
            c.set(GregorianCalendar.SECOND, 0)
            c.set(GregorianCalendar.MILLISECOND, 0)
            Log.e("times", c.timeInMillis.toString())

            val alarm = activity!!.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val pendingIntent = PendingIntent.getBroadcast(context, 0, Intent(context, RingingAlarm::class.java), 0)
            alarm.setExact(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pendingIntent)
        }

        val spinnerArray = listOf("Martin Garrix - Poision", "DVLM & Martin Garrix - Tremor", "Sheck Wes - Mo Bamba", "Swedish House Mafia & Knife Party - Antidote")
        context?.let {
            val adapter = ArrayAdapter<String>(
                it, android.R.layout.simple_spinner_item, spinnerArray
            )
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner?.adapter = adapter
        }
        return view
    }
}
