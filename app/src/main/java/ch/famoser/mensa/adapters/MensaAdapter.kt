package ch.famoser.mensa.adapters

import android.app.Activity
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
    values: List<Mensa>,
    private val twoPane: Boolean
) : RecyclerView.Adapter<MensaAdapter.ViewHolder>() {
    companion object {
        private const val MensaIsFavoriteSettingPrefix = "MensaMenusVisibility"
        private const val ShowOnlyFavoriteMensasSetting = "ShowOnlyFavoriteMensas"

        fun saveIsFavoriteMensa(context: Activity, mensa: Mensa, value: Boolean) {
            val sharedPreferences = context.getPreferences(Context.MODE_PRIVATE) ?: return
            sharedPreferences
                .edit()
                .putBoolean(MensaIsFavoriteSettingPrefix + "." + mensa.id, value)
                .apply()
        }

        fun isFavoriteMensa(context: Activity, mensa: Mensa): Boolean {
            val sharedPreferences = context.getPreferences(Context.MODE_PRIVATE) ?: return false
            return sharedPreferences.getBoolean(MensaIsFavoriteSettingPrefix + "." + mensa.id, false)
        }

        fun showOnlyFavoriteMensas(context: Activity): Boolean {
            val sharedPreferences = context.getPreferences(Context.MODE_PRIVATE) ?: return false
            return sharedPreferences.getBoolean(ShowOnlyFavoriteMensasSetting, false);
        }

        fun saveOnlyFavoriteMensasSetting(context: Activity, value: Boolean) {
            val sharedPreferences = context.getPreferences(Context.MODE_PRIVATE) ?: return
            sharedPreferences
                .edit()
                .putBoolean(ShowOnlyFavoriteMensasSetting, value)
                .apply()
        }
    }

    private val displayedMensas: List<Mensa>;

    init {
        displayedMensas = if (showOnlyFavoriteMensas()) {
            values.filter { isFavoriteMensa(it) }
        } else {
            values
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_mensa, parent, false)
        return ViewHolder(view)
    }

    private val viewHoldersByMensaId: MutableMap<UUID, ViewHolder> = HashMap()

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val mensa = displayedMensas[position]
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

        val visibility = isFavoriteMensa(mensa)
        if (visibility) {
            holder.menuView.visibility = View.VISIBLE
        }
    }

    private fun toggleMenuVisibility(mensa: Mensa, holder: ViewHolder) {
        if (holder.menuView.visibility == View.GONE) {
            holder.menuView.visibility = View.VISIBLE
            saveIsFavoriteMensa(mensa, true)
        } else {
            holder.menuView.visibility = View.GONE
            saveIsFavoriteMensa(mensa, false)
        }
    }

    private fun saveIsFavoriteMensa(mensa: Mensa, value: Boolean) {
        saveIsFavoriteMensa(parentActivity, mensa, value)
    }

    private fun isFavoriteMensa(mensa: Mensa): Boolean {
        return Companion.isFavoriteMensa(parentActivity, mensa)
    }

    private fun showOnlyFavoriteMensas(): Boolean {
        return Companion.showOnlyFavoriteMensas(parentActivity)
    }

    override fun getItemCount() = displayedMensas.size;

    fun mensaMenusRefreshed(mensaId: UUID) {
        val viewHolder = viewHoldersByMensaId[mensaId]
        if (viewHolder != null) {
            viewHolder.menuView.adapter?.notifyDataSetChanged()

            val menu = displayedMensas.first { it.id == mensaId }
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