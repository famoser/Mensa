package ch.famoser.mensa.repositories

import android.content.Context
import android.os.AsyncTask
import ch.famoser.mensa.events.RefreshMensaFinishedEvent
import ch.famoser.mensa.models.Location
import ch.famoser.mensa.models.Mensa
import ch.famoser.mensa.repositories.tasks.RefreshETHMensaTask
import ch.famoser.mensa.repositories.tasks.RefreshUZHMensaTask
import ch.famoser.mensa.services.*
import ch.famoser.mensa.services.providers.AbstractMensaProvider
import ch.famoser.mensa.services.providers.ETHMensaProvider2
import ch.famoser.mensa.services.providers.UZHMensaProvider3
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*

class LocationRepository internal constructor(
    private val cacheService: ICacheService,
    assetService: IAssetService,
    serializationService: SerializationService
) {
    companion object {
        private var defaultInstance: LocationRepository? = null

        fun getInstance(context: Context): LocationRepository {
            synchronized(this) {
                if (defaultInstance == null) {
                    val sharedPreferences = context.applicationContext.getSharedPreferences("ch.famoser.mensa_preferences",  Context.MODE_PRIVATE)
                    val serializationService = SerializationService()
                    val cacheService = CacheService(sharedPreferences, serializationService)
                    val assetService = AssetService(context.assets)

                    defaultInstance = LocationRepository(cacheService, assetService, serializationService)
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
    private var refreshed: Date? = null
    private var activeRefreshingTasks = 0
    private val locations: MutableList<Location> = LinkedList()

    private val ethMensaProvider = ETHMensaProvider2(cacheService, assetService, serializationService)
    private val uzhMensaProvider = UZHMensaProvider3(cacheService, assetService, serializationService)

    fun isRefreshPending(): Boolean {
        val now = Date(System.currentTimeMillis());
        val refreshed = refreshed;

        return refreshed == null || !isSameDay(now, refreshed);
    }

    private fun isSameDay(now: Date, other: Date): Boolean {
        return now.year == other.year && now.month == other.month && now.day == other.day;
    }

    fun getLocations(): MutableList<Location> {
        if (!initialized) {
            initialized = true

            loadLocations(ethMensaProvider)
            loadLocations(uzhMensaProvider)
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

        return mensas
    }

    fun refresh(date: Date, language: AbstractMensaProvider.Language, ignoreCache: Boolean = false) {
        synchronized(this) {
            if (activeRefreshingTasks > 0) {
                return
            }

            refreshed = Date(System.currentTimeMillis())

            // how many tasks are launched
            // 2 because one ETH and one UZH task
            activeRefreshingTasks = 2
        }

        cacheService.startObserveCacheUsage()

        RefreshETHMensaTask(ethMensaProvider, date, language, ignoreCache)
            .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)

        RefreshUZHMensaTask(uzhMensaProvider, date, language, ignoreCache)
            .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
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
