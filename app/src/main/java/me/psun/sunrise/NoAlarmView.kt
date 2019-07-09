package me.psun.sunrise

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout

class NoAlarmView : FrameLayout {
    constructor (context: Context) : super(context)
    constructor (context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor (context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)
    constructor (context: Context, attrs: AttributeSet?, defStyle: Int, defStyleRes: Int) : super(context, attrs, defStyle, defStyleRes)

    init {
        inflate(context, R.layout.no_alarm_view, this)
    }
}