package ch.famoser.mensa.services.providers

import ch.famoser.mensa.BuildConfig
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.UUID

class UZHMensaProvider3(
    private val cacheService: ICacheService,
    private val assetService: IAssetService,
    private val serializationService: SerializationService
) : AbstractMensaProvider(cacheService) {

    companion object {
        const val CACHE_PROVIDER_PREFIX = "uzh_zfv"
    }

    override fun getLocations(): List<Location> {
        val json: String =
            assetService.readStringFile("uzh/locations_zfv.json") ?: return ArrayList()
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

    private val mensaMap: MutableMap<Mensa, UzhMensa> = HashMap()

    fun getMenus(language: Language, ignoreCache: Boolean): List<Mensa> {
        return try {
            val json = loadFromApi(ignoreCache) ?: return emptyList()

            val apiRoot = serializationService.deserialize<ApiRoot>(json)
            val menuPerMensa = parseApiRoot(apiRoot, mensaMap.values.toList(), language)

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
            emptyList()
        }
    }

    private fun loadFromApi(
        ignoreCache: Boolean
    ): String? {
        val date = Date()
        val cacheKey = CACHE_PROVIDER_PREFIX + getDateTimeString(date)

        if (!ignoreCache) {
            val location = cacheService.readString(cacheKey)

            if (location != null) {
                return location
            }
        }

        val location = loadLocationFromApi(date)
        if (location != null) {
            cacheService.saveString(cacheKey, location)
        }

        return location
    }

    private fun loadLocationFromApi(date: Date): String? {
        val url = URL("https://api.zfv.ch/graphql")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"

        try {
            // Set up the connection
            connection.doOutput = true
            connection.setRequestProperty("Accept", "*/*")
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("api-key", BuildConfig.ZFV_API_KEY)

            val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
            val isoDateString = isoFormat.format(date);
            val requestBody =
                "{\"query\":\"query Client { organisation(where: {id: \\\"cm1tjaby3002o72q4lhhak996\\\", tenantId: \\\"zfv\\\"}) { outlets(take: 100) { slug calendar { day(date: \\\"$isoDateString\\\") { menuItems { prices { amount } ... on OutletMenuItemDish { category { name path } dish { allergens { allergen { name } } name_i18n { label locale } } } } } } } }}\",\"operationName\":\"Client\"}"

            val outputStream: OutputStream = connection.outputStream
            outputStream.write(requestBody.toByteArray())
            outputStream.flush()
            outputStream.close()

            return connection.inputStream.bufferedReader().use { it.readText() }
        } catch (_: Exception) {
            // do not care, likely because of network errors. in any case, cannot recover
        } finally {
            connection.disconnect()
        }

        return null
    }

    private fun parseApiRoot(root: ApiRoot, mensas: List<UzhMensa>, language: Language): HashMap<UzhMensa, ArrayList<Menu>> {
        val result = HashMap<UzhMensa, ArrayList<Menu>>()

        for (mensa in mensas) {
            val outlet = root.data?.organisation?.outlets?.find { it.slug == mensa.slug }
            val relevantMenuItems = outlet?.calendar?.day?.menuItems?.filter {
                menuItem -> mensa.categoryPath == null || menuItem.category?.path?.contains(mensa.categoryPath) == true
            }

            if (relevantMenuItems == null) {
                continue;
            }

            val parsedMenus = ArrayList<Menu>()
            for (relevantMenu in relevantMenuItems) {
                val title = relevantMenu.category?.name

                val deDescription = relevantMenu.dish?.name_i18n?.find { it.locale == "de" }?.label
                val enDescription = relevantMenu.dish?.name_i18n?.find { it.locale == "en" }?.label
                val descriptionRaw = if (language == Language.German && deDescription != null) deDescription else enDescription
                val description = descriptionRaw?.replaceFirst(",", "\n")?.replace("\n ", "\n")

                val priceStrings = relevantMenu.prices?.mapNotNull { it.amount }?.toTypedArray<String>() ?: emptyArray<String>()
                val price = priceStrings.map { parseFloat(it) }.sorted() .map { String.format("%.2f", it) }.toTypedArray()

                val allergens = relevantMenu.dish?.allergens?.map { it.allergen?.name }?.filterNotNull()?.joinToString(separator = ", ")

                if (title == null || description == null) {
                    continue;
                }

                val menu = Menu(title, description, price, allergens)
                if (isNoMenuNotice(menu, language)) {
                    continue;
                }

                parsedMenus.add(menu)
            }

            result[mensa] = parsedMenus
        }

        return result
    }

    private fun isNoMenuNotice(menu: Menu, language: Language): Boolean {
        when (language) {
            Language.English -> {
                val invalidMenus = arrayOf("no dinner", "is closed")
                return invalidMenus.any { menu.description.contains(it) }
            }

            Language.German -> {
                val invalidMenus = arrayOf("kein Abendessen", "geschlossen")
                return invalidMenus.any { menu.description.contains(it) }
            }
        }
    }

    @Serializable
    protected class ApiRoot {
        var data: ApiData? = null
    }

    @Serializable
    protected class ApiData {
        var organisation: ApiOrganisation? = null
    }

    @Serializable
    protected class ApiOrganisation {
        var outlets: Array<Outlet>? = null
    }

    @Serializable
    protected class Outlet {
        var slug: String? = null
        var calendar: Calendar? = null
    }

    @Serializable
    protected class Calendar {
        var day: Day? = null
    }

    @Serializable
    protected class Day {
        var menuItems: Array<MenuItem>? = null
    }

    @Serializable
    protected class MenuItem {
        var prices: Array<Price>? = null
        var category: Category? = null
        var dish: Dish? = null
    }

    @Serializable
    protected class Category {
        var name: String? = null
        var path: Array<String>? = null
    }

    @Serializable
    protected class Dish {
        var name_i18n: Array<i18nValue>? = null
        var allergens: Array<AllergenContainer>? = null
    }

    @Serializable
    protected class AllergenContainer {
        var allergen: Allergen? = null
    }

    @Serializable
    protected class Allergen {
        var name: String? = null
    }

    @Serializable
    protected class Price {
        var amount: String? = null
    }

    @Serializable
    protected class i18nValue {
        var label: String? = null
        var locale: String? = null
    }

    @Serializable
    data class UzhLocation(val title: String, val mensas: List<UzhMensa>)

    @Serializable
    open class UzhMensa(
        val id: String,
        val title: String,
        val mealTime: String,
        val infoUrlSlug: String,
        val slug: String,
        val categoryPath: String? = null,
    )
}
