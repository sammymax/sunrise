package me.psun.sunrise

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.skydoves.colorpickerview.ColorPickerView
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener
import com.skydoves.colorpickerview.sliders.BrightnessSlideBar
import me.psun.sunrise.colorio.ColorListener
import kotlin.math.pow
import kotlin.math.roundToInt

class StaticFragment(val colorListener: ColorListener) : Fragment() {
    private var colorPickerView : ColorPickerView? = null
    private var colorPickerBrightness : BrightnessSlideBar? = null
    private var colorText : TextView? = null
    private var rgbPreview : View? = null

    private var coldBar : DiscreteSliderView? = null
    private var warmBar : DiscreteSliderView? = null
    private var gammaToggle : Switch? = null
    private var allOff : Button? = null
    private var allOn : Button? = null

    private var cw = 0
    private var ww = 0
    private var rgb = 0

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
        gammaToggle = view.findViewById(R.id.gammaSwitch)
        allOff = view.findViewById(R.id.all_off)
        allOn = view.findViewById(R.id.all_on)

        gammaToggle?.setOnCheckedChangeListener { _, _ ->
            updateColors()
        }

        coldBar?.setDiscreteValue(0)
        warmBar?.setDiscreteValue(0)
        coldBar?.setDiscreteSliderListener(object : DiscreteSliderListener{
            override fun onChange(newValue: Int, fromUser: Boolean) {
                cw = newValue
                updateColors()
            }
        })
        warmBar?.setDiscreteSliderListener(object : DiscreteSliderListener {
            override fun onChange(newValue: Int, fromUser: Boolean) {
                ww = newValue
                updateColors()
            }
        })

        colorPickerView?.let {
            colorPickerBrightness?.let{brightness ->
                it.attachBrightnessSlider(brightness)
            }
            it.setLifecycleOwner(this)
            it.setColorListener(ColorEnvelopeListener{envelope, _ ->
                colorText?.text = "#" + envelope.hexCode.substring(2)
                rgbPreview?.setBackgroundColor(envelope.color)
                rgb = envelope.color and 0x00FFFFFF
                updateColors()
            })
        }

        allOff?.setOnClickListener{ _ ->
            colorPickerBrightness?.updateSelectorX(0)
            coldBar?.setDiscreteValue(0)
            warmBar?.setDiscreteValue(0)
        }

        allOn?.setOnClickListener { _ ->
            colorPickerView?.let {
                it.selectCenter()
                it.pureColor = Color.WHITE
            }
            colorPickerBrightness?.updateSelectorX(1000)
            coldBar?.setDiscreteValue(255)
            warmBar?.setDiscreteValue(255)
        }

        return view
    }

    private fun gammaCorrect(orig: Int): Int {
        if (gammaToggle?.isChecked == false) return orig

        // convert to range [0, 1] then apply sRGB -> linear formula
        val unit = orig / 255.0
        if (unit < 0.04045) return (unit / 12.92 * 255.0).roundToInt()
        return (((unit + 0.055) / 1.055).pow(2.4) * 255).roundToInt()
    }

    private fun updateColors() {
        colorListener.setWW(gammaCorrect(ww), RootService.ColorSetSource.STATIC)
        colorListener.setCW(gammaCorrect(cw), RootService.ColorSetSource.STATIC)
        val r = gammaCorrect(rgb shr 16)
        val g = gammaCorrect((rgb shr 8) and 255)
        val b = gammaCorrect(rgb and 255)
        colorListener.setRGB((r shl 16) or (g shl 8) or b, RootService.ColorSetSource.STATIC)
    }
}
