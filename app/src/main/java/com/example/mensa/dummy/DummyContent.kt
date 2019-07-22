package com.example.mensa.dummy

import android.content.res.AssetManager
import com.example.mensa.services.RefreshMensaTask
import com.example.mensa.services.providers.ETHMensaProvider
import com.example.mensa.models.Location
import com.example.mensa.models.Mensa
import com.example.mensa.models.Menu
import com.example.mensa.services.EventBus
import java.time.LocalDate
import java.util.*

/**
 * Helper class for providing sample content for user interfaces.
 */
object DummyContent {

    val MENSA_MAP: MutableMap<UUID, Mensa> = HashMap()
    val LOCATIONS: MutableList<Location> = LinkedList()

    fun initialize(assetManager: AssetManager, eventBus: EventBus) {
        val ethProvider = ETHMensaProvider(assetManager);
        val ethLocations = ethProvider.getLocations();
        for (location in ethLocations) {
            for (mensa in location.mensas) {
                MENSA_MAP[mensa.id] = mensa
                if (mensa.title.equals("Dozentenfoyer"))
                    addDozentenfoyerMenus(mensa)
                if (mensa.title.equals("Mensa Polyterrasse"))
                    addMensaPolyterrasseMenus(mensa)
            }
        }

        val today = LocalDate.now();
        RefreshMensaTask(ethProvider, today, eventBus).execute(*MENSA_MAP.values.toTypedArray());

        LOCATIONS.addAll(ethLocations)
    }

    private fun addDozentenfoyerMenus(mensa: Mensa) {
        mensa.menus.add(
            Menu(
                "favorite",
                "Meatballs vom Rind mit Senfsauce, Spätzli und zweierlei Karotten mit Schnittlauch",
                arrayOf("11.50", "13.50", "17.00")
            )
        )
        mensa.menus.add(
            Menu(
                "vitality",
                "Quornwürfeln mit Tomatensauce mit Gnocchi",
                arrayOf("14.50")
            )
        )
        mensa.menus.add(Menu("chefs world", "Saiblingsfilet mit Pesto Genovese", arrayOf("21.50")))
        mensa.menus.add(
            Menu(
                "hot&cold",
                "Tägliche warme und kalte Spezialitäten im Angebot",
                arrayOf("0.00")
            )
        )
        mensa.menus.add(Menu("soup world", "Tomaten Kalt Schale", arrayOf("7.50")))
    }

    private fun addMensaPolyterrasseMenus(mensa: Mensa) {
        mensa.menus.add(
            Menu(
                "LOCAL",
                "Dieses Menu servieren wir Ihnen gerne bald wieder!",
                arrayOf("10.50", "11.50", "15.50")
            )
        )
        mensa.menus.add(
            Menu(
                "STREET",
                "Tuna Bowl mit Sesam, Basmatireis Mango, Spinatblätter, Ingwer, Edamame und Spicy-Teriyakisauce",
                arrayOf("12.50", "14.50", "17.00")
            )
        )
        mensa.menus.add(
            Menu(
                "GARDEN",
                "Zucchetti Piccata mit Tomatensugo. Tagliatelle. Champignons mit Balsamico.",
                arrayOf("9.90", "11.90", "15.00")
            )
        )
        mensa.menus.add(
            Menu(
                "HOME",
                "Metzger-Rösti mit verschiedenen Fleischsorten, und gerösteten Zwiebeln. Tagessalat.",
                arrayOf("6.20", "9.30", "12.70")
            )
        )
        mensa.menus.add(
            Menu(
                "SOUP",
                "Basler Mehlsuppe mit gerösteten Zwiebelstreifen und Gruyère.",
                arrayOf("3.50", "3.50", "4.50")
            )
        )
    }
}
