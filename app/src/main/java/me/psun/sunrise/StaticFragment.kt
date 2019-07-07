package me.psun.sunrise

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import com.skydoves.colorpickerview.ColorEnvelope
import com.skydoves.colorpickerview.ColorPickerView
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener
import com.skydoves.colorpickerview.sliders.BrightnessSlideBar
import kotlinx.android.synthetic.main.mode_static.view.*
import com.skydoves.colorpickerview.preference.ColorPickerPreferenceManager



class StaticFragment : Fragment() {
    private var colorPickerView : ColorPickerView? = null
    private var colorPickerBrightness : BrightnessSlideBar? = null
    private var colorText : TextView? = null
    private var rgbPreview : View? = null

    private var coldBar : DiscreteSlider? = null
    private var warmLevel : TextView? = null

    private var ww : Int = 0
    private var wc : Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.mode_static, container, false)
        colorPickerView = view.findViewById(R.id.colorPickerView)
        colorPickerBrightness = view.findViewById(R.id.brightnessSlide)
        colorText = view.findViewById(R.id.colorText)
        rgbPreview = view.findViewById(R.id.rgbPreview)

        coldBar = view.findViewById(R.id.coldSlider)

        val colorListener = ColorEnvelopeListener{envelope, _ ->
            colorText?.text = "#" + envelope.hexCode.substring(2)
            rgbPreview?.setBackgroundColor(envelope.color)
        }
        colorPickerView?.let {
            colorPickerBrightness?.let{brightness ->
                it.attachBrightnessSlider(brightness)
            }
            it.setLifecycleOwner(this)
            it.setColorListener(colorListener)
        }
        return view
    }
}
