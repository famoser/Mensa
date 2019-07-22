package com.example.mensa.adapters

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mensa.activities.MensaActivity
import com.example.mensa.fragments.MensaDetailFragment
import com.example.mensa.activities.MainActivity
import com.example.mensa.R
import com.example.mensa.models.Mensa
import kotlinx.android.synthetic.main.row_mensa.view.*

class MensaAdapter constructor(
    private val parentActivity: MainActivity,
    private val values: List<Mensa>,
    private val twoPane: Boolean
) : RecyclerView.Adapter<MensaAdapter.ViewHolder>() {

    private val onClickListener: View.OnClickListener

    init {
        onClickListener = View.OnClickListener { v ->
            val item = v.tag as Mensa
            if (twoPane) {
                val fragment = MensaDetailFragment().apply {
                    arguments = Bundle().apply {
                        putString(MensaDetailFragment.MENSA_ID, item.id.toString())
                    }
                }
                parentActivity.supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.details_container, fragment)
                    .commit()
            } else {
                val intent = Intent(v.context, MensaActivity::class.java).apply {
                    putExtra(MensaDetailFragment.MENSA_ID, item.id.toString())
                }
                v.context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_mensa, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]
        holder.titleView.text = item.title
        holder.openingTimesView.text = item.mealTime
        holder.menuView.adapter = MenuAdapter(item.menus);

        with(holder.itemView) {
            tag = item
            setOnClickListener(onClickListener)
        }
    }

    override fun getItemCount() = values.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleView: TextView = view.title
        val openingTimesView: TextView = view.meal_time
        val menuView: RecyclerView = view.menu_list
    }
}