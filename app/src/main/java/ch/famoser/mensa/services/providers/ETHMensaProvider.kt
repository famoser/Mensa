package ch.famoser.mensa.services.providers

import android.annotation.SuppressLint
import android.util.Log
import ch.famoser.mensa.models.Location
import ch.famoser.mensa.models.Mensa
import ch.famoser.mensa.models.Menu
import ch.famoser.mensa.services.IAssetService
import ch.famoser.mensa.services.ICacheService
import ch.famoser.mensa.services.SerializationService
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.net.URI
import java.net.URL
import java.util.*


class ETHMensaProvider(
    private val cacheService: ICacheService,
    private val assetService: IAssetService,
    private val serializationService: SerializationService
) : AbstractMensaProvider(cacheService) {

    companion object {
        const val CACHE_PROVIDER_PREFIX = "eth"
        const val MEAL_TIME_LUNCH = "lunch"
        const val MEAL_TIME_DINNER = "dinner"
    }

    private val mensaMap: MutableMap<Mensa, EthMensa> = HashMap()

    fun getMenus(time: String, date: Date, language: Language, ignoreCache: Boolean)
            : List<Mensa> {
        try {
            val menuByMensaIds = getMenuByMensaId(date, ignoreCache, time, language)

            for ((mensa, ethzMensa) in mensaMap) {
                val menus = menuByMensaIds[ethzMensa.getMapId()]
                if (menus != null) {
                    mensa.replaceMenus(menus)
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

        return mensaMap.keys.toList()
    }

    private fun getMenuByMensaId(
        date: Date,
        ignoreCache: Boolean,
        time: String,
        language: Language
    ): Map<String, List<Menu>> {
        val languageString = languageToString(language)
        if (!ignoreCache) {
            val menuByMensaIds = getMenuByMensaIdFromCache(date, time, languageString)
            if (menuByMensaIds != null) {
                return menuByMensaIds
            }
        }

        val menuByMensaIds = getMensaMenusFromSearchApi(languageString, date, time)

        for ((_, ethzMensa) in mensaMap) {
            val menus = menuByMensaIds[ethzMensa.getMapId()]
            if (menus == null && ethzMensa.timeSlug == time) {
                menuByMensaIds[ethzMensa.getMapId()] =
                    getMensaMenuFromApi(languageString, date, time, ethzMensa)
            }

            if (menus !== null && menus.any() && menus.all { it.isSomeEmpty() }) {
                val fallbackLanguage = fallbackLanguage(language)
                val fallbackLanguageString = languageToString(fallbackLanguage)
                val fallbackMenus = getMensaMenuFromApi(fallbackLanguageString, date, time, ethzMensa)

                val newMenus = ArrayList<Menu>(menus);
                var i = 0;
                while (i < newMenus.size && i < fallbackMenus.size) {
                    newMenus[i] = newMenus[i].mergeWithFallback(fallbackMenus[i]);
                    i++;
                }
                menuByMensaIds[ethzMensa.getMapId()] = newMenus;
            }
        }

        val cacheKey = getMensaIdCacheKey(date, time, languageString)
        cacheService.saveMensaIds(cacheKey, menuByMensaIds.keys.toList())

        for ((mensaId, menus) in menuByMensaIds) {
            cacheMenus(CACHE_PROVIDER_PREFIX, mensaId, date, languageString, menus)
        }

        return menuByMensaIds
    }

    private fun getMenuByMensaIdFromCache(
        date: Date,
        source: String,
        language: String
    ): Map<String, List<Menu>>? {
        val cacheKey = getMensaIdCacheKey(date, source, language)
        val impactedMensas = cacheService.readMensaIds(cacheKey) ?: return null

        val menuByMensaId = HashMap<String, List<Menu>>()
        for (mensaId in impactedMensas) {
            val menus = tryGetMenusFromCache(CACHE_PROVIDER_PREFIX, mensaId, date, language)
            if (menus != null) {
                menuByMensaId[mensaId] = menus
            }
        }

        return menuByMensaId
    }

    private fun getMensaIdCacheKey(date: Date, source: String, language: String): String {
        val dateSlug = getDateTimeString(date)
        return "$CACHE_PROVIDER_PREFIX.$source.$dateSlug.$language"
    }

    @SuppressLint("UseSparseArrays")
    private fun getMensaMenusFromSearchApi(
        language: String,
        date: Date,
        time: String
    ): MutableMap<String, List<Menu>> {
        var apiMensas: List<ApiMensaSearch> = ArrayList();

        val dateSlug = getDateTimeString(date)
        val url =
            URL("https://www.webservices.ethz.ch/gastro/v1/RVRI/Q1E1/meals/$language/$dateSlug/$time?language=$language")

        try {
            val json = url.readText()

            apiMensas = serializationService.deserializeList(json)
        } catch (e: java.lang.Exception) {
            Log.e("ETHMensaProvider", "request for mensa search failed: $url", e);
        }

        val menuByMensaIds = HashMap<String, List<Menu>>()
        for (apiMensa in apiMensas) {
            val menus =
                apiMensa.meals.map { parseApiMenu(it) }.filter { !isNoMenuNotice(it, language) }

            menuByMensaIds[apiMensa.id.toString() + "_" + time] = menus
        }

        return menuByMensaIds
    }

    private fun isNoMenuNotice(menu: Menu, language: String): Boolean {
        val invalidMenus = when (language) {
            "en" -> {
                arrayOf(
                    "We look forward to serving you this menu again soon!",
                    "is closed",
                    "Closed"
                )
            }
            "de" -> {
                arrayOf(
                    "Dieses Menu servieren wir Ihnen gerne bald wieder!",
                    "geschlossen",
                    "Geschlossen"
                )
            }
            else -> {
                arrayOf()
            }
        }

        return invalidMenus.any { menu.description.contains(it) || menu.title == it }
    }

    @SuppressLint("UseSparseArrays")
    private fun getMensaMenuFromApi(
        language: String,
        date: Date,
        time: String,
        mensa: EthMensa
    ): List<Menu> {
        val dateSlug = getDateTimeString(date)
        val url =
            URL("https://www.webservices.ethz.ch/gastro/v1/RVRI/Q1E1/mensas/${mensa.idSlug}/$language/menus/daily/$dateSlug/$time?language=$language")

        try {
            val json = url.readText()

            val apiMensa = serializationService.deserialize<ApiMensa>(json)
            return apiMensa.menu.meals.map { parseApiMenu(it) }
        } catch (e: java.lang.Exception) {
            Log.e("ETHMensaProvider", "request for single mensa ${mensa.title} failed: $url", e);

            return ArrayList();
        }
    }

    private fun parseApiMenu(apiMeal: ApiMeal): Menu {
        var label = apiMeal.label
        var descriptionLines = apiMeal.description
        if (label.isEmpty() && apiMeal.description.isNotEmpty()) {
            label = apiMeal.description.first()
            if (descriptionLines.size > 1) {
                descriptionLines = descriptionLines.subList(1, descriptionLines.size)
            } else {
                descriptionLines = ArrayList()
            }
        }

        val description = normalizeText(descriptionLines.joinToString(separator = "\n").trim())

        val prices = apiMeal.prices
            .run { arrayOf(student, staff, extern) }
            .filterNot { it.isNullOrEmpty() || it == "NaN" }
            .filterNotNull()
            .toTypedArray()

        val allergens = apiMeal.allergens
            .fold(ArrayList<String>(), { acc, apiAllergen -> acc.add(apiAllergen.label); acc })
            .joinToString(separator = ", ")

        return Menu(
            label,
            description,
            prices,
            allergens
        )
    }

    override fun getLocations(): List<Location> {
        val json: String = assetService.readStringFile("eth/locations.json") ?: return ArrayList()
        val ethLocations = serializationService.deserializeList<EthLocation>(json)

        return ethLocations.map { ethLocation ->
            Location(ethLocation.title, ethLocation.mensas.map {
                val mensaId = UUID.fromString(it.id)
                val imageName = it.infoUrlSlug.substring(it.infoUrlSlug.indexOf("/") + 1)
                val mensa = Mensa(
                    mensaId,
                    it.title,
                    it.mealTime,
                    URI("https://ethz.ch/de/campus/erleben/gastronomie-und-einkaufen/gastronomie/restaurants-und-cafeterias/" + it.infoUrlSlug),
                    "eth/images/$imageName.jpg"
                )
                mensaMap[mensa] = it
                mensa
            })
        }
    }

    @Serializable
    data class ApiMensaSearch(
        val id: Int,
        val mensa: String,
        val daytime: String,
        val hours: ApiHours,
        val meals: List<ApiMeal>
    )

    @Serializable
    data class ApiMensa(
        val id: Int,
        val mensa: String,
        val daytime: String,
        val hours: ApiHours,
        val menu: ApiMenu
    )

    @Serializable
    data class ApiMenu(
        val date: String,
        val day: String,
        val meals: List<ApiMeal>
    )

    @Serializable
    data class ApiHours(val opening: List<ApiOpening>, val mealtime: List<ApiMealtime>)

    @Serializable
    data class ApiOpening(val from: String, val to: String, val type: String)

    @Serializable
    data class ApiMealtime(val from: String, val to: String, val type: String)

    @Serializable
    data class ApiMeal(
        val id: Int,
        val label: String,
        val description: List<String>,
        val prices: ApiPrices,
        val allergens: List<ApiAllergen>
    )

    @Serializable
    data class ApiPrices(val student: String?, val staff: String?, val extern: String?)

    @Serializable
    data class ApiAllergen(val allergen_id: Int, val label: String)

    @Serializable
    data class EthLocation(val title: String, val mensas: List<EthMensa>)

    @Serializable
    data class EthMensa(
        val id: String,
        val title: String,
        val mealTime: String,
        val idSlug: Int,
        val timeSlug: String,
        val infoUrlSlug: String
    ) {
        fun getMapId(): String {
            return idSlug.toString() + "_" + timeSlug
        }
    }
}