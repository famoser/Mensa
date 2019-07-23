package com.example.mensa.adapters

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.app.ActivityOptionsCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.mensa.R
import com.example.mensa.activities.MainActivity
import com.example.mensa.activities.MensaActivity
import com.example.mensa.fragments.MensaDetailFragment
import com.example.mensa.models.Menu
import kotlinx.android.synthetic.main.row_menu.view.*


class MenuAdapter constructor(
    private val parentActivity: MainActivity,
    private val values: List<Menu>,
    private val twoPane: Boolean,
    private val titleView: View
) :
    RecyclerView.Adapter<MenuAdapter.ViewHolder>() {

    private val onClickListener: View.OnClickListener

    init {
        onClickListener = View.OnClickListener { view ->
            val item = view.tag as Menu
            if (twoPane) {
                val fragment = MensaDetailFragment().apply {
                    arguments = Bundle().apply {
                        putString(MensaDetailFragment.MENSA_ID, item.mensa!!.id.toString())
                    }
                }
                parentActivity.supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.details_container, fragment)
                    .commit()
            } else {
                val intent = Intent(view.context, MensaActivity::class.java).apply {
                    putExtra(MensaDetailFragment.MENSA_ID, item.mensa!!.id.toString())
                }

                val options = ActivityOptionsCompat.makeSceneTransitionAnimation(parentActivity, titleView, "mensa_title")
                view.context.startActivity(intent, options.toBundle())
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_menu, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]
        holder.titleView.text = item.title
        holder.descriptionView.text = item.description
        holder.priceView.text = item.price.firstOrNull()

        with(holder.itemView) {
            tag = item
            setOnClickListener(onClickListener)
        }
    }

    override fun getItemCount() = values.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleView: TextView = view.title
        val descriptionView: TextView = view.description
        val priceView: TextView = view.price
    }
}