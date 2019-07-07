package me.psun.sunrise

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView

class DiscreteSlider : LinearLayout {
    constructor (context: Context) : super(context)
    constructor (context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor (context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)
    constructor (context: Context, attrs: AttributeSet?, defStyle: Int, defStyleRes: Int) : super(context, attrs, defStyle, defStyleRes)

    private val label : TextView
    private val level : TextView
    private val bar : SeekBar
    private var listener : DiscreteSliderListener? = null

    init {
        inflate(context, R.layout.my_discrete_slider, this)
        orientation = VERTICAL

        label = findViewById(R.id.discreteLabel)
        level = findViewById(R.id.discreteLevel)
        bar = findViewById(R.id.bar)
        bar.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener{
                override fun onStartTrackingTouch(bar: SeekBar?) {
                }

                override fun onStopTrackingTouch(bar: SeekBar?) {
                }

                override fun onProgressChanged(bar: SeekBar?, progress: Int, fromUser: Boolean) {
                    level.text = progress.toString()
                    listener?.onChange(progress, fromUser)
                }
            }
        )
    }

    fun setDiscreteSliderListener(d : DiscreteSliderListener) {
        listener = d
    }

    fun setDiscreteValue(v : Int) {
        level.text = v.toString()
        bar.progress = v
    }
}