package ch.famoser.mensa.adapters

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import ch.famoser.mensa.R
import ch.famoser.mensa.models.Menu
import com.google.android.material.button.MaterialButton
import kotlinx.android.synthetic.main.row_menu_details.view.*


class MenuDetailsAdapter constructor(
    private val values: List<Menu>,
    private val activity: Activity
) :
    RecyclerView.Adapter<MenuDetailsAdapter.ViewHolder>() {

    private val onClickListener: View.OnClickListener
    private val onButtonClickListener: View.OnClickListener

    init {
        onClickListener = View.OnClickListener { view ->
            val item = view.tag as Menu
            val clip = ClipData.newPlainText("meals content", item.title + ": " + item.description)

            val clipboardManager = activity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboardManager.setPrimaryClip(clip)

            Toast.makeText(activity, "Copied to clipboard", Toast.LENGTH_SHORT).show()
        }
        onButtonClickListener = View.OnClickListener { view ->
            val item = view.tag as Menu
            val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, item.title + ": " + item.description)
                type = "text/plain"
            }

            val shareIntent = Intent.createChooser(sendIntent, null)
            activity.startActivity(shareIntent)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_menu_details, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]
        if (item.title.isEmpty()) {
            holder.titleView.visibility = View.GONE
        } else {
            holder.titleView.text = item.title
        }
        holder.descriptionView.text = item.description

        if (item.price.isNotEmpty()) {
            val priceString = item.price.joinToString(separator = " / ")
            holder.priceView.text = activity.getString(R.string.price, priceString)
        } else {
            holder.priceView.visibility = View.GONE
        }

        if (item.allergens != null && item.allergens.isNotEmpty()) {
            holder.allergensView.text = item.allergens
        } else {
            holder.allergensView.visibility = View.GONE
        }

        with(holder.itemView) {
            tag = item
            setOnClickListener(onClickListener)
        }
        with(holder.shareButton){
            tag = item
            setOnClickListener(onButtonClickListener)
        }
    }

    override fun getItemCount() = values.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleView: TextView = view.title
        val descriptionView: TextView = view.description
        val priceView: TextView = view.price
        val allergensView: TextView = view.allergens
        val shareButton: MaterialButton = view.share
    }
}