package me.psun.sunrise

import android.graphics.Color
import java.io.Serializable

class AppState : Serializable {
    var frag_idx : Int = 0
    var static_rgb : Int = Color.BLACK
    var static_cw : Int = 0
    var static_ww : Int = 0
}