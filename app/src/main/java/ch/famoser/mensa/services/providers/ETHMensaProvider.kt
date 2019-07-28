package ch.famoser.mensa.services.providers

import android.annotation.SuppressLint
import android.content.res.AssetManager
import android.net.Uri
import android.util.SparseArray
import ch.famoser.mensa.models.Location
import ch.famoser.mensa.models.Mensa
import java.net.URL
import java.util.*
import kotlin.collections.HashMap
import ch.famoser.mensa.models.Menu
import java.text.SimpleDateFormat
import kotlin.collections.ArrayList


class ETHMensaProvider(assetManager: AssetManager) : AbstractMensaProvider(assetManager) {
    private val mensaMap: MutableMap<Mensa, EthMensa> = HashMap()

    fun getMenus(source: String, date: Date, language: String)
            : List<Mensa> {
        try {
            val dateSlug = getDateTimeString(date);
            val json = URL("https://www.webservices.ethz.ch/gastro/v1/RVRI/Q1E1/meals/$language/$dateSlug/$source")
                .readText()

            val apiMensas = jsonToListOfT(json, ApiMensa::class.java);

            val mensaLookup = SparseArray<Mensa>()
            mensaMap.map { mensa -> mensaLookup.put(mensa.value.idSlug, mensa.key) }

            return replaceMenus(apiMensas, mensaLookup)
        } catch (ex: Exception) {
            ex.printStackTrace()

            return ArrayList()
        }
    }

    private fun replaceMenus(
        apiMensas: List<ApiMensa>,
        mensaLookup: SparseArray<Mensa>
    ): List<Mensa> {
        val refreshedMensas = ArrayList<Mensa>()

        for (apiMensa in apiMensas) {
            val mensa = mensaLookup[apiMensa.id]
            if (mensa === null) {
                continue
            }

            val menus = apiMensa.menu.meals.map { apiMeal ->
                Menu(
                    apiMeal.label,
                    normalizeText(apiMeal.description.joinToString(separator = "\n")),
                    apiMeal.prices.run { arrayOf<String?>(student, staff, extern) }.filterNotNull().toTypedArray(),
                    apiMeal.allergens
                        .fold(ArrayList<String>(), { acc, apiAllergen -> acc.add(apiAllergen.label); acc })
                        .joinToString(separator = ", ")
                )
            }

            mensa.replaceMenus(menus)
            refreshedMensas.add(mensa)
        }

        return refreshedMensas
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

    @SuppressLint("SimpleDateFormat")
    private fun getDateTimeString(date: Date): String {
        val calender = Calendar.getInstance()
        calender.time = date

        val format1 = SimpleDateFormat("yyyy-MM-dd")
        return format1.format(calender.time)
    }

    data class ApiMensa(val id: Int, val mensa: String, val daytime: String, val hours: ApiHours, val menu: ApiMenu)
    data class ApiHours(val opening: List<ApiOpening>, val mealtime: List<ApiMealtime>)
    data class ApiOpening(val from: String, val to: String, val type: String)
    data class ApiMealtime(val from: String, val to: String, val type: String)
    data class ApiMenu(val date: String, val day: String, val meals: List<ApiMeal>)
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