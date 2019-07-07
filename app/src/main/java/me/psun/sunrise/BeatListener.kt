package me.psun.sunrise

interface BeatListener {
    fun BPMChange(bpm: Double)
    fun BPMSync()
}