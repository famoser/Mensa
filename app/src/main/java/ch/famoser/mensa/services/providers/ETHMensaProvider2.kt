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


class ETHMensaProvider2(
    private val cacheService: ICacheService,
    private val assetService: IAssetService,
    private val serializationService: SerializationService
) : AbstractMensaProvider(cacheService) {

    companion object {
        const val CACHE_PROVIDER_PREFIX = "eth2"
        const val MEAL_TIME_LUNCH = "lunch"
        const val MEAL_TIME_DINNER = "dinner"
    }

    private val mensaMap: MutableMap<Mensa, EthMensa> = HashMap()

    fun getMenus(date: Date, language: Language, ignoreCache: Boolean)
            : List<Mensa> {
        try {
            val menuByFacilityIds = getMenuByFacilityId(date, ignoreCache, language)

            for ((mensa, ethzMensa) in mensaMap) {
                val menus = menuByFacilityIds[ethzMensa.getMapId()]
                mensa.replaceMenus(menus.orEmpty())
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

        return mensaMap.keys.toList()
    }

    private fun getMenuByFacilityId(
        date: Date,
        ignoreCache: Boolean,
        language: Language
    ): Map<String, List<Menu>> {
        val languageString = languageToString(language)
        if (!ignoreCache) {
            val menuByMensaIds = getMenuByMensaIdFromCache(date, languageString)
            if (menuByMensaIds != null) {
                return menuByMensaIds
            }
        }

        val menuByFacilityIds = getMensaMenusFromCookpit(languageString, date, ignoreCache)

        val cacheKey = getMensaIdCacheKey(date, languageString)
        cacheService.saveMensaIds(cacheKey, menuByFacilityIds.keys.toList())

        for ((facilityId, menus) in menuByFacilityIds) {
            cacheMenus(CACHE_PROVIDER_PREFIX, facilityId, date, languageString, menus)
        }

        return menuByFacilityIds
    }

    private fun getMenuByMensaIdFromCache(
        date: Date,
        language: String
    ): Map<String, List<Menu>>? {
        val cacheKey = getMensaIdCacheKey(date, language)
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

    private fun getMensaIdCacheKey(date: Date, language: String): String {
        val dateSlug = getDateTimeString(date)
        return "$CACHE_PROVIDER_PREFIX.$dateSlug.$language"
    }

    @SuppressLint("UseSparseArrays")
    private fun getMensaMenusFromCookpit(
        language: String,
        date: Date,
        ignoreCache: Boolean
    ): MutableMap<String, List<Menu>> {
        // observation: dateslug is ignored by API; all future entries are returned in any case
        val dateSlug = getDateTimeStringOfMonday(date)
        val url =
            URL("https://idapps.ethz.ch/cookpit-pub-services/v1/weeklyrotas?client-id=ethz-wcms&lang=$language&rs-first=0&rs-size=50&valid-after=$dateSlug")

        val json = getCachedRequest(url, ignoreCache)
            ?: throw java.lang.Exception("Cannot load web content")

        val apiMensas: ApiRoot = serializationService.deserialize(json)

        val cal = Calendar.getInstance()
        cal.time = date
        val dayOfWeek = cal[Calendar.DAY_OF_WEEK] - 1

        val menuByFacilityIds = HashMap<String, List<Menu>>()
        val currentWeekRotaArray = apiMensas.weeklyRotaArray.filter { it.validFrom == dateSlug }
        for (weeklyRotaArray in currentWeekRotaArray) {
            val today = weeklyRotaArray.dayOfWeekArray.firstOrNull { it.dayOfWeekCode == dayOfWeek }
                ?: continue

            for (openingHour in today.openingHourArray.orEmpty()) {
                for (mealTime in openingHour.mealTimeArray.orEmpty()) {
                    if (mealTime.lineArray == null) {
                        continue
                    }

                    val time = parseMealTime(mealTime.name) ?: continue
                    val menus =
                        mealTime.lineArray
                            .mapNotNull { parseApiLineArray(it.name, it.meal) }
                            .filter { !isNoMenuNotice(it, language) }

                    menuByFacilityIds[weeklyRotaArray.facilityId.toString() + "_" + time] = menus
                }
            }
        }

        return menuByFacilityIds
    }

    private fun parseMealTime(mealTime: String): String? {
        if (mealTime.lowercase().contains("mittag") || mealTime.lowercase().contains("lunch") || mealTime.lowercase().contains("pranzo")) {
            return MEAL_TIME_LUNCH
        }

        if (mealTime.lowercase().contains("abend") || mealTime.lowercase().contains("dinner") || mealTime.lowercase().contains("cena")) {
            return MEAL_TIME_DINNER
        }

        return null
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


    private fun parseApiLineArray(name: String, meal: ApiMeal?): Menu? {
        if (meal == null)  {
            return null
        }

        val description = meal.name.trim() + "\n" +
                meal.description.replace("\\s+".toRegex(), " ")

        val prices = meal.mealPriceArray.orEmpty()
            .map { String.format("%.2f", it.price) }
            .toTypedArray()

        val allergens = meal.allergenArray.orEmpty()
            .fold(ArrayList<String>()) { acc, apiAllergen -> acc.add(apiAllergen.desc); acc }
            .joinToString(separator = ", ")

        return Menu(
            name,
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
    data class EthLocation(val title: String, val mensas: List<EthMensa>)

    @Serializable
    data class EthMensa(
        val id: String,
        val title: String,
        val mealTime: String,
        val idSlug: Int,
        val facilityId: Int,
        val timeSlug: String,
        val infoUrlSlug: String
    ) {
        fun getMapId(): String {
            return facilityId.toString() + "_" + timeSlug
        }
    }

    @Serializable
    data class ApiRoot(
        @SerialName("weekly-rota-array")
        val weeklyRotaArray: List<ApiWeeklyRotaArray>,
    )

    @Serializable
    data class ApiWeeklyRotaArray(
        @SerialName("weekly-rota-id")
        val weeklyRotaId: Int,
        @SerialName("facility-id")
        val facilityId: Int,
        @SerialName("valid-from")
        val validFrom: String,
        @SerialName("day-of-week-array")
        val dayOfWeekArray: List<ApiDayOfWeekArray>,
        @SerialName("valid-to")
        val validTo: String? = null,
    )

    @Serializable
    data class ApiDayOfWeekArray(
        @SerialName("day-of-week-code")
        val dayOfWeekCode: Int,
        @SerialName("day-of-week-desc")
        val dayOfWeekDesc: String,
        @SerialName("day-of-week-desc-short")
        val dayOfWeekDescShort: String,
        @SerialName("opening-hour-array")
        val openingHourArray: List<ApiOpeningHourArray>? = null,
    )

    @Serializable
    data class ApiOpeningHourArray(
        @SerialName("time-from")
        val timeFrom: String,
        @SerialName("time-to")
        val timeTo: String,
        @SerialName("meal-time-array")
        val mealTimeArray: List<ApiMealTimeArray>? = null,
    )

    @Serializable
    data class ApiMealTimeArray(
        val name: String,
        @SerialName("time-from")
        val timeFrom: String,
        @SerialName("time-to")
        val timeTo: String,
        val menu: ApiMenu? = null,
        @SerialName("line-array")
        val lineArray: List<ApiLineArray>? = null,
    )

    @Serializable
    data class ApiMenu(
        @SerialName("menu-url")
        val menuUrl: String,
    )

    @Serializable
    data class ApiLineArray(
        val name: String,
        val meal: ApiMeal? = null,
    )

    @Serializable
    data class ApiMeal(
        @SerialName("line-id")
        val lineId: Int,
        val name: String,
        val description: String,
        @SerialName("price-unit-code")
        val priceUnitCode: Int,
        @SerialName("price-unit-desc")
        val priceUnitDesc: String,
        @SerialName("price-unit-desc-short")
        val priceUnitDescShort: String,
        @SerialName("meal-price-array")
        val mealPriceArray: List<ApiMealPriceArray>? = null,
        @SerialName("meal-class-array")
        val mealClassArray: List<ApiMealClassArray>? = null,
        @SerialName("allergen-array")
        val allergenArray: List<ApiAllergenArray>? = null,
    )

    @Serializable
    data class ApiMealPriceArray(
        val price: Double,
        @SerialName("customer-group-code")
        val customerGroupCode: Int,
        @SerialName("customer-group-position")
        val customerGroupPosition: Int,
        @SerialName("customer-group-desc")
        val customerGroupDesc: String,
        @SerialName("customer-group-desc-short")
        val customerGroupDescShort: String,
    )

    @Serializable
    data class ApiMealClassArray(
        val code: Int,
        val position: Int,
        @SerialName("desc-short")
        val descShort: String,
        val desc: String,
    )


    @Serializable
    data class ApiAllergenArray(
        val code: Long,
        val position: Long,
        @SerialName("desc-short")
        val descShort: String,
        val desc: String,
    )
}