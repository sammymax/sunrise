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
            val c = GregorianCalendar()
            /*if (c.get(GregorianCalendar.HOUR_OF_DAY) > hour || (c.get(GregorianCalendar.HOUR_OF_DAY) == hour && c.get(GregorianCalendar.MINUTE) >= minute))
                c.add(GregorianCalendar.DATE, 1)
            c.set(GregorianCalendar.HOUR_OF_DAY, hour)
            c.set(GregorianCalendar.MINUTE, minute)
            c.set(GregorianCalendar.SECOND, 0)
            c.set(GregorianCalendar.MILLISECOND, 0)
            Log.e("times", c.timeInMillis.toString())*/

            /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Log.e("ad", Settings.canDrawOverlays(context).toString())
            }
            try {
                val mgr = context?.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                val viewToAdd = View(context)
                val params = WindowManager.LayoutParams(
                    0,
                    0,
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                    else
                        WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSPARENT
                )
                viewToAdd.layoutParams = params
                mgr.addView(viewToAdd, params)
                mgr.removeView(viewToAdd)
                Log.e("succ", "succ")
            } catch (e: Exception) {
                e.printStackTrace()
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Log.e("ad", Settings.canDrawOverlays(context).toString())
            }*/
            val alarm = activity!!.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            //val pendingIntent = PendingIntent.getBroadcast(context, 0, Intent(context, RingingAlarmReceiver::class.java), 0)
            val pendingIntent = PendingIntent.getActivity(context, 0, Intent(context, RingingAlarm::class.java), 0)
            //val pendingIntent = PendingIntent.getActivity(context, 0, Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION), 0)
            alarm.setExact(AlarmManager.RTC_WAKEUP, c.timeInMillis + 2500, pendingIntent)
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
