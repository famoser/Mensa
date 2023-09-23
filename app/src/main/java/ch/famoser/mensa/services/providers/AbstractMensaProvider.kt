package ch.famoser.mensa.services.providers

import android.annotation.SuppressLint
import android.util.Log
import ch.famoser.mensa.models.Location
import ch.famoser.mensa.models.Menu
import ch.famoser.mensa.services.ICacheService
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*


abstract class AbstractMensaProvider(
    private val cacheService: ICacheService
) {
    abstract fun getLocations(): List<Location>

    protected fun tryGetMenusFromCache(
        providerPrefix: String,
        mensaId: String,
        date: Date,
        language: String
    ): List<Menu>? {
        val cacheKey = getCacheKey(providerPrefix, mensaId, date, language)

        return cacheService.readMenus(cacheKey)
    }


    private fun getMensaRequestCacheKey(url: URL): String {
        return "${ETHMensaProvider2.CACHE_PROVIDER_PREFIX}.$url"
    }

    protected fun getCachedRequest(
        url: URL,
        ignoreCache: Boolean
    ): String? {
        val cacheKey = getMensaRequestCacheKey(url)

        if (!ignoreCache) {
            val json = cacheService.readString(cacheKey)
            if (json != null) {
                return json
            }
        }

        try {
            val json = url.readText()

            cacheService.saveString(cacheKey, json)

            return json
        } catch (e: java.lang.Exception) {
            Log.e("AbstractMensaProvider", "cached request failed: $url", e);
        }

        return null;
    }

    protected fun cacheMenus(providerPrefix: String, facilityId: String, date: Date, language: String, menus: List<Menu>) {
        val cacheKey = getCacheKey(providerPrefix, facilityId, date, language)

        cacheService.saveMenus(cacheKey, menus)
    }

    private fun getCacheKey(
        providerPrefix: String,
        facilityId: String,
        date: Date,
        language: String
    ): String {
        val dateSlug = getDateTimeString(date)

        return "$providerPrefix.$facilityId.$dateSlug.$language"
    }

    @SuppressLint("SimpleDateFormat")
    protected fun getDateTimeString(date: Date): String {
        val calender = Calendar.getInstance()
        calender.time = date

        val format1 = SimpleDateFormat("yyyy-MM-dd")
        return format1.format(calender.time)
    }

    @SuppressLint("SimpleDateFormat")
    protected fun getDateTimeStringOfMonday(date: Date): String {
        val calender = Calendar.getInstance()
        calender.time = date
        calender.set(Calendar.DAY_OF_WEEK, calender.getFirstDayOfWeek());

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

    protected fun languageToString(language: Language): String {
        return when (language) {
            Language.German -> "de"
            Language.English -> "en"
        }
    }

    protected fun fallbackLanguage(language: Language): Language {
        return when (language) {
            Language.German -> Language.English
            Language.English -> Language.German
        }
    }

    enum class Language {
        German,
        English
    }
}