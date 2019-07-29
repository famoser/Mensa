package ch.famoser.mensa.repositories

import android.content.Context
import android.content.res.AssetManager
import android.os.AsyncTask
import android.preference.PreferenceManager
import ch.famoser.mensa.events.RefreshMensaFinishedEvent
import ch.famoser.mensa.models.Location
import ch.famoser.mensa.models.Mensa
import ch.famoser.mensa.repositories.tasks.RefreshETHMensaTask
import ch.famoser.mensa.repositories.tasks.RefreshUZHMensaTask
import ch.famoser.mensa.services.CacheService
import ch.famoser.mensa.services.SerializationService
import ch.famoser.mensa.services.providers.AbstractMensaProvider
import ch.famoser.mensa.services.providers.ETHMensaProvider
import ch.famoser.mensa.services.providers.UZHMensaProvider
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
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
    }

    init {
        EventBus.getDefault().register(this)
    }

    private val mensaMap: MutableMap<UUID, Mensa> = HashMap()
    private var initialized = false
    private var refreshed = false
    private var activeRefreshingTasks = 0
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
        synchronized(this) {
            if (activeRefreshingTasks > 0 || (refreshed && !ignoreCache)) {
                return
            }

            activeRefreshingTasks = 2
            refreshed = true
        }

        cacheService.startObserveCacheUsage()

        RefreshETHMensaTask(ethMensaProvider, today, "de", ignoreCache)
            .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "lunch", "dinner")

        RefreshUZHMensaTask(uzhMensaProvider, today, "de", ignoreCache)
            .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, *uzhMensas.toTypedArray())
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRefreshMensaFinishedEvent(event: RefreshMensaFinishedEvent) {
        activeRefreshingTasks--

        if (activeRefreshingTasks == 0) {
            cacheService.removeAllUntouchedCacheEntries()
        }
    }

    fun getMensa(mensaId: UUID): Mensa? {
        return mensaMap[mensaId]
    }

    fun refreshActive(): Boolean {
        return activeRefreshingTasks > 0
    }
}
