package ch.famoser.mensa.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ch.famoser.mensa.R
import ch.famoser.mensa.activities.MainActivity
import ch.famoser.mensa.models.Location
import ch.famoser.mensa.models.Mensa
import kotlinx.android.synthetic.main.row_location.view.*

class LocationAdapter(
    private val parentActivity: MainActivity,
    private val twoPane: Boolean,
    private val values: List<Location>,
    initializeFully: Boolean
) :
    RecyclerView.Adapter<LocationAdapter.ViewHolder>() {

    private val mensaAdapterByLocation: MutableMap<Location, MensaAdapter> = HashMap()
    private val displayedLocations: MutableList<DisplayedLocation> = ArrayList()

    init {
        reset(initializeFully)
    }

    fun reset(initializeFully: Boolean = true) {
        mensaAdapterByLocation.clear()
        displayedLocations.clear()

        for (location in values) {
            val adapter = MensaAdapter(parentActivity, location.mensas, twoPane, initializeFully);
            if (adapter.itemCount > 0) {
                mensaAdapterByLocation[location] = adapter
                displayedLocations.add(DisplayedLocation(location, adapter))
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_location, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = displayedLocations[position]

        holder.titleView.text = item.location.title
        holder.mensaView.adapter = item.mensaAdapter

        with(holder.itemView) {
            tag = item
        }
    }

    override fun getItemCount() = displayedLocations.size

    fun mensaUpdated(mensa: Mensa) {
        val mensaAdapter = mensaAdapterByLocation[mensa.location]
        mensaAdapter?.mensaMenusRefreshed(mensa.id)
    }

    fun mensasUpdated(mensas: List<Mensa>) {
        val groupByLocation = mensas.groupBy { it.location }
        for (sameLocationMensas in groupByLocation) {
            val mensaAdapter = mensaAdapterByLocation[sameLocationMensas.key]
            for (sameLocationMensa in sameLocationMensas.value) {
                mensaAdapter?.mensaMenusRefreshed(sameLocationMensa.id)
            }
        }
    }

    inner class DisplayedLocation(val location: Location, val mensaAdapter: MensaAdapter)

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleView: TextView = view.title
        val mensaView: RecyclerView = view.mensa_list
    }
}