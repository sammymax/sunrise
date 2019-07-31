package me.psun.sunrise

import android.content.ComponentName
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
import android.content.ServiceConnection
import android.os.IBinder
import android.widget.ImageView
import me.psun.sunrise.colorio.ColorListener

class RootActivity : AppCompatActivity() {
    var sunriseFragment: SunriseFragment? = null
    var stateService: RootService? = null
    var stateServiceBound = false
    private val stateServiceConnection = object: ServiceConnection{
        override fun onServiceDisconnected(className: ComponentName) {
            stateServiceBound = false
        }

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            stateServiceBound = true
            stateService = (service as RootService.LocalBinder).getService()
            intent?.let {
                processIntent(intent)
            }

            val bpmFragment = BPMFragment()
            bpmFragment.setBeatListener(object : BeatListener{
                override fun bpmChange(bpm: Double) {
                    stateService?.bpmChange(bpm)
                }

                override fun bpmSync() {
                    stateService?.bpmSync()
                }
            })
            val staticFragment = StaticFragment(object : ColorListener{
                override fun setRGB(rgb: Int, source: RootService.ColorSetSource) { stateService?.staticSetRGB(rgb) }
                override fun setCW(cw: Int, source: RootService.ColorSetSource) { stateService?.staticSetCW(cw) }
                override fun setWW(ww: Int, source: RootService.ColorSetSource) { stateService?.staticSetWW(ww) }
            })
            sunriseFragment = SunriseFragment(stateService!!)
            val settingsFragment = SettingsFragment(stateService?.settings_mac.orEmpty())
            settingsFragment.setMacAddressListener{mac ->
                stateService?.settings_mac = mac
            }
            stateService?.addColorListener(object: ColorListener{
                override fun setRGB(rgb: Int, source: RootService.ColorSetSource) {
                    if (source == RootService.ColorSetSource.BPM)
                        bpmFragment.setPreviewRGB(rgb)
                }
                override fun setCW(cw: Int, source: RootService.ColorSetSource) {}
                override fun setWW(ww: Int, source: RootService.ColorSetSource) {}
            })
            supportFragmentManager.beginTransaction()
                .add(R.id.item_detail_container, settingsFragment, "frag3").hide(settingsFragment)
                .add(R.id.item_detail_container, sunriseFragment!!, "frag2").hide(sunriseFragment!!)
                .add(R.id.item_detail_container, bpmFragment, "frag1").hide(bpmFragment)
                .add(R.id.item_detail_container, staticFragment, "frag0")
                .commit()
            setupRecyclerView(item_list)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item_list)

        setSupportActionBar(toolbar)
        toolbar.title = title

        val serviceIntent = Intent(this, RootService::class.java)
        startService(serviceIntent)
        bindService(serviceIntent, stateServiceConnection, Context.BIND_AUTO_CREATE)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            val myIntent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
            startActivity(myIntent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (stateServiceBound)
            unbindService(stateServiceConnection)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let {
            processIntent(it)
        }
    }

    private fun processIntent(intent: Intent) {
        val isAlarmOff = intent.extras?.getBoolean(RootService.ALARM_OFF_ACTION_OFF, false)
        if (isAlarmOff != true) return

        val shouldSnooze = intent.extras?.getBoolean(RootService.ALARM_OFF_ACTION_SNOOZE, false)
        stateService?.delSunrise()
        if (shouldSnooze == true) {
            stateService?.snoozeAlarm()
            sunriseFragment?.updateAlarmTime()
        }
        sunriseFragment?.updateViewToState()
    }

    private fun setupRecyclerView(recyclerView: RecyclerView) {
        recyclerView.adapter = SimpleItemRecyclerViewAdapter(this)
    }

    fun changeFragIdx(newIdx: Int) {
        val oldId = "frag${stateService!!.fragIdx}"
        val newId = "frag$newIdx"
        supportFragmentManager.beginTransaction()
            .hide(supportFragmentManager.findFragmentByTag(oldId) as Fragment)
            .show(supportFragmentManager.findFragmentByTag(newId) as Fragment)
            .commit()
        stateService!!.fragIdx = newIdx
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
