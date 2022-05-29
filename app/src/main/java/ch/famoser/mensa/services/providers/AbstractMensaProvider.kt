package ch.famoser.mensa.services.providers

import android.annotation.SuppressLint
import ch.famoser.mensa.models.Location
import ch.famoser.mensa.models.Menu
import ch.famoser.mensa.services.ICacheService
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