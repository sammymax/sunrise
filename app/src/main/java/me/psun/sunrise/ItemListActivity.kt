package me.psun.sunrise

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment

import kotlinx.android.synthetic.main.activity_item_list.*
import kotlinx.android.synthetic.main.item_list_content.view.*
import kotlinx.android.synthetic.main.item_list.*

class ItemListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item_list)

        setSupportActionBar(toolbar)
        toolbar.title = title

        setupRecyclerView(item_list)
        //setupAlarmSpinner()
    }

    private fun setupAlarmSpinner() {
        val alarmSpinner : Spinner = findViewById(R.id.alarm_spinner)
        val dataAdapter = ArrayAdapter<String>(this,
            android.R.layout.simple_spinner_dropdown_item,
            listOf("Hello1", "hello2", "hello3"))
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        alarmSpinner.adapter = dataAdapter
    }

    private fun setupRecyclerView(recyclerView: RecyclerView) {
        recyclerView.adapter = SimpleItemRecyclerViewAdapter(this)
    }

    class SimpleItemRecyclerViewAdapter(
        private val parentActivity: ItemListActivity
    ) :
        RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder>() {

        private val onClickListener: View.OnClickListener
        private val recyclerListTexts = listOf<String>("Set Color", "BPM Sync", "Sunrise Alarm", "Settings")

        init {
            onClickListener = View.OnClickListener { v ->
                val idx = v.tag as Int
                when (idx) {
                    0 -> StaticFragment()
                    1 -> BPMFragment()
                    2 -> SunriseFragment()
                    3 -> SettingsFragment()
                    else -> null
                }?.let {
                    parentActivity.supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.item_detail_container, it)
                        .commit()
                }
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
