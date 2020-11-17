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
                    "id": "e01cded9-53aa-47c0-9ea5-3b5c85909e23",
                    "title": "FUSION coffee",
                    "mealTime": "17:30-19:00",
                    "idSlug": 22,
                    "timeSlug": "lunch",
                    "infoUrlSlug": "hoenggerberg/fusion-coffee"
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
        val dayOfWeek = c.get(Calendar.DAY_OF_WEEK)
        if (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) {
            c.add(Calendar.DAY_OF_WEEK, 2);
        }

        val nearestValidDate = Date.from(c.toInstant())

        // act
        val locations = provider.getLocations()
        val response = provider.getMenus(ETHMensaProvider.MEAL_TIME_LUNCH, nearestValidDate, AbstractMensaProvider.Language.German, true)

        // assert
        val polymensa = locations.first().mensas.first()
        assertThat(response).contains(polymensa)
        assertThat(locations).hasSize(1)
        assertThat(locations.first().mensas).hasSize(1)
        assertThat(locations.first().mensas.filter { it.menus.isNotEmpty() }).isNotEmpty()
    }
}