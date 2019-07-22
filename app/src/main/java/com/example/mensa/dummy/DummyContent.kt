package com.example.mensa.dummy

import android.content.res.AssetManager
import com.example.mensa.services.RefreshMensaTask
import com.example.mensa.services.providers.ETHMensaProvider
import com.example.mensa.models.Location
import com.example.mensa.models.Mensa
import com.example.mensa.services.EventBus
import com.example.mensa.services.providers.AbstractMensaProvider
import com.example.mensa.services.providers.UZHMensaProvider
import java.time.LocalDate
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/**
 * Helper class for providing sample content for user interfaces.
 */
object DummyContent {

    val MENSA_MAP: MutableMap<UUID, Mensa> = HashMap()
    val LOCATIONS: MutableList<Location> = LinkedList()

    val MENSA_BY_PROVIDER: MutableMap<AbstractMensaProvider, ArrayList<Mensa>> = HashMap()

    fun initialize(assetManager: AssetManager, eventBus: EventBus) {
        val today = LocalDate.now();

        val ethProvider = ETHMensaProvider(assetManager);
        loadLocations(ethProvider)

        val uzhProvider = UZHMensaProvider(assetManager);
        loadLocations(uzhProvider)

        refreshMensas(today, eventBus)
    }

    private fun loadLocations(mensaProvider: AbstractMensaProvider) {
        val locations = mensaProvider.getLocations();
        val mensas = ArrayList<Mensa>()
        for (location in locations) {
            for (mensa in location.mensas) {
                mensas.add(mensa)
                MENSA_MAP[mensa.id] = mensa
            }
        }

        MENSA_BY_PROVIDER[mensaProvider] = mensas
        LOCATIONS.addAll(locations)
    }

    private fun refreshMensas(today: LocalDate, eventBus: EventBus) {
        for ((provider, mensas) in MENSA_BY_PROVIDER) {
            RefreshMensaTask(provider, today, eventBus).execute(*mensas.toTypedArray());
        }
    }
}
