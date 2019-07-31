package me.psun.sunrise.colorio

import me.psun.sunrise.RootService

class MultiColorListener(vararg listeners: ColorListener) : ColorListener {
    private val listenerList: MutableList<ColorListener> = listeners.toMutableList()
    fun addListener(listener: ColorListener) {
        listenerList.add(listener)
    }

    override fun setRGB(rgb: Int, source: RootService.ColorSetSource) {
        for (l in listenerList)
            l.setRGB(rgb, source)
    }

    override fun setCW(cw: Int, source: RootService.ColorSetSource) {
        for (l in listenerList) l.setCW(cw, source)
    }

    override fun setWW(ww: Int, source: RootService.ColorSetSource) {
        for (l in listenerList) l.setWW(ww, source)
    }
}