package ch.famoser.mensa.services.providers

import android.annotation.SuppressLint
import android.content.res.AssetManager
import ch.famoser.mensa.models.Location
import ch.famoser.mensa.models.Mensa
import ch.famoser.mensa.models.Menu
import ch.famoser.mensa.services.CacheService
import ch.famoser.mensa.services.SerializationService
import java.io.IOException
import java.nio.charset.Charset
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


abstract class AbstractMensaProvider(
    private val cacheService: CacheService,
    private val assetManager: AssetManager,
    private val serializationService: SerializationService
) {
    abstract fun getLocations(): List<Location>

    protected fun <T> readJsonAssetFileToListOfT(rawFileName: String, classOfT: Class<T>): List<T> {
        val json: String = readStringAssetFile(rawFileName) ?: return ArrayList()

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
        val dateSlug = getDateTimeString(date);
        val cacheKey = "$providerPrefix.$mensaId.$dateSlug.$language"
        return cacheKey
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

        return normalized;
    }

    private fun readStringAssetFile(rawFileName: String): String? {
        var json: String? = null
        try {
            val inputStream = assetManager.open(rawFileName)
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            json = String(buffer, Charset.forName("UTF-8"))
        } catch (ex: IOException) {
            ex.printStackTrace()
        }

        return json;
    }
}