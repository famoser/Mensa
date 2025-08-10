package ch.famoser.mensa.services.providers

import ch.famoser.mensa.services.SerializationService
import ch.famoser.mensa.testServices.InMemoryAssetService
import ch.famoser.mensa.testServices.NoCacheService
import com.google.common.truth.Truth.assertThat
import org.jsoup.Jsoup
import org.junit.Ignore
import org.junit.Test
import java.util.*
import kotlin.collections.HashMap

class UZHFood2025MensaProviderTest: BaseProviderTest() {
    private fun getUzhLocationsJson(): String {
        return """
        [
            {
                "title": "Zentrum (UZH)",
                "mensas": [
                  {
                    "id": "4bd02416-f190-4578-be30-b407fe8711cb",
                    "title": "Lichthof Zentrum",
                    "mealTime": "11:00-14:00",
                    "infoUrlSlug": "lichthof-rondell",
                    "locationId": "e321519e-3f83-4a10-b6d8-22d395ebfc5d",
                    "slug": "lichthof"
                  }
                ]
            }
        ]
        """
    }

    @Test
    @Ignore("In use is the ZFV mensa provider at the moment.")
    fun locationsAndMenu_locationsAndMenuAreLoad() {
        // on saturday / sunday no menus; cannot use other date than today with this provider.
        if (!this.isWeekday()) {
            return
        }

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