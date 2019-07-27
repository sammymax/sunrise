package me.psun.sunrise.colorio

import me.psun.sunrise.AppState
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

class H801ColorListener : ColorListener {
    private val socket = DatagramSocket()
    private var rgb = 0
    private var cw = 0
    private var ww = 0

    override fun setRGB(rgb: Int, source: AppState.ColorSetSource) {
        this.rgb = rgb
        update()
    }

    override fun setCW(cw: Int, source: AppState.ColorSetSource) {
        this.cw = cw
        update()
    }

    override fun setWW(ww: Int, source: AppState.ColorSetSource) {
        this.ww = ww
        update()
    }

    private fun update() {
        val h801Addr = InetAddress.getByName("192.168.4.1")
        val buf = ByteArray(11)
        buf[0] = 0xFB.toByte()
        buf[1] = 0xEB.toByte()
        buf[2] = (rgb shr 16).toByte()
        buf[3] = ((rgb shr 8) and 255).toByte()
        buf[4] = (rgb and 255).toByte()
        buf[5] = ww.toByte()
        buf[6] = cw.toByte()
        // 7, 8, 9 are from MAC
        buf[10] = 0
        val packet = DatagramPacket(buf, buf.size, h801Addr, 30977)
        socket.send(packet)
    }
}