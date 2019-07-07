package me.psun.sunrise

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
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
    private var warmBar : DiscreteSlider? = null
    private var cw = 0
    private var ww = 0

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
        warmBar = view.findViewById(R.id.warmSlider)

        coldBar?.setDiscreteValue(cw)
        warmBar?.setDiscreteValue(ww)
        coldBar?.setDiscreteSliderListener(object : DiscreteSliderListener{
            override fun onChange(newValue: Int) {
                cw = newValue
            }
        })
        warmBar?.setDiscreteSliderListener(object : DiscreteSliderListener {
            override fun onChange(newValue: Int) {
                ww = newValue
            }
        })

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
