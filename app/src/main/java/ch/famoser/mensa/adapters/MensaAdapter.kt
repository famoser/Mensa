package ch.famoser.mensa.adapters

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import ch.famoser.mensa.R
import ch.famoser.mensa.activities.MainActivity
import ch.famoser.mensa.models.Mensa
import kotlinx.android.synthetic.main.row_mensa.view.*
import java.util.*


class MensaAdapter constructor(
    private val parentActivity: MainActivity,
    values: List<Mensa>,
    private val twoPane: Boolean,
    private val initializeFully: Boolean
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
            return sharedPreferences.getBoolean(ShowOnlyFavoriteMensasSetting, false)
        }

        fun saveOnlyFavoriteMensasSetting(context: Activity, value: Boolean) {
            val sharedPreferences = context.getPreferences(Context.MODE_PRIVATE) ?: return
            sharedPreferences
                .edit()
                .putBoolean(ShowOnlyFavoriteMensasSetting, value)
                .apply()
        }
    }

    private val displayedMensas: List<MensaViewModel>

    init {
        displayedMensas = if (showOnlyFavoriteMensas()) {
            values.filter { isFavoriteMensa(it) }
        } else {
            values
        }.map { MensaViewModel(it) }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_mensa, parent, false)
        return ViewHolder(view)
    }

    private val statefulViewHoldersByMensaId: MutableMap<UUID, StatefulViewHolder> = HashMap()

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val mensaViewModel = displayedMensas[position]
        val holder = StatefulViewHolder(viewHolder, mensaViewModel)
        statefulViewHoldersByMensaId[mensaViewModel.mensa.id] = holder

        viewHolder.titleView.text = mensaViewModel.mensa.title

        viewHolder.itemView.setOnClickListener {
            if (mensaViewModel.viewState == ViewState.Available) {
                saveIsFavoriteMensa(mensaViewModel.mensa, true)
                transitionToDesiredHolderState(holder, ViewState.Expanded)
            } else if (mensaViewModel.viewState == ViewState.Expanded) {
                saveIsFavoriteMensa(mensaViewModel.mensa, false)
                transitionToDesiredHolderState(holder, ViewState.Available)
            }
        }

        if (mensaViewModel.viewState == ViewState.Initial) {
            if (initializeFully) {
                val recommendedState = getRecommendedState(holder.mensaViewModel.mensa)
                transitionToDesiredHolderState(holder, recommendedState)
            } else {
                showInitialHeader(holder)
            }
        } else {
            transitionToDesiredHolderState(holder, holder.mensaViewModel.viewState)
        }
    }

    private fun transitionToDesiredHolderState(holder: StatefulViewHolder, target: ViewState) {
        when (target) {
            ViewState.Closed -> {
                showClosedHeader(holder)
                hideMenu(holder)
            }
            ViewState.Expanded -> {
                showAvailableHeader(holder)
                showMenuList(holder)
            }
            ViewState.Available -> {
                showAvailableHeader(holder)
                hideMenu(holder)
            }
            ViewState.Initial -> throw Exception("you are not allowed to go to the initial state")
        }

        holder.mensaViewModel.viewState = target
    }

    private fun showMenuList(holder: StatefulViewHolder) {
        holder.viewHolder.getMenuView().visibility = View.VISIBLE
        val recyclerView = holder.viewHolder.getMenuRecyclerView()
        if (recyclerView.adapter == null) {
            recyclerView.adapter = MenuAdapter(parentActivity, holder.mensaViewModel.mensa.menus, twoPane)
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
        setViewBackground(holder.viewHolder.headerWrapper, R.attr.colorSurfaceVariant)
        setTextViewColor(holder.viewHolder.openingTimesView, R.attr.colorOnSurfaceVariant)
        setTextViewColor(holder.viewHolder.titleView, R.attr.colorOnSurfaceVariant)
    }

    private fun showInitialHeader(holder: StatefulViewHolder) {
        holder.viewHolder.openingTimesView.text = holder.mensaViewModel.mensa.mealTime
        setViewBackground(holder.viewHolder.headerWrapper, R.attr.colorSurfaceVariant)
        setTextViewColor(holder.viewHolder.openingTimesView, R.attr.colorOnSurfaceVariant)
        setTextViewColor(holder.viewHolder.titleView, R.attr.colorOnSurfaceVariant)
    }

    private fun showAvailableHeader(holder: StatefulViewHolder) {
        holder.viewHolder.openingTimesView.text = holder.mensaViewModel.mensa.mealTime

        when (parentActivity.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> {
                setViewBackground(holder.viewHolder.headerWrapper, R.attr.colorPrimaryContainer)
                setTextViewColor(holder.viewHolder.titleView, R.attr.colorOnPrimaryContainer)
                setTextViewColor(holder.viewHolder.openingTimesView, R.attr.colorOnPrimaryContainer)
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                setViewBackground(holder.viewHolder.headerWrapper, R.attr.colorPrimary)
                setTextViewColor(holder.viewHolder.titleView, R.attr.colorOnPrimary)
                setTextViewColor(holder.viewHolder.openingTimesView, R.attr.colorOnPrimary)
            }
        }
    }

    private fun setTextViewColor(view: TextView, attrId: Int) {
        val typedValue = TypedValue()
        parentActivity.theme.resolveAttribute(attrId, typedValue, true)
        view.setTextColor(typedValue.data)
    }

    private fun setViewBackground(view: View, attrId: Int) {
        val typedValue = TypedValue()
        parentActivity.theme.resolveAttribute(attrId, typedValue, true)
        view.setBackgroundColor(typedValue.data)
    }

    private fun hideMenu(holder: StatefulViewHolder) {
        holder.viewHolder.getMenuView().visibility = View.GONE
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

    override fun getItemCount() = displayedMensas.size

    fun mensaMenusRefreshed(mensaId: UUID) {
        val holder = statefulViewHoldersByMensaId[mensaId] ?: return

        val currentState = holder.mensaViewModel.viewState
        val recommendedState = getRecommendedState(holder.mensaViewModel.mensa)
        if (recommendedState == ViewState.Closed) {
            if (currentState != ViewState.Closed) {
                transitionToDesiredHolderState(holder, ViewState.Closed)
            }
        } else {
            if (currentState == ViewState.Expanded) {
                holder.viewHolder.getMenuRecyclerView().adapter?.notifyDataSetChanged()
            } else if (currentState == ViewState.Closed || currentState == ViewState.Initial) {
                transitionToDesiredHolderState(holder, recommendedState)
            }
        }
    }

    private fun getRecommendedState(mensa: Mensa): ViewState {
        return if (mensa.menus.isNotEmpty()) {
            val isFavorite = isFavoriteMensa(mensa)
            if (isFavorite) {
                ViewState.Expanded
            } else {
                ViewState.Available
            }
        } else {
            ViewState.Closed
        }
    }

    inner class MensaViewModel(val mensa: Mensa, var viewState: ViewState = ViewState.Initial)

    inner class StatefulViewHolder(val viewHolder: ViewHolder, val mensaViewModel: MensaViewModel)

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

    enum class ViewState {
        Initial,
        Closed,
        Available,
        Expanded
    }
}