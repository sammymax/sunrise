package me.psun.sunrise

import android.content.Context
import android.os.Bundle
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

class ItemListActivity : AppCompatActivity() {
    var appState : AppState = AppState()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item_list)

        setSupportActionBar(toolbar)
        toolbar.title = title

        appState = AppState(getPreferences(Context.MODE_PRIVATE))
        val frag0 = StaticFragment()
        val frag1 = BPMFragment()
        val frag2 = SunriseFragment()
        val frag3 = SettingsFragment()
        frag3.arguments = appState.getSettingsBundle()
        frag3.setMacAddressListener(object : MacAddressListener {
            override fun onMacAddressChange(mac: String) {
                appState.settings_mac = mac
                with (getPreferences(Context.MODE_PRIVATE).edit()) {
                    putString("settings.mac", mac)
                    commit()
                }
            }
        })
        supportFragmentManager.beginTransaction()
            .add(R.id.item_detail_container, frag3, "frag3").hide(frag3)
            .add(R.id.item_detail_container, frag2, "frag2").hide(frag2)
            .add(R.id.item_detail_container, frag1, "frag1").hide(frag1)
            .add(R.id.item_detail_container, frag0, "frag0")
            .commit()
        setupRecyclerView(item_list)
    }

    private fun setupRecyclerView(recyclerView: RecyclerView) {
        recyclerView.adapter = SimpleItemRecyclerViewAdapter(this)
    }

    fun changeFragIdx(newIdx: Int) {
        val oldId = "frag${appState.frag_idx}"
        val newId = "frag$newIdx"
        supportFragmentManager.beginTransaction()
            .hide(supportFragmentManager.findFragmentByTag(oldId) as Fragment)
            .show(supportFragmentManager.findFragmentByTag(newId) as Fragment)
            .commit()
        appState.frag_idx = newIdx
    }

    class SimpleItemRecyclerViewAdapter(
        private val parentActivity: ItemListActivity
    ) :
        RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder>() {

        private val onClickListener: View.OnClickListener
        private val recyclerListTexts = listOf<String>("Set Color", "BPM Sync", "Sunrise Alarm", "Settings")

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
            holder.idView.text = position.toString()
            holder.contentView.text = recyclerListTexts[position]

            with(holder.itemView) {
                tag = position
                setOnClickListener(onClickListener)
            }
        }

        override fun getItemCount() = recyclerListTexts.size

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val idView: TextView = view.id_text
            val contentView: TextView = view.content
        }
    }
}
