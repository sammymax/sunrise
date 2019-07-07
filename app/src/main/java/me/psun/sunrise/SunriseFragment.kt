package me.psun.sunrise

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Spinner
import android.widget.ArrayAdapter



class SunriseFragment : Fragment() {
    private var spinner : Spinner? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.mode_sunrise, container, false)
        spinner = view.findViewById(R.id.alarm_spinner)
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
