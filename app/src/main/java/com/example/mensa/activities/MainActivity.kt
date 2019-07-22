package com.example.mensa.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.mensa.adapters.LocationAdapter
import com.example.mensa.events.MensaMenuUpdatedEvent
import com.example.mensa.events.RefreshMensaFinishedEvent
import com.example.mensa.repositories.LocationRepository
import kotlinx.android.synthetic.main.content_main.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.time.LocalDate


/**
 * An activity representing a list of Pings. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a [MensaActivity] representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
class MainActivity : AppCompatActivity() {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private var twoPane: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.example.mensa.R.layout.activity_main)

        if (details_container != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            twoPane = true
        }


        val locationRepository = LocationRepository.getInstance(assets);
        val locations = locationRepository.getLocations();

        val locationAdapter = LocationAdapter(this, locations, twoPane)
        location_list.adapter = locationAdapter
        this.locationAdapter = locationAdapter

        EventBus.getDefault().register(this)
        locationRepository.refresh(LocalDate.now());

        swipeContainer.setOnRefreshListener {
            locationRepository.refresh(LocalDate.now(), true)
        };
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMensaMenuUpdatedEvent(event: MensaMenuUpdatedEvent) {
        locationAdapter?.mensaMenusRefreshed(event.mensaId)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRefreshMensaFinishedEvent(event: RefreshMensaFinishedEvent) {
        swipeContainer.isRefreshing = false
    }

    private var locationAdapter: LocationAdapter? = null

    public override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }
}
