package me.psun.sunrise.colorio

import me.psun.sunrise.AppState

interface ColorListener {
    fun setRGB(rgb: Int, source: AppState.ColorSetSource)
    fun setCW(cw: Int, source: AppState.ColorSetSource)
    fun setWW(ww: Int, source: AppState.ColorSetSource)
}