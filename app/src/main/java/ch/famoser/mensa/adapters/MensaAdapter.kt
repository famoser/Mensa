package ch.famoser.mensa.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import ch.famoser.mensa.R
import ch.famoser.mensa.activities.MainActivity
import ch.famoser.mensa.models.Mensa
import kotlinx.android.synthetic.main.row_mensa.view.*
import java.util.*
import kotlin.collections.HashMap


class MensaAdapter constructor(
    private val parentActivity: MainActivity,
    private val values: List<Mensa>,
    private val twoPane: Boolean
) : RecyclerView.Adapter<MensaAdapter.ViewHolder>() {
    companion object {
        const val MensaMenusVisibilitySettingPrefix = "MensaMenusVisibility"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_mensa, parent, false)
        return ViewHolder(view)
    }

    private val viewHoldersByMensaId: MutableMap<UUID, ViewHolder> = HashMap()

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val mensa = values[position]
        viewHoldersByMensaId[mensa.id] = holder

        holder.titleView.text = mensa.title
        setOpeningTimes(mensa, holder)
        holder.menuView.adapter = MenuAdapter(parentActivity, mensa.menus, twoPane)
        holder.menuView.addItemDecoration(
            DividerItemDecoration(
                parentActivity,
                DividerItemDecoration.VERTICAL
            )
        )

        holder.itemView.setOnClickListener {
            if (mensa.menus.isNotEmpty()) {
                toggleMenuVisibility(mensa, holder)
            }
        }

        val sharedPreferences = parentActivity.getPreferences(Context.MODE_PRIVATE) ?: return
        val visibility = sharedPreferences.getBoolean(MensaMenusVisibilitySettingPrefix + "." + mensa.id, false)
        if (visibility) {
            holder.menuView.visibility = View.VISIBLE
        }
    }

    private fun toggleMenuVisibility(mensa: Mensa, holder: ViewHolder) {
        if (holder.menuView.visibility == View.GONE) {
            holder.menuView.visibility = View.VISIBLE
            saveVisibilityState(mensa, true)
        } else {
            holder.menuView.visibility = View.GONE
            saveVisibilityState(mensa, false)
        }
    }

    private fun saveVisibilityState(mensa: Mensa, visibilityState: Boolean) {
        val sharedPreferences = parentActivity.getPreferences(Context.MODE_PRIVATE) ?: return
        sharedPreferences
            .edit()
            .putBoolean(MensaMenusVisibilitySettingPrefix + "." + mensa.id, visibilityState)
            .apply()
    }

    override fun getItemCount() = values.size

    fun mensaMenusRefreshed(mensaId: UUID) {
        val viewHolder = viewHoldersByMensaId[mensaId]
        if (viewHolder != null) {
            viewHolder.menuView.adapter?.notifyDataSetChanged()

            val menu = values.first { it.id == mensaId }
            setOpeningTimes(menu, viewHolder)
        }
    }

    private fun setOpeningTimes(mensa: Mensa, viewHolder: ViewHolder) {
        if (mensa.menus.isNotEmpty()) {
            viewHolder.openingTimesView.text = mensa.mealTime
            viewHolder.headerWrapper.background =
                ContextCompat.getDrawable(parentActivity.applicationContext, R.color.colorPrimary)
        } else {
            viewHolder.openingTimesView.text = parentActivity.getString(R.string.closed)
            viewHolder.headerWrapper.background =
                ContextCompat.getDrawable(parentActivity.applicationContext, R.color.colorPrimaryLight)
        }
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleView: TextView = view.title
        val openingTimesView: TextView = view.meal_time
        val menuView: RecyclerView = view.menu_list
        val headerWrapper: View = view.header_wrapper
    }
}