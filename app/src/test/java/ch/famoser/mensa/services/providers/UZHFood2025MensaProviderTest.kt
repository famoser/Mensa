package ch.famoser.mensa.services.providers

import ch.famoser.mensa.services.SerializationService
import ch.famoser.mensa.testServices.InMemoryAssetService
import ch.famoser.mensa.testServices.NoCacheService
import com.google.common.truth.Truth.assertThat
import org.jsoup.Jsoup
import org.junit.Test
import java.util.*
import kotlin.collections.HashMap

class UZHFood2025MensaProviderTest {
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
                    "locationId": "e321519e-3f83-4a10-b6d8-22d395ebfc5d",
                    "slug": "untere-mensa",
                    "recipeSelector": "mittag"
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
        val inMemoryAssetService = InMemoryAssetService(mapOf("uzh/locations_food2025.json" to getUzhLocationsJson()))
        val serializationService = SerializationService()
        val provider = UZHMensaProvider2(cacheService, inMemoryAssetService, serializationService)

        // act
        val locations = provider.getLocations()
        val mensaGroup = provider.getMensaGroups().first()
        val response = provider.getMenus(mensaGroup, AbstractMensaProvider.Language.German, true)

        // assert
        assertThat(locations).hasSize(1)
        assertThat(locations.first().mensas).hasSize(1)
        val mercatoMensa = locations.first().mensas.first()
        assertThat(response).contains(mercatoMensa)
        assertThat(mercatoMensa.menus).isNotEmpty()
    }
}