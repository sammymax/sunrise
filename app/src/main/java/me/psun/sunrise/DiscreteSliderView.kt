package me.psun.sunrise

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView

class DiscreteSliderView : LinearLayout {
    constructor (context: Context) : super(context)
    constructor (context: Context, attrs: AttributeSet?) : super(context, attrs) { getText(attrs, 0) }
    constructor (context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle) { getText(attrs, defStyle) }

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

    private fun getText(attrs: AttributeSet?, defStyle: Int) {
        context.theme.obtainStyledAttributes(attrs, R.styleable.DiscreteSliderView, defStyle, 0).let {
            label?.text = it.getString(R.styleable.DiscreteSliderView_labelText)
        }
    }
}