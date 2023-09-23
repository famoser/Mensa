package ch.famoser.mensa.services.providers

import ch.famoser.mensa.services.SerializationService
import ch.famoser.mensa.testServices.InMemoryAssetService
import ch.famoser.mensa.testServices.NoCacheService
import com.google.common.truth.Truth.assertThat
import org.jsoup.Jsoup
import org.junit.Test
import java.util.*
import kotlin.collections.HashMap

class UZHRSSMensaProviderTest {
    private fun getUzhLocationsJson(): String {
        return """
        [
            {
                "title": "Zentrum (UZH)",
                "mensas": [
                    {
                        "id": "25588906-4759-4b54-9c0e-bf7b4fd158a4",
                        "title": "Untere Mensa",
                        "mealTime": "11:00-14:30",
                        "idSlugDe": 142,
                        "idSlugEn": 142,
                        "infoUrlSlug": "mensa-uzh-zentrum"
                    }
                ] 
            }
        ]
        """
    }

    @Test
    fun locationsAndMenu_locationsAndMenuAreLoad() {
        // arrange
        val cacheService = NoCacheService()
        val inMemoryAssetService = InMemoryAssetService(mapOf("uzh/locations_rss.json" to getUzhLocationsJson()))
        val serializationService = SerializationService()
        val provider = UZHMensaProvider(cacheService, inMemoryAssetService, serializationService)

        val c = Calendar.getInstance()
        c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        val nearestMondayDate = Date.from(c.toInstant())

        // act
        val locations = provider.getLocations()
        val mensa = locations.first().mensas.first()
        val response = provider.getMenus(mensa, nearestMondayDate, AbstractMensaProvider.Language.German, true)

        // assert
        assertThat(locations).hasSize(1)
        assertThat(locations.first().mensas).hasSize(1)
        val mercatoMensa = locations.first().mensas.first()
        assertThat(response).isTrue()
        assertThat(mercatoMensa.menus).isNotEmpty()
    }

    @Test
    fun locationsAndMenu_listAllKnownLocations() {
        val results = HashMap<Int, String>()
        for (i in 400..450) {
            try {
                val url = "https://zfv.ch/de/menus/rssMenuPlan?menuId=$i&type=uzh2&dayOfWeek=2"
                val doc = Jsoup.connect(url).get()
                val titleDiv = doc.select("title").first()
                results.set(i, titleDiv.text())
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }

        // 395: ViCAFE Irchel
        assertThat(results).isNotEmpty()
    }
}