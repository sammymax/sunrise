package me.psun.sunrise

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment

import kotlinx.android.synthetic.main.activity_item_list.*
import kotlinx.android.synthetic.main.item_list_content.view.*
import kotlinx.android.synthetic.main.item_list.*
import android.content.Intent
import android.widget.ImageView
import me.psun.sunrise.colorio.ColorListener
import me.psun.sunrise.colorio.H801ColorListener


class RootActivity : AppCompatActivity() {
    var appState : AppState? = null
    var sunriseFragment: SunriseFragment? = null
    val colorListener = H801ColorListener{appState?.settings_mac!!}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item_list)

        setSupportActionBar(toolbar)
        toolbar.title = title

        val bpmFragment = BPMFragment()
        bpmFragment.setBeatListener(object : BeatListener{
            override fun BPMChange(bpm: Double) {
                appState?.bpmChange(bpm)
            }

            override fun BPMSync() {
                appState?.bpmSync()
            }
        })
        appState = AppState(this, object : ColorListener{
            override fun setRGB(rgb: Int, source: AppState.ColorSetSource) {
                if (source == AppState.ColorSetSource.BPM)
                    bpmFragment.setPreviewRGB(rgb)
                colorListener.setRGB(rgb, source)
            }

            override fun setCW(cw: Int, source: AppState.ColorSetSource) {
                colorListener.setCW(cw, source)
            }

            override fun setWW(ww: Int, source: AppState.ColorSetSource) {
                colorListener.setWW(ww, source)
            }

        }, getPreferences(Context.MODE_PRIVATE))
        val staticFragment = StaticFragment(appState!!)
        sunriseFragment = SunriseFragment(appState!!)
        val frag3 = SettingsFragment()
        frag3.arguments = appState!!.getSettingsBundle()
        frag3.setMacAddressListener(object : MacAddressListener {
            override fun onMacAddressChange(mac: String) {
                appState!!.settings_mac = mac
            }
        })
        supportFragmentManager.beginTransaction()
            .add(R.id.item_detail_container, frag3, "frag3").hide(frag3)
            .add(R.id.item_detail_container, sunriseFragment!!, "frag2").hide(sunriseFragment!!)
            .add(R.id.item_detail_container, bpmFragment, "frag1").hide(bpmFragment)
            .add(R.id.item_detail_container, staticFragment, "frag0")
            .commit()
        setupRecyclerView(item_list)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            val myIntent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
            startActivity(myIntent)
        }
        intent?.let {
            processIntent(intent)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let {
            processIntent(it)
        }
    }

    private fun processIntent(intent: Intent) {
        val isSunriseUpdate = intent.extras?.getBoolean(AppState.SUNRISE_UPDATE, false)
        if (isSunriseUpdate == true) {
            appState?.updateSunriseBrightness()
        }

        val isAlarmOff = intent.extras?.getBoolean(AppState.ALARM_OFF_ACTION_OFF, false)
        if (isAlarmOff != true) return

        val shouldSnooze = intent.extras?.getBoolean(AppState.ALARM_OFF_ACTION_SNOOZE, false)
        appState?.delSunrise()
        if (shouldSnooze == true) {
            appState?.snoozeAlarm()
            sunriseFragment?.updateAlarmTime()
        }
        sunriseFragment?.updateViewToState()
    }

    private fun setupRecyclerView(recyclerView: RecyclerView) {
        recyclerView.adapter = SimpleItemRecyclerViewAdapter(this)
    }

    fun changeFragIdx(newIdx: Int) {
        val oldId = "frag${appState!!.fragIdx}"
        val newId = "frag$newIdx"
        supportFragmentManager.beginTransaction()
            .hide(supportFragmentManager.findFragmentByTag(oldId) as Fragment)
            .show(supportFragmentManager.findFragmentByTag(newId) as Fragment)
            .commit()
        appState!!.fragIdx = newIdx
    }

    class SimpleItemRecyclerViewAdapter(
        private val parentActivity: RootActivity
    ) :
        RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder>() {

        private val onClickListener: View.OnClickListener
        private val recyclerListIcons = listOf("ic_color_lens_white_24dp", "ic_music_note_white_24dp", "ic_wb_sunny_white_24dp", "ic_settings_white_24dp")
        private val recyclerListTexts = listOf("Set Color", "BPM Sync", "Sunrise Alarm", "Settings")

        init {
            onClickListener = View.OnClickListener { v ->
                parentActivity.changeFragIdx(v.tag as Int)
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_list_content, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val resourceName = recyclerListIcons[position]
            holder.menuItemIcon.setImageResource(
                parentActivity.resources.getIdentifier(resourceName, "drawable", parentActivity.packageName)
            )
            holder.contentView.text = recyclerListTexts[position]

            with(holder.itemView) {
                tag = position
                setOnClickListener(onClickListener)
            }
        }

        override fun getItemCount() = recyclerListTexts.size

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val menuItemIcon: ImageView = view.menu_item_icon
            val contentView: TextView = view.content
        }
    }
}
