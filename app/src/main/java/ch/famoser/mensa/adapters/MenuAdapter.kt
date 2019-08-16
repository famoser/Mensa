package ch.famoser.mensa.adapters

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ch.famoser.mensa.R
import ch.famoser.mensa.activities.MainActivity
import ch.famoser.mensa.activities.MensaActivity
import ch.famoser.mensa.fragments.MensaDetailFragment
import ch.famoser.mensa.models.Menu
import kotlinx.android.synthetic.main.row_menu.view.*


class MenuAdapter constructor(
    private val parentActivity: MainActivity,
    private val values: List<Menu>,
    private val twoPane: Boolean
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

                view.context.startActivity(intent)
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
        if (item.price.isNotEmpty()) {
            holder.priceView.text = parentActivity.getString(R.string.price, item.price.first())
        }

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