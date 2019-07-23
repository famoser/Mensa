package ch.famoser.mensa.adapters

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import ch.famoser.mensa.models.Menu
import kotlinx.android.synthetic.main.row_menu_details.view.*


class MenuDetailsAdapter constructor(
    private val values: List<Menu>,
    private val activity: Activity
) :
    RecyclerView.Adapter<MenuDetailsAdapter.ViewHolder>() {

    private val onClickListener: View.OnClickListener

    init {
        onClickListener = View.OnClickListener { view ->
            val item = view.tag as Menu
            val clip = ClipData.newPlainText("menu content", item.title + ": " + item.description)

            val clipboardManager = activity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboardManager.setPrimaryClip(clip)

            Toast.makeText(activity, "Copied to clipboard", Toast.LENGTH_SHORT).show();
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(ch.famoser.mensa.R.layout.row_menu_details, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]
        holder.titleView.text = item.title
        holder.descriptionView.text = item.description
        holder.priceView.text = item.price.joinToString(separator = " / ")
        if (item.allergens != null && item.allergens.isNotEmpty()) {
            holder.allergensView.text = item.allergens
        } else {
            holder.allergensView.visibility = View.GONE
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
        val allergensView: TextView = view.allergens
    }
}