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

class BPMFragment : Fragment() {
    var bpm_tap : Button? = null
    var bpm_sync : Button? = null
    var bpm_halve : Button? = null
    var bpm_double : Button? = null

    var quantize : Switch? = null
    var bpm_show : TextView? = null

    val taps  = CircularArray<Long>()
    // output bpm = calculated bpm * (2 ^ bpm_exp)
    var bpm_exp : Int = 0
    var quant: Boolean = false
    var listener : BeatListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.mode_bpm, container, false)
        bpm_tap = view.findViewById(R.id.bpm_tap)
        bpm_sync = view.findViewById(R.id.bpm_sync)
        bpm_halve = view.findViewById(R.id.bpm_halve)
        bpm_double = view.findViewById(R.id.bpm_double)
        quantize = view.findViewById(R.id.quantize)
        bpm_show = view.findViewById(R.id.bpm_display)

        bpm_tap?.setOnClickListener{_ ->
            taps.addLast(SystemClock.elapsedRealtime())
            recalc()
        }
        bpm_sync?.setOnClickListener{_ ->
            listener?.BPMSync()
        }
        bpm_halve?.setOnClickListener {_ ->
            if (bpm_exp > -5) {
                bpm_exp--
                recalc()
            }
        }
        bpm_double?.setOnClickListener{_ ->
            if (bpm_exp < 5) {
                bpm_exp++
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
                listener?.BPMSync()
                0.0
            }
            else -> {
                if (taps.size() >= 5) taps.popFirst()
                // time was in ms; 0.001 to get to seconds
                val duration = 0.001 * (taps.last - taps.first)
                val numBeats = taps.size() - 1.0
                val multiplier = Math.pow(2.0, bpm_exp.toDouble())
                val bpm = 60.0 * multiplier * numBeats / duration
                if (quant) Math.round(bpm).toDouble()
                else bpm
            }
        }
        listener?.BPMChange(bpm)
        if (bpm > 0) bpm_show?.text = String.format("BPM: %.1f", bpm)
        else bpm_show?.text = "BPM: -"
    }

    fun setBeatListener(bl : BeatListener) {
        listener = bl
    }
}
