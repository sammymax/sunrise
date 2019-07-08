package me.psun.sunrise

interface AlarmListener {
    fun onChange(hour: Int, minute: Int, soundId: Int?)
}