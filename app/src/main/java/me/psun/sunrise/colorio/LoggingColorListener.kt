package me.psun.sunrise.colorio

import android.util.Log
import me.psun.sunrise.RootService

class LoggingColorListener : ColorListener {
    override fun setRGB(rgb: Int, source: RootService.ColorSetSource) {
        Log.e("LoggingColorListener", "RGB set to ${Integer.toHexString(rgb)} from ${source.name}")
    }

    override fun setCW(cw: Int, source: RootService.ColorSetSource) {
        Log.e("LoggingColorListener", "CW set to ${Integer.toHexString(cw)} from ${source.name}")
    }

    override fun setWW(ww: Int, source: RootService.ColorSetSource) {
        Log.e("LoggingColorListener", "WW set to ${Integer.toHexString(ww)} from ${source.name}")
    }
}