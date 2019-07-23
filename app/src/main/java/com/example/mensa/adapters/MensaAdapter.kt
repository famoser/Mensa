package com.example.mensa.adapters

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.example.mensa.R
import com.example.mensa.activities.MainActivity
import com.example.mensa.activities.MensaActivity
import com.example.mensa.fragments.MensaDetailFragment
import com.example.mensa.models.Mensa
import kotlinx.android.synthetic.main.row_mensa.view.*
import java.util.*
import kotlin.collections.HashMap


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

    private val menuAdapters: MutableMap<UUID, MenuAdapter> = HashMap()

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]

        val adapter = MenuAdapter(parentActivity, item.menus, twoPane);
        menuAdapters.put(item.id, adapter);

        holder.titleView.text = item.title
        holder.openingTimesView.text = item.mealTime
        holder.menuView.adapter = adapter;
        holder.menuView.addItemDecoration(
            DividerItemDecoration(
                parentActivity,
                DividerItemDecoration.VERTICAL
            )
        )

        holder.itemView.setOnClickListener {
            if (item.menus.isNotEmpty()) {
                if (holder.menuView.visibility == View.GONE) {
                    holder.menuView.visibility = View.VISIBLE
                } else {
                    holder.menuView.visibility = View.GONE
                }
            }
        }
    }

    override fun getItemCount() = values.size

    fun mensaMenusRefreshed(mensaId: UUID) {
        val menuAdapter = menuAdapters[mensaId];
        if (menuAdapter != null) {
            menuAdapter.notifyDataSetChanged()
        }
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleView: TextView = view.title
        val openingTimesView: TextView = view.meal_time
        val menuView: RecyclerView = view.menu_list
        val parentView: ViewGroup = view.mensa_wrapper
    }
}