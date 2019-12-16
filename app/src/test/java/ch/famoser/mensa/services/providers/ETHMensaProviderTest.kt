package ch.famoser.mensa.services.providers

import ch.famoser.mensa.services.SerializationService
import ch.famoser.mensa.testServices.InMemoryAssetService
import ch.famoser.mensa.testServices.NoCacheService
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.util.*

class ETHMensaProviderTest {
    private fun getEthLocationsJson(): String {
        return """
        [
            {
                "title": "Zentrum",
                "mensas": [
                  {
                    "id": "58c14697-c998-42f1-b6ac-2c0bc782af6d",
                    "title": "Mensa Polyterrasse - Abendessen",
                    "mealTime": "17:30-19:30",
                    "idSlug": 12,
                    "timeSlug": "dinner",
                    "infoUrlSlug": "zentrum/mensa-polyterrasse"
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
        val inMemoryAssetService = InMemoryAssetService(mapOf("eth/locations.json" to getEthLocationsJson()))
        val serializationService = SerializationService()
        val provider = ETHMensaProvider(cacheService, inMemoryAssetService, serializationService)

        val c = Calendar.getInstance()
        c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        val nearestMondayDate = Date.from(c.toInstant())

        // act
        val locations = provider.getLocations()
        val response = provider.getMenus(ETHMensaProvider.MEAL_TIME_DINNER, nearestMondayDate, AbstractMensaProvider.Language.German, true)

        // assert
        assertThat(locations).hasSize(1)
        assertThat(locations.first().mensas).hasSize(1)
        val polymensa = locations.first().mensas.first()
        assertThat(response).contains(polymensa)
        assertThat(polymensa.menus).isNotEmpty()
    }
}