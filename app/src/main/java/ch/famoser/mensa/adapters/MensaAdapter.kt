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

    private val statefulViewHoldersByMensaId: MutableMap<UUID, StatefulViewHolder> = HashMap()

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val mensa = displayedMensas[position]
        val holder = StatefulViewHolder(viewHolder, mensa)
        statefulViewHoldersByMensaId[mensa.id] = holder

        viewHolder.titleView.text = mensa.title

        viewHolder.itemView.setOnClickListener {
            if (holder.viewHolderState == ViewHolderState.Available) {
                saveIsFavoriteMensa(mensa, true)
                transitionToDesiredHolderState(holder, ViewHolderState.Expanded)
            } else if (holder.viewHolderState == ViewHolderState.Expanded) {
                saveIsFavoriteMensa(mensa, false)
                transitionToDesiredHolderState(holder, ViewHolderState.Available)
            }
        }

        if (mensa.menus.isNotEmpty()) {
            val isFavorite = isFavoriteMensa(holder.mensa)
            if (isFavorite) {
                transitionToDesiredHolderState(holder, ViewHolderState.Expanded)
            } else {
                transitionToDesiredHolderState(holder, ViewHolderState.Available)
            }
        } else {
            transitionToDesiredHolderState(holder, ViewHolderState.Closed)
        }
    }

    private fun transitionToDesiredHolderState(holder: StatefulViewHolder, target: ViewHolderState) {
        when (target) {
            ViewHolderState.Closed -> {
                showClosedHeader(holder)
                hideMenu(holder)
            }
            ViewHolderState.Expanded -> {
                showAvailableHeader(holder)
                showMenuList(holder)
            }
            ViewHolderState.Available -> {
                showAvailableHeader(holder)
                hideMenu(holder);
            }
            ViewHolderState.Initial -> throw Exception("you are not allowed to go to the initial state")
        }

        holder.viewHolderState = target
    }

    private fun showMenuList(holder: StatefulViewHolder) {
        holder.viewHolder.getMenuView().visibility = View.VISIBLE
        val recyclerView = holder.viewHolder.getMenuRecyclerView()
        if (recyclerView.adapter == null) {
            recyclerView.adapter = MenuAdapter(parentActivity, holder.mensa.menus, twoPane)
            recyclerView.addItemDecoration(
                DividerItemDecoration(
                    parentActivity,
                    DividerItemDecoration.VERTICAL
                )
            )
        }
    }

    private fun showClosedHeader(holder: StatefulViewHolder) {
        holder.viewHolder.openingTimesView.text = parentActivity.getString(R.string.closed)
        holder.viewHolder.headerWrapper.background =
            ContextCompat.getDrawable(parentActivity.applicationContext, R.color.colorPrimaryLight)
    }

    private fun showAvailableHeader(holder: StatefulViewHolder) {
        if (holder.viewHolderState == ViewHolderState.Initial || holder.viewHolderState == ViewHolderState.Closed) {
            holder.viewHolder.openingTimesView.text = holder.mensa.mealTime
            holder.viewHolder.headerWrapper.background =
                ContextCompat.getDrawable(parentActivity.applicationContext, R.color.colorPrimary)
        }
    }

    private fun hideMenu(holder: StatefulViewHolder) {
        if (holder.viewHolderState == ViewHolderState.Expanded) {
            holder.viewHolder.getMenuView().visibility = View.GONE
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
        val viewHolder = statefulViewHoldersByMensaId[mensaId] ?: return

        if (viewHolder.mensa.menus.isNotEmpty()) {
            if (viewHolder.viewHolderState == ViewHolderState.Expanded) {
                viewHolder.viewHolder.getMenuRecyclerView().adapter?.notifyDataSetChanged()
            } else if (viewHolder.viewHolderState == ViewHolderState.Closed) {
                val isFavorite = isFavoriteMensa(viewHolder.mensa)
                if (isFavorite) {
                    transitionToDesiredHolderState(viewHolder, ViewHolderState.Expanded)
                } else {
                    transitionToDesiredHolderState(viewHolder, ViewHolderState.Available)
                }
            }
        } else if (viewHolder.viewHolderState != ViewHolderState.Closed) {
            transitionToDesiredHolderState(viewHolder, ViewHolderState.Closed)
        }
    }

    inner class StatefulViewHolder(
        val viewHolder: ViewHolder,
        val mensa: Mensa,
        var viewHolderState: ViewHolderState = ViewHolderState.Initial
    )

    inner class ViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        val titleView: TextView = view.title
        val openingTimesView: TextView = view.meal_time

        private var menuView: View = view.findViewById(R.id.menu_list)
        fun getMenuView() = menuView

        private var resolvedMenuRecycler: RecyclerView? = null
        fun getMenuRecyclerView(): RecyclerView {
            if (resolvedMenuRecycler == null) {
                val menuRecycler = view.findViewById<RecyclerView>(R.id.menu_list)
                menuView = menuRecycler
                resolvedMenuRecycler = menuRecycler
            }

            return resolvedMenuRecycler!!
        }

        val headerWrapper: View = view.header_wrapper
    }

    enum class ViewHolderState {
        Initial,
        Closed,
        Available,
        Expanded
    }
}