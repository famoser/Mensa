package ch.famoser.mensa.services.providers

import ch.famoser.mensa.models.Location
import ch.famoser.mensa.models.Mensa
import ch.famoser.mensa.models.Menu
import ch.famoser.mensa.services.IAssetService
import ch.famoser.mensa.services.ICacheService
import ch.famoser.mensa.services.SerializationService
import kotlinx.serialization.Serializable
import java.io.OutputStream
import java.lang.Float.parseFloat
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.util.Date
import java.util.UUID
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class UZHMensaProvider2(
    private val cacheService: ICacheService,
    private val assetService: IAssetService,
    private val serializationService: SerializationService
) : AbstractMensaProvider(cacheService) {

    companion object {
        const val CACHE_PROVIDER_PREFIX = "uzh_food2025"
    }

    override fun getLocations(): List<Location> {
        val json: String =
            assetService.readStringFile("uzh/locations_food2025.json") ?: return ArrayList()
        val uzhLocations = serializationService.deserializeList<UzhLocation>(json)

        return uzhLocations.map { uzhLocation ->
            Location(uzhLocation.title, uzhLocation.mensas.map {
                val mensa = Mensa(
                    UUID.fromString(it.id),
                    it.title,
                    it.mealTime,
                    URI("https://www.mensa.uzh.ch/de/menueplaene/${it.infoUrlSlug}.html")
                )
                mensaMap[mensa] = it
                mensa
            })
        }
    }

    fun getMensaGroups(): List<String> {
        return mensaMap.values.map { it.locationId }.asIterable().distinct()
    }

    private val mensaMap: MutableMap<Mensa, UzhMensa> = HashMap()

    fun getMenus(mensaGroup: String, language: Language, ignoreCache: Boolean): List<Mensa> {
        val impactedMensas = mensaMap.values.filter { it.locationId == mensaGroup }

        return try {
            val locationJson = loadLocation(mensaGroup, ignoreCache) ?: return emptyList()

            val apiRoot = serializationService.deserialize<ApiRoot>(locationJson)
            val menuPerMensa = parseApiRoot(apiRoot, impactedMensas, language)

            val updateMensas = ArrayList<Mensa>()
            for (uzhMensa in menuPerMensa.entries) {
                val mensa = mensaMap.entries.find { it.value == uzhMensa.key }?.key
                if (mensa == null) {
                    continue
                }

                mensa.replaceMenus(uzhMensa.value)
                updateMensas.add(mensa)
            }

            updateMensas
        } catch (ex: Exception) {
            ex.printStackTrace()

            emptyList()
        }
    }

    private fun loadLocation(
        locationId: String,
        ignoreCache: Boolean
    ): String? {
        val date = Date()
        val cacheKey = CACHE_PROVIDER_PREFIX + getDateTimeString(date) + locationId

        if (!ignoreCache) {
            val location = cacheService.readString(cacheKey)

            if (location != null) {
                return location
            }
        }

        val location = loadLocationFromApi(locationId)
        if (location != null) {
            cacheService.saveString(cacheKey, location)
        }

        return location
    }

    private fun loadLocationFromApi(
        locationId: String
    ): String? {
        val url = URL("https://api.app.food2050.ch/")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"

        try {
            // Set up the connection
            connection.doOutput = true
            connection.setRequestProperty("Accept", "*/*")
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("Host", "api.app.food2050.ch")

            val requestBody = "{ \"query\":\"query ExampleQuery(\$locationId: String!) { location(id: \$locationId) { kitchens { slug, todayOffer { digitalMenuItems { displayName recipeSelectorConfig recipe { title(returnAll: true) prices { amount category { title } } allergensList } } } } } }\",\"variables\":{ \"locationId\":\"$locationId\" },\"operationName\":\"ExampleQuery\" }"

            val outputStream: OutputStream = connection.outputStream
            outputStream.write(requestBody.toByteArray())
            outputStream.flush()
            outputStream.close()

            return connection.inputStream.bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            connection.disconnect()
        }

        return null
    }

    private fun parseApiRoot(root: ApiRoot, mensas: List<UzhMensa>, language: Language): HashMap<UzhMensa, ArrayList<Menu>> {
        val result = HashMap<UzhMensa, ArrayList<Menu>>()

        for (mensa in mensas) {
            val kitchen = root.data?.location?.kitchens?.find { it.slug == mensa.slug }
            val relevantMenus = kitchen?.todayOffer?.flatMap {
                offer -> offer.digitalMenuItems?.filter { mensa.recipeSelector == null || it.recipeSelectorConfig?.name?.contains(mensa.recipeSelector) == true } ?: emptyList()
            }

            if (relevantMenus == null) {
                continue;
            }

            val parsedMenus = ArrayList<Menu>()
            for (relevantMenu in relevantMenus) {
                val title = relevantMenu.displayName
                val description = if (language == Language.German) relevantMenu.recipe?.title?.de else relevantMenu.recipe?.title?.en
                val priceStrings = relevantMenu.recipe?.prices?.mapNotNull { it.amount }?.toTypedArray<String>() ?: emptyArray<String>()
                val price = priceStrings.map { parseFloat(it) } .map { String.format("%.2f", it) }.toTypedArray()
                val allergens = relevantMenu.recipe?.allergensList?.joinToString(separator = ", ")
                if (title == null || description == null) {
                    continue;
                }

                val menu = Menu(title, description, price, allergens)
                parsedMenus.add(menu)
            }

            result[mensa] = parsedMenus
        }

        return result
    }

    private fun isNoMenuNotice(menu: Menu, language: String): Boolean {
        when (language) {
            "en" -> {
                val invalidMenus = arrayOf("no dinner", "is closed")
                return invalidMenus.any { menu.description.contains(it) }
            }

            "de" -> {
                val invalidMenus = arrayOf("kein Abendessen", "geschlossen")
                return invalidMenus.any { menu.description.contains(it) }
            }
        }
        return false
    }

    @Serializable
    protected class ApiRoot {
        var data: ApiData? = null
    }

    @Serializable
    protected class ApiData {
        var location: ApiLocation? = null
    }

    @Serializable
    protected class ApiLocation {
        var kitchens: Array<Kitchen>? = null
    }

    @Serializable
    protected class Kitchen {
        var slug: String? = null
        var todayOffer: Array<Offer>? = null
    }

    @Serializable
    protected class Offer {
        var digitalMenuItems: Array<MenuItem>? = null
    }

    @Serializable
    protected class MenuItem {
        var displayName: String? = null
        var recipeSelectorConfig: SelectorConfig? = null
        var recipe: Recipe? = null
    }

    @Serializable
    protected class SelectorConfig {
        var name: String? = null
    }

    @Serializable
    protected class Recipe {
        var title: Translated? = null
        var prices: Array<Price>? = null
        var allergensList: Array<String>? = null

    }

    @Serializable
    protected class Translated {
        var de: String? = null
        var en: String? = null
    }

    @Serializable
    protected class Price {
        var amount: String? = null
        var category: PriceCategory? = null
    }

    @Serializable
    protected class PriceCategory {
        var title: String? = null
    }

    @Serializable
    data class UzhLocation(val title: String, val mensas: List<UzhMensa>)

    @Serializable
    open class UzhMensa(
        val id: String,
        val title: String,
        val mealTime: String,
        val infoUrlSlug: String,
        val locationId: String,
        val slug: String,
        val recipeSelector: String? = null,
    )
}
