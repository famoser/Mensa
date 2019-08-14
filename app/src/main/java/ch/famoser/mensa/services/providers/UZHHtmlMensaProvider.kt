package ch.famoser.mensa.services.providers

import ch.famoser.mensa.services.IAssetService
import ch.famoser.mensa.services.ICacheService
import ch.famoser.mensa.services.ISerializationService
import com.squareup.moshi.Types
import java.lang.reflect.ParameterizedType
import java.util.*


class UZHHtmlMensaProvider(
    cacheService: ICacheService,
    assetService: IAssetService,
    serializationService: ISerializationService
) : UZHMensaProvider<HtmlUZHMensa>(cacheService, assetService, serializationService) {
    override fun getContentSelector(): String {
        return "#main .mod-newslist .newslist-description"
    }

    override fun getLocationsJsonFileName(): String {
        return "locations.json"
    }

    override fun getParametrizedUzhLocation(): ParameterizedType {
        return Types.newParameterizedType(UzhLocation::class.java, HtmlUZHMensa::class.java)
    }

    override fun getUrlFor(uzhMensa: HtmlUZHMensa, dayOfWeek: Int, language: String): String? {
        val dayOfWeekForApi = dayOfWeekToString(dayOfWeek) ?: return null
        return "https://www.mensa.uzh.ch/$language/menueplaene/${uzhMensa.apiUrlSlug}/$dayOfWeekForApi.html"
    }

    private fun dayOfWeekToString(dayOfWeek: Int): String? {
        return when (dayOfWeek) {
            Calendar.MONDAY -> "montag"
            Calendar.TUESDAY -> "dienstag"
            Calendar.WEDNESDAY -> "mittwoch"
            Calendar.THURSDAY -> "donnerstag"
            Calendar.FRIDAY -> "freitag"
            else -> null
        }
    }
}