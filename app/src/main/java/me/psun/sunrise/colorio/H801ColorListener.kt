package me.psun.sunrise.colorio

import android.os.AsyncTask
import android.os.Handler
import me.psun.sunrise.AppState
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

class H801ColorListener(val getMAC: () -> String) : ColorListener {
    private var rgb = 0
    private var cw = 0
    private var ww = 0

    private var numScheduled = 0
    private val handler = Handler()

    override fun setRGB(rgb: Int, source: AppState.ColorSetSource) {
        this.rgb = rgb
        scheduleUpdate()
    }

    override fun setCW(cw: Int, source: AppState.ColorSetSource) {
        this.cw = cw
        scheduleUpdate()
    }

    override fun setWW(ww: Int, source: AppState.ColorSetSource) {
        this.ww = ww
        scheduleUpdate()
    }

    private fun scheduleUpdate() {
        var delay = 250L
        while (numScheduled < 5) {
            handler.postDelayed({
                val mac = getMAC()
                val buf = ByteArray(11)
                buf[0] = hexStringToByte("FB")
                buf[1] = hexStringToByte("EB")
                buf[2] = (rgb shr 16).toByte()
                buf[3] = ((rgb shr 8) and 255).toByte()
                buf[4] = (rgb and 255).toByte()
                buf[5] = ww.toByte()
                buf[6] = cw.toByte()
                // 7, 8, 9 are from MAC: 6th, 5th, 4th octets respectively
                buf[7] = hexStringToByte(mac.substring(10, 12))
                buf[8] = hexStringToByte(mac.substring(8, 10))
                buf[9] = hexStringToByte(mac.substring(6, 8))
                buf[10] = 0

                UpdateTask().execute(buf)
                numScheduled--
            }, delay)
            delay -= 50
            numScheduled++
        }
    }

    private class UpdateTask : AsyncTask<ByteArray, Unit, Unit>() {
        override fun doInBackground(vararg p0: ByteArray) {
            val h801Addr = InetAddress.getByName("192.168.4.1")
            val packet = DatagramPacket(p0[0], p0[0].size, h801Addr, 30977)
            DatagramSocket().send(packet)
        }
    }

    private fun hexStringToByte(hex: String): Byte {
        val HEX_CHARS = "0123456789ABCDEF"
        val i1 = HEX_CHARS.indexOf(hex[0])
        val i2 = HEX_CHARS.indexOf(hex[1])
        return i1.shl(4).or(i2).toByte()
    }
}