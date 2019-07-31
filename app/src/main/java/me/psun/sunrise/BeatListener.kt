package me.psun.sunrise

interface BeatListener {
    fun bpmChange(bpm: Double)
    fun bpmSync()
}