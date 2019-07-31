package me.psun.sunrise

import android.os.Bundle
import android.os.SystemClock
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import androidx.collection.CircularArray
import kotlin.math.pow
import kotlin.math.roundToInt

class BPMFragment : Fragment() {
    private var bpmTap : Button? = null
    private var bpmSync : Button? = null
    private var bpmHalve : Button? = null
    private var bpmDouble : Button? = null

    private var quantize : Switch? = null
    private var bpmShow : TextView? = null
    private var bpmPreview : TextView? = null

    private val taps  = CircularArray<Long>()
    // output bpm = calculated bpm * (2 ^ bpm_exp)
    private var bpmExp : Int = 0
    private var quant: Boolean = false
    private var listener : BeatListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.mode_bpm, container, false)
        bpmTap = view.findViewById(R.id.bpm_tap)
        bpmSync = view.findViewById(R.id.bpm_sync)
        bpmHalve = view.findViewById(R.id.bpm_halve)
        bpmDouble = view.findViewById(R.id.bpm_double)
        quantize = view.findViewById(R.id.quantize)
        bpmShow = view.findViewById(R.id.bpm_display)
        bpmPreview = view.findViewById(R.id.bpm_preview)

        bpmTap?.setOnClickListener{_ ->
            taps.addLast(SystemClock.elapsedRealtime())
            recalc()
        }
        bpmSync?.setOnClickListener{_ ->
            listener?.bpmSync()
        }
        bpmHalve?.setOnClickListener {_ ->
            if (bpmExp > -5) {
                bpmExp--
                recalc()
            }
        }
        bpmDouble?.setOnClickListener{_ ->
            if (bpmExp < 5) {
                bpmExp++
                recalc()
            }
        }
        quantize?.setOnCheckedChangeListener{_, isChecked ->
            quant = isChecked
            recalc()
        }
        return view
    }

    private fun recalc() {
        val bpm : Double = when (taps.size()) {
            0 -> 0.0
            1 -> {
                listener?.bpmSync()
                0.0
            }
            else -> {
                if (taps.size() >= 5) taps.popFirst()
                // time was in ms; 0.001 to get to seconds
                val duration = 0.001 * (taps.last - taps.first)
                val numBeats = taps.size() - 1.0
                val multiplier = 2.0.pow(bpmExp.toDouble())
                val bpm = 60.0 * multiplier * numBeats / duration
                if (quant) bpm.roundToInt().toDouble()
                else bpm
            }
        }
        listener?.bpmChange(bpm)
        if (bpm > 0) bpmShow?.text = String.format("BPM: %.1f", bpm)
        else bpmShow?.text = "BPM: -"
    }

    fun setBeatListener(bl : BeatListener) {
        listener = bl
    }

    fun setPreviewRGB(rgb: Int) {
        bpmPreview?.setBackgroundColor(0xFF000000.toInt() or rgb)
        bpmPreview?.setTextColor(rgb.inv())
    }
}
