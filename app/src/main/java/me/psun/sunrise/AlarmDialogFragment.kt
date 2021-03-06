package me.psun.sunrise

import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.*
import androidx.fragment.app.DialogFragment

class AlarmDialogFragment(
    private val suggestedHour: Int,
    private val suggestedMinute: Int,
    private val spinnerIdx: Int
) : DialogFragment() {
    private var buttonCancel : Button? = null
    private var buttonSave : Button? = null
    private var timePicker : TimePicker? = null
    private var alarmSpinner : Spinner? = null
    private var alarmListener : AlarmListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.alarm_dialog, container, false)
        buttonCancel = view.findViewById(R.id.dialog_cancel)
        buttonSave = view.findViewById(R.id.dialog_save)
        timePicker = view.findViewById(R.id.timePicker)
        alarmSpinner = view.findViewById(R.id.alarm_spinner)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            timePicker?.hour = suggestedHour
            timePicker?.minute = suggestedMinute
        } else {
            timePicker?.currentHour = suggestedHour
            timePicker?.currentMinute = suggestedMinute
        }

        val spinnerArray = listOf("None", "Random") + RootService.songNames
        context?.let {
            val adapter = ArrayAdapter(
                it, android.R.layout.simple_spinner_item, spinnerArray
            )
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            alarmSpinner?.adapter = adapter
            alarmSpinner?.setSelection(spinnerIdx)
        }

        buttonCancel?.setOnClickListener{_ ->
            dismiss()
        }
        buttonSave?.setOnClickListener {_ ->
            val idx = alarmSpinner?.selectedItemPosition
            timePicker?.let {
                val useNew = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                val hour = if (useNew) it.hour else it.currentHour
                val min = if (useNew) it.minute else it.currentMinute
                alarmListener?.onChange(hour, min, idx ?: 0)
            }
            dismiss()
        }
        return view
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        // on Amazon Fire 7 this otherwise causes huge top space
        dialog.window?.requestFeature(Window.FEATURE_NO_TITLE)
        return dialog
    }

    fun setAlarmListener(al: AlarmListener) {
        alarmListener = al
    }
}