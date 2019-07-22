package com.example.mensa.repositories

import android.content.res.AssetManager
import android.os.AsyncTask
import com.example.mensa.models.Location
import com.example.mensa.models.Mensa
import com.example.mensa.repositories.tasks.RefreshMensaTask
import com.example.mensa.services.providers.AbstractMensaProvider
import com.example.mensa.services.providers.ETHMensaProvider
import com.example.mensa.services.providers.UZHMensaProvider
import java.time.LocalDate
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class LocationRepository internal constructor(private val assetManager: AssetManager) {
    companion object {
        private var defaultInstance: LocationRepository? = null

        fun getInstance(assetManager: AssetManager): LocationRepository {
            synchronized(this) {
                if (defaultInstance == null) {
                    defaultInstance = LocationRepository(assetManager)
                }

                return defaultInstance!!
            }
        }

        fun getInstance(): LocationRepository {
            synchronized(this) {
                if (defaultInstance == null) {
                    throw NotImplementedError("You need to supply the asset manager if the repository has not been constructed yet.")
                }

                return defaultInstance!!
            }
        }
    }

    private val mensaMap: MutableMap<UUID, Mensa> = HashMap()
    private var initialized = false
    private var refreshed = false
    private val locations: MutableList<Location> = LinkedList()

    private val mensaByProvider: MutableMap<AbstractMensaProvider, ArrayList<Mensa>> = HashMap()

    fun getLocations(): MutableList<Location> {
        if (!initialized) {
            initialized = true
            initialize()
        }

        return locations
    }

    private fun initialize() {
        val ethProvider = ETHMensaProvider(assetManager)
        loadLocations(ethProvider)

        val uzhProvider = UZHMensaProvider(assetManager)
        loadLocations(uzhProvider)
    }

    private fun loadLocations(mensaProvider: AbstractMensaProvider) {
        val locations = mensaProvider.getLocations()
        val mensas = ArrayList<Mensa>()
        for (location in locations) {
            for (mensa in location.mensas) {
                mensas.add(mensa)
                mensaMap[mensa.id] = mensa
            }
        }

        mensaByProvider[mensaProvider] = mensas
        this.locations.addAll(locations)
    }

    fun refresh(today: LocalDate, force: Boolean = false) {
        if (!refreshed || force) {
            refreshed = true

            for ((provider, mensas) in mensaByProvider) {
                RefreshMensaTask(provider, today)
                    .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, *mensas.toTypedArray())
            }
        }
    }

    fun getMensa(mensaId: UUID): Mensa? {
        return mensaMap[mensaId]
    }
}
