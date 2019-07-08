package me.psun.sunrise

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.DialogFragment

class AlarmDialogFragment : DialogFragment() {
    private var buttonCancel : Button? = null
    private var buttonSave : Button? = null
    private var timePicker : TimePicker? = null
    private var alarmSpinner : Spinner? = null
    private var alarmListener : AlarmListener? = null

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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.alarm_dialog, container, false)
        buttonCancel = view.findViewById(R.id.dialog_cancel)
        buttonSave = view.findViewById(R.id.dialog_save)
        timePicker = view.findViewById(R.id.timePicker)
        alarmSpinner = view.findViewById(R.id.alarm_spinner)

        context?.let {
            songToIdentifier = songDict.mapValues { kv ->
                it.resources.getIdentifier(kv.value, "raw", it.packageName)
            }
        }

        val spinnerArray = listOf("None", "Random") + songDict.keys.toList()
        context?.let {
            val adapter = ArrayAdapter<String>(
                it, android.R.layout.simple_spinner_item, spinnerArray
            )
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            alarmSpinner?.adapter = adapter
        }

        buttonCancel?.setOnClickListener{_ ->
            dismiss()
        }
        buttonSave?.setOnClickListener {_ ->
            val idx = alarmSpinner?.selectedItemPosition
            val id = when(idx) {
                null -> null
                0 -> null
                1 -> songToIdentifier[songDict.keys.random()]
                else -> songToIdentifier[spinnerArray[idx]]
            }
            timePicker?.let {
                val useNew = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                val hour = if (useNew) it.hour else it.currentHour
                val min = if (useNew) it.minute else it.currentMinute
                alarmListener?.onChange(hour, min, id)
            }
            dismiss()
        }
        return view
    }

    fun setAlarmListener(al: AlarmListener) {
        alarmListener = al
    }
}