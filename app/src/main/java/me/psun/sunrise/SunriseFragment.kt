package me.psun.sunrise

import android.os.Bundle
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
import android.widget.AdapterView

class SunriseFragment : Fragment() {
    private var spinner : Spinner? = null
    private var timePicker : TimePicker? = null
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
    private var songToIdentifier : Map<String, Int> = mapOf()
    private var songId : Int? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.mode_sunrise, container, false)
        spinner = view.findViewById(R.id.alarm_spinner)
        timePicker = view.findViewById(R.id.timePicker)
        context?.let {
            songToIdentifier = songDict.mapValues { kv ->
                it.resources.getIdentifier(kv.value, "raw", it.packageName)
            }
        }

        timePicker?.setOnTimeChangedListener{_, hour, minute ->
            val c = GregorianCalendar()
            if (c.get(GregorianCalendar.HOUR_OF_DAY) > hour || (c.get(GregorianCalendar.HOUR_OF_DAY) == hour && c.get(GregorianCalendar.MINUTE) >= minute))
                c.add(GregorianCalendar.DATE, 1)
            c.set(GregorianCalendar.HOUR_OF_DAY, hour)
            c.set(GregorianCalendar.MINUTE, minute)
            c.set(GregorianCalendar.SECOND, 0)
            c.set(GregorianCalendar.MILLISECOND, 0)

            val alarm = activity!!.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, RingingAlarm::class.java)
            intent.putExtra("SongIdentifier", songId ?: -1)
            val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_ONE_SHOT)
            alarm.setExact(AlarmManager.RTC_WAKEUP, c.timeInMillis + 2500, pendingIntent)
        }

        val spinnerArray = listOf("None", "Random") + songDict.keys.toList()
        context?.let {
            val adapter = ArrayAdapter<String>(
                it, android.R.layout.simple_spinner_item, spinnerArray
            )
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner?.adapter = adapter
        }
        spinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {}

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, idx: Int, id: Long) {
                if (idx == 0) {
                    songId = null
                    return
                }
                val numSongs = spinnerArray.size - 2
                var songIdx = idx
                if (idx == 1) songIdx = Random().nextInt(numSongs) + 2
                songId = songToIdentifier[spinnerArray[songIdx]]
            }
        }
        return view
    }
}
