package me.psun.sunrise.colorio

import me.psun.sunrise.RootService

interface ColorListener {
    fun setRGB(rgb: Int, source: RootService.ColorSetSource)
    fun setCW(cw: Int, source: RootService.ColorSetSource)
    fun setWW(ww: Int, source: RootService.ColorSetSource)
}