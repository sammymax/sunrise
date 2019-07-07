package me.psun.sunrise

import android.os.Bundle
import android.text.SpannableStringBuilder
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText

class SettingsFragment : Fragment() {
    private var macAddress : EditText? = null
    private var saveButton : Button? = null
    private var macAddressListener : MacAddressListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.mode_settings, container, false)
        macAddress = view.findViewById(R.id.macAddress)
        saveButton = view.findViewById(R.id.save_settings)

        arguments?.let {
            it.getString("settings.mac")?.let { mac ->
                macAddress?.text = SpannableStringBuilder(mac)
            }
        }
        saveButton?.setOnClickListener { _ ->
            macAddress?.let {
                macAddressListener?.onMacAddressChange(it.text.toString())
            }
        }
        return view
    }

    fun setMacAddressListener(listener : MacAddressListener) {
        macAddressListener = listener
    }

    fun setMacAddress(mac : String) {
        macAddress?.text = SpannableStringBuilder(mac)
    }
}
