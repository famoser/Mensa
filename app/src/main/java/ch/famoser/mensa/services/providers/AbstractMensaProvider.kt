package ch.famoser.mensa.services.providers

import android.annotation.SuppressLint
import ch.famoser.mensa.models.Location
import ch.famoser.mensa.models.Menu
import ch.famoser.mensa.services.IAssetService
import ch.famoser.mensa.services.ICacheService
import ch.famoser.mensa.services.ISerializationService
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


abstract class AbstractMensaProvider(
    private val cacheService: ICacheService,
    private val assetService: IAssetService,
    private val serializationService: ISerializationService
) {
    abstract fun getLocations(): List<Location>

    protected fun <T> readJsonAssetFileToListOfT(rawFileName: String, classOfT: Class<T>): List<T> {
        val json: String = assetService.readStringFile(rawFileName) ?: return ArrayList()

        return serializationService.deserializeList(json, classOfT)
    }

    protected fun tryGetMenusFromCache(providerPrefix: String, mensaId: String, date: Date, language: String): List<Menu>? {
        val cacheKey = getCacheKey(providerPrefix, mensaId, date, language)

        return cacheService.readMenus(cacheKey)
    }

    protected fun cacheMenus(providerPrefix: String, mensaId: String, date: Date, language: String, menus: List<Menu>) {
        val cacheKey = getCacheKey(providerPrefix, mensaId, date, language)

        cacheService.saveMenus(cacheKey, menus)
    }

    private fun getCacheKey(
        providerPrefix: String,
        mensaId: String,
        date: Date,
        language: String
    ): String {
        val dateSlug = getDateTimeString(date)

        return "$providerPrefix.$mensaId.$dateSlug.$language"
    }

    @SuppressLint("SimpleDateFormat")
    protected fun getDateTimeString(date: Date): String {
        val calender = Calendar.getInstance()
        calender.time = date

        val format1 = SimpleDateFormat("yyyy-MM-dd")
        return format1.format(calender.time)
    }

    protected fun normalizeText(text: String): String {
        // remove too much whitespace
        var normalized = text.replace("  ", " ")
        normalized = normalized.replace(" \n", "\n")
        normalized = normalized.replace("\n ", "\n")

        return normalized
    }
}