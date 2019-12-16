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
import ch.famoser.mensa.services.providers.ETHMensaProvider
import ch.famoser.mensa.services.providers.UZHRSSMensaProvider
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.defaultSharedPreferences
import org.jetbrains.anko.doAsync
import java.time.Instant
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class LocationRepository internal constructor(
    private val cacheService: ICacheService,
    assetService: IAssetService,
    serializationService: ISerializationService
) {
    companion object {
        private var defaultInstance: LocationRepository? = null

        fun getInstance(context: Context): LocationRepository {
            synchronized(this) {
                if (defaultInstance == null) {
                    val sharedPreferences = context.applicationContext.defaultSharedPreferences
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

    private var uzhMensas: List<Mensa> = ArrayList()
    private val ethMensaProvider = ETHMensaProvider(cacheService, assetService, serializationService)
    private val uzhMensaProvider = UZHRSSMensaProvider(cacheService, assetService, serializationService)

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

        return mensas
    }

    fun refresh(today: Date, language: AbstractMensaProvider.Language, ignoreCache: Boolean = false) {
        synchronized(this) {
            if (activeRefreshingTasks > 0) {
                return
            }

            refreshed = Date(System.currentTimeMillis())

            // how many tasks are launched
            // 3 because one ETH and two UZH tasks
            activeRefreshingTasks = 3
        }

        cacheService.startObserveCacheUsage()

        RefreshETHMensaTask(ethMensaProvider, today, language, ignoreCache)
            .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, ETHMensaProvider.MEAL_TIME_LUNCH, ETHMensaProvider.MEAL_TIME_DINNER)

        val half = uzhMensas.size / 2
        val batch1 = uzhMensas.subList(0, half)
        val batch2 = uzhMensas.subList(half, uzhMensas.size)

        RefreshUZHMensaTask(uzhMensaProvider, today, language, ignoreCache)
            .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, *batch1.toTypedArray())

        RefreshUZHMensaTask(uzhMensaProvider, today, language, ignoreCache)
            .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, *batch2.toTypedArray())
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
