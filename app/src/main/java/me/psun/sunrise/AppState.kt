package me.psun.sunrise

import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import java.io.Serializable

class AppState : Serializable {
    var frag_idx : Int = 0
    var static_rgb : Int = Color.BLACK
    var static_cw : Int = 0
    var static_ww : Int = 0
    var settings_mac : String = ""

    constructor() {}

    constructor(prefs : SharedPreferences) {
        prefs.getString("settings.mac", "")?.let{ settings_mac = it}
    }

    fun getSettingsBundle() : Bundle {
        val b = Bundle()
        b.putString("settings.mac", settings_mac)
        return b
    }
}