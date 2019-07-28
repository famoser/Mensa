package ch.famoser.mensa.repositories

import android.content.res.AssetManager
import android.os.AsyncTask
import ch.famoser.mensa.models.Location
import ch.famoser.mensa.models.Mensa
import ch.famoser.mensa.repositories.tasks.RefreshETHMensaTask
import ch.famoser.mensa.repositories.tasks.RefreshUZHMensaTask
import ch.famoser.mensa.services.providers.AbstractMensaProvider
import ch.famoser.mensa.services.providers.ETHMensaProvider
import ch.famoser.mensa.services.providers.UZHMensaProvider
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

    private var uzhMensas: List<Mensa> = ArrayList()
    private val ethMensaProvider = ETHMensaProvider(assetManager)
    private val uzhMensaProvider = UZHMensaProvider(assetManager)

    fun getLocations(): MutableList<Location> {
        if (!initialized) {
            initialized = true

            loadLocations(ethMensaProvider)
            uzhMensas = loadLocations(uzhMensaProvider)
        }

        return locations
    }

    private fun loadLocations(mensaProvider: AbstractMensaProvider) : List<Mensa> {
        val locations = mensaProvider.getLocations()
        val mensas = ArrayList<Mensa>()
        for (location in locations) {
            for (mensa in location.mensas) {
                mensas.add(mensa)
                mensaMap[mensa.id] = mensa
            }
        }

        this.locations.addAll(locations)

        return mensas;
    }

    fun refresh(today: Date, force: Boolean = false) {
        if (!refreshed || force) {
            refreshed = true

            RefreshETHMensaTask(ethMensaProvider, today, "de")
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "lunch", "dinner")

            RefreshUZHMensaTask(uzhMensaProvider, today, "de")
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, *uzhMensas.toTypedArray())
        }
    }

    fun getMensa(mensaId: UUID): Mensa? {
        return mensaMap[mensaId]
    }
}
