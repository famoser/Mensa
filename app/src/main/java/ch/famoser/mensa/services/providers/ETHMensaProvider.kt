package ch.famoser.mensa.services.providers

import android.annotation.SuppressLint
import android.content.res.AssetManager
import android.net.Uri
import ch.famoser.mensa.models.Location
import ch.famoser.mensa.models.Mensa
import java.net.URL
import java.util.*
import kotlin.collections.HashMap
import ch.famoser.mensa.models.Menu
import ch.famoser.mensa.services.CacheService
import ch.famoser.mensa.services.SerializationService
import kotlin.collections.ArrayList


class ETHMensaProvider(
    private val cacheService: CacheService,
    assetManager: AssetManager,
    private val serializationService: SerializationService
) :
    AbstractMensaProvider(cacheService, assetManager, serializationService) {
    companion object {
        const val CACHE_PROVIDER_PREFIX = "eth"
    }

    private val mensaMap: MutableMap<Mensa, EthMensa> = HashMap()

    fun getMenus(source: String, date: Date, language: String, ignoreCache: Boolean)
            : List<Mensa> {
        try {
            val menuByMensaIds = getMenuByMensaId(date, ignoreCache, source, language)

            val refreshedMensas = ArrayList<Mensa>()
            for ((mensa, ethzMensa) in mensaMap) {
                val menus = menuByMensaIds[ethzMensa.idSlug]
                if (menus != null) {
                    mensa.replaceMenus(menus)
                    refreshedMensas.add(mensa)
                }
            }

            return refreshedMensas
        } catch (ex: Exception) {
            ex.printStackTrace()

            return ArrayList()
        }
    }

    private fun getMenuByMensaId(
        date: Date,
        ignoreCache: Boolean,
        source: String,
        language: String
    ): Map<Int, List<Menu>> {
        if (!ignoreCache) {
            val menuByMensaIds = getMenuByMensaIdFromCache(date, source, language)
            if (menuByMensaIds != null) {
                return menuByMensaIds
            }
        }

        val menuByMensaIds = getMenuByMensaIdFromApi(language, date, source)

        val cacheKey = getMensaIdCacheKey(date, source, language)
        cacheService.saveMensaIds(cacheKey, menuByMensaIds.keys.toList())

        for ((mensaId, menus) in menuByMensaIds) {
            cacheMenus(CACHE_PROVIDER_PREFIX, mensaId.toString(), date, language, menus)
        }

        return menuByMensaIds
    }

    private fun getMenuByMensaIdFromCache(date: Date, source: String, language: String): Map<Int, List<Menu>>? {
        val cacheKey = getMensaIdCacheKey(date, source, language)
        val impactedMensas = cacheService.readMensaIds(cacheKey) ?: return null

        val menuByMensaId = HashMap<Int, List<Menu>>()
        for (mensaId in impactedMensas) {
            val menus = tryGetMenusFromCache(CACHE_PROVIDER_PREFIX, mensaId.toString(), date, language)
            if (menus != null) {
                menuByMensaId[mensaId] = menus
            }
        }

        return menuByMensaId
    }

    private fun getMensaIdCacheKey(date: Date, source: String, language: String): String {
        val dateSlug = getDateTimeString(date);
        return "$CACHE_PROVIDER_PREFIX.$source.$dateSlug.$language"
    }

    @SuppressLint("UseSparseArrays")
    private fun getMenuByMensaIdFromApi(
        language: String,
        date: Date,
        source: String
    ): Map<Int, List<Menu>> {
        val dateSlug = getDateTimeString(date);
        val json = URL("https://www.webservices.ethz.ch/gastro/v1/RVRI/Q1E1/meals/$language/$dateSlug/$source")
            .readText()

        val apiMensas = serializationService.deserializeList(json, ApiMensa::class.java);

        val menuByMensaIds = HashMap<Int, List<Menu>>()
        for (apiMensa in apiMensas) {
            val menus = apiMensa.meals.map { apiMeal ->
                Menu(
                    apiMeal.label,
                    normalizeText(apiMeal.description.joinToString(separator = "\n")),
                    apiMeal.prices.run { arrayOf<String?>(student, staff, extern) }.filterNotNull().toTypedArray(),
                    apiMeal.allergens
                        .fold(ArrayList<String>(), { acc, apiAllergen -> acc.add(apiAllergen.label); acc })
                        .joinToString(separator = ", ")
                )
            }

            menuByMensaIds[apiMensa.id] = menus
        }

        return menuByMensaIds
    }

    override fun getLocations(): List<Location> {
        val ethLocations = super.readJsonAssetFileToListOfT("eth/inventory.json", EthLocation::class.java);

        return ethLocations.map { ethLocation ->
            Location(ethLocation.title, ethLocation.mensas.map {
                val mensaId = UUID.fromString(it.id)
                val imageName = it.infoUrlSlug.substring(it.infoUrlSlug.indexOf("/") + 1)
                val mensa = Mensa(
                    mensaId,
                    it.title,
                    it.mealTime,
                    Uri.parse("https://ethz.ch/de/campus/erleben/gastronomie-und-einkaufen/gastronomie/restaurants-und-cafeterias/" + it.infoUrlSlug),
                    "eth/images/$imageName.jpg"
                )
                mensaMap[mensa] = it
                mensa
            })
        }
    }

    data class ApiMensa(val id: Int, val mensa: String, val daytime: String, val hours: ApiHours, val meals: List<ApiMeal>)
    data class ApiHours(val opening: List<ApiOpening>, val mealtime: List<ApiMealtime>)
    data class ApiOpening(val from: String, val to: String, val type: String)
    data class ApiMealtime(val from: String, val to: String, val type: String)
    data class ApiMeal(
        val id: Int,
        val type: String,
        val label: String,
        val description: List<String>,
        val position: Int,
        val prices: ApiPrices,
        val allergens: List<ApiAllergen>,
        val origins: List<ApiOrigin>
    )

    data class ApiPrices(val student: String, val staff: String, val extern: String)
    data class ApiAllergen(val allergen_id: Int, val label: String)
    data class ApiOrigin(val origin_id: Int, val label: String)

    data class EthLocation(val title: String, val mensas: List<EthMensa>)
    data class EthMensa(
        val id: String,
        val title: String,
        val mealTime: String,
        val idSlug: Int,
        val timeSlug: String,
        val infoUrlSlug: String
    )
}