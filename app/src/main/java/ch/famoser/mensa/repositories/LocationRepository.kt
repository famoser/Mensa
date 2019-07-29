package ch.famoser.mensa.repositories

import android.content.Context
import android.content.res.AssetManager
import android.os.AsyncTask
import android.preference.PreferenceManager
import ch.famoser.mensa.models.Location
import ch.famoser.mensa.models.Mensa
import ch.famoser.mensa.repositories.tasks.RefreshETHMensaTask
import ch.famoser.mensa.repositories.tasks.RefreshUZHMensaTask
import ch.famoser.mensa.services.CacheService
import ch.famoser.mensa.services.SerializationService
import ch.famoser.mensa.services.providers.AbstractMensaProvider
import ch.famoser.mensa.services.providers.ETHMensaProvider
import ch.famoser.mensa.services.providers.UZHMensaProvider
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class LocationRepository internal constructor(
    private val cacheService: CacheService,
    assetManager: AssetManager,
    serializationService: SerializationService
) {
    companion object {
        private var defaultInstance: LocationRepository? = null

        fun getInstance(context: Context): LocationRepository {
            synchronized(this) {
                if (defaultInstance == null) {
                    val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
                    val serializationService = SerializationService()
                    val cacheService = CacheService(sharedPreferences, serializationService)
                    val assetManager = context.assets

                    defaultInstance = LocationRepository(cacheService, assetManager, serializationService)
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
    private val ethMensaProvider = ETHMensaProvider(cacheService, assetManager, serializationService)
    private val uzhMensaProvider = UZHMensaProvider(cacheService, assetManager, serializationService)

    fun getLocations(): MutableList<Location> {
        if (!initialized) {
            initialized = true

            loadLocations(ethMensaProvider)
            uzhMensas = loadLocations(uzhMensaProvider)
        }

        return locations
    }

    fun someMenusLoaded(): Boolean {
        return mensaMap.values.filter { m -> m.menus.isNotEmpty() }.any()
    }

    private fun loadLocations(mensaProvider: AbstractMensaProvider): List<Mensa> {
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

    fun refresh(today: Date, ignoreCache: Boolean = false) {
        if (!refreshed || ignoreCache) {
            refreshed = true

            cacheService.startObserveUsedCacheUsage()

            RefreshETHMensaTask(ethMensaProvider, today, "de", false)
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "lunch", "dinner")

            RefreshUZHMensaTask(uzhMensaProvider, today, "de", false)
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, *uzhMensas.toTypedArray())

            cacheService.removeAllUntouchedCacheEntries()
        }
    }

    fun getMensa(mensaId: UUID): Mensa? {
        return mensaMap[mensaId]
    }
}
