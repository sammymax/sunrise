package me.psun.sunrise

import android.os.Bundle
import android.text.SpannableStringBuilder
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText

class SettingsFragment(val initialMac: String) : Fragment() {
    private var macAddress : EditText? = null
    private var saveButton : Button? = null
    private var macAddressListener : ((String) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.mode_settings, container, false)
        macAddress = view.findViewById(R.id.macAddress)
        saveButton = view.findViewById(R.id.save_settings)

        saveButton?.setOnClickListener { _ ->
            macAddress?.let {
                macAddressListener?.invoke(it.text.toString())
            }
        }
        macAddress?.text = SpannableStringBuilder(initialMac)
        return view
    }

    fun setMacAddressListener(listener : (String) -> Unit) {
        macAddressListener = listener
    }
}
