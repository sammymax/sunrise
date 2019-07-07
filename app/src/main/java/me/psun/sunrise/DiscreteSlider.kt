package me.psun.sunrise

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout

class DiscreteSlider : LinearLayout {
    constructor (context: Context) : super(context)
    constructor (context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor (context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)
    constructor (context: Context, attrs: AttributeSet?, defStyle: Int, defStyleRes: Int) : super(context, attrs, defStyle, defStyleRes)
    init {
        LayoutInflater.from(context).inflate(R.layout.my_discrete_slider, this, true)
        orientation = VERTICAL
    }
}