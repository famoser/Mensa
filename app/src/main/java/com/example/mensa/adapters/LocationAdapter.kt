package com.example.mensa.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mensa.R
import com.example.mensa.activities.MainActivity
import com.example.mensa.data.Location
import kotlinx.android.synthetic.main.row_location.view.*

class LocationAdapter constructor(
    private val parentActivity: MainActivity,
    private val values: List<Location>,
    private val twoPane: Boolean) :
    RecyclerView.Adapter<LocationAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_location, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]
        holder.titleView.text = item.title
        holder.mensaView.adapter = MensaAdapter(parentActivity, item.mensas, twoPane)

        with(holder.itemView) {
            tag = item
        }
    }

    override fun getItemCount() = values.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleView: TextView = view.title
        val mensaView: RecyclerView = view.mensa_list
    }
}