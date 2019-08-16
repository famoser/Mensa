package ch.famoser.mensa.services.providers

import ch.famoser.mensa.services.IAssetService
import ch.famoser.mensa.services.ICacheService
import ch.famoser.mensa.services.ISerializationService
import com.squareup.moshi.Types
import java.lang.reflect.ParameterizedType
import java.util.*


class UZHRSSMensaProvider(
    cacheService: ICacheService,
    assetService: IAssetService,
    serializationService: ISerializationService
) : UZHMensaProvider<RSSUZHMensa>(cacheService, assetService, serializationService) {
    override fun getContentSelector(): String {
        return "summary"
    }

    override fun getLocationsJsonFileName(): String {
        return "locations_rss.json"
    }

    override fun getParametrizedUzhLocation(): ParameterizedType {
        return Types.newParameterizedType(UzhLocation::class.java, RSSUZHMensa::class.java)
    }

    override fun getUrlFor(uzhMensa: RSSUZHMensa, dayOfWeek: Int, language: String): String? {
        val dayOfWeekForApi = transformDayOfWeek(dayOfWeek) ?: return null

        var idSlug = uzhMensa.idSlugEn
        if (language === "de") {
            idSlug = uzhMensa.idSlugDe;
        }

        return "https://zfv.ch/$language/menus/rssMenuPlan?menuId=$idSlug&type=uzh2&dayOfWeek=$dayOfWeekForApi"
    }

    private fun transformDayOfWeek(dayOfWeek: Int): Int? {
        if (dayOfWeek < Calendar.MONDAY || dayOfWeek > Calendar.FRIDAY) {
            return null;
        }

        return dayOfWeek - 1;
    }
}