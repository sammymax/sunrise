package me.psun.sunrise.colorio

import android.util.Log
import me.psun.sunrise.AppState

class LoggingColorListener : ColorListener {
    override fun setRGB(rgb: Int, source: AppState.ColorSetSource) {
        Log.e("LoggingColorListener", "RGB set to ${Integer.toHexString(rgb)} from ${source.name}")
    }

    override fun setCW(cw: Int, source: AppState.ColorSetSource) {
        Log.e("LoggingColorListener", "CW set to ${Integer.toHexString(cw)} from ${source.name}")
    }

    override fun setWW(ww: Int, source: AppState.ColorSetSource) {
        Log.e("LoggingColorListener", "WW set to ${Integer.toHexString(ww)} from ${source.name}")
    }
}