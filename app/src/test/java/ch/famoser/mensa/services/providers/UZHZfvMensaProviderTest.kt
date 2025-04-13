package ch.famoser.mensa.services.providers

import ch.famoser.mensa.services.SerializationService
import ch.famoser.mensa.testServices.InMemoryAssetService
import ch.famoser.mensa.testServices.NoCacheService
import com.google.common.truth.Truth.assertThat
import org.jsoup.Jsoup
import org.junit.Test
import java.util.*
import kotlin.collections.HashMap

class UZHZfvMensaProviderTest: BaseProviderTest() {
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
                    "infoUrlSlug": "zentrum-mercato",
                    "slug": "untere-mensa",
                    "categoryPath": "mittag"
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
        val inMemoryAssetService = InMemoryAssetService(mapOf("uzh/locations_zfv.json" to getUzhLocationsJson()))
        val serializationService = SerializationService()
        val provider = UZHMensaProvider3(cacheService, inMemoryAssetService, serializationService)

        // act
        val locations = provider.getLocations()
        val date = this.getNextWeekdayDate()
        val response = provider.getMenus(AbstractMensaProvider.Language.German, date, true)

        // assert
        assertThat(locations).hasSize(1)
        assertThat(locations.first().mensas).hasSize(1)
        val mercatoMensa = locations.first().mensas.first()
        assertThat(response).contains(mercatoMensa)
        assertThat(mercatoMensa.menus).isNotEmpty()
    }
}