package com.example.mensa.activities

import android.content.BroadcastReceiver
import android.content.IntentFilter
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.mensa.adapters.LocationAdapter

import com.example.mensa.dummy.DummyContent
import kotlinx.android.synthetic.main.content_main.*
import android.content.Intent
import android.content.Context
import com.example.mensa.services.EventBus
import java.util.*


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

        val locationAdapter = LocationAdapter(
            this,
            DummyContent.LOCATIONS,
            twoPane
        )
        location_list.adapter = locationAdapter

        myReceiver = MyBroadcastReceiver(locationAdapter)
        val mensaMenuFilter = IntentFilter(EventBus.MENSA_MENUS_REFRESHED);
        registerReceiver(myReceiver, mensaMenuFilter);

        val eventBus = EventBus { sendBroadcast(it) }
        DummyContent.initialize(assets, eventBus)
    }

    class MyBroadcastReceiver(private val locationAdapter: LocationAdapter) : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val mensaId = intent.getStringExtra(EventBus.MENSA_MENUS_REFRESHED_MENSA_ID)
            locationAdapter.mensaMenusRefreshed(UUID.fromString(mensaId));
        }
    }

    private var myReceiver: BroadcastReceiver? = null

    public override fun onDestroy() {
        super.onDestroy()

        if (myReceiver != null) {
            unregisterReceiver(myReceiver)
        }
    }
}
