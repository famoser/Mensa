package com.example.mensa.dummy

import java.util.*
import kotlin.collections.ArrayList

/**
 * Helper class for providing sample content for user interfaces.
 */
object DummyContent {
    
    val MENSA_MAP: MutableMap<UUID, Mensa> = HashMap()

    init {
        addDozentenfoyer()
        addMensaPolyterrasse()
    }

    private fun addDozentenfoyer() {
        val menus: MutableList<Menu> = ArrayList();
        menus.add(Menu( "favorite", "Meatballs vom Rind mit Senfsauce, Spätzli und zweierlei Karotten mit Schnittlauch", arrayOf("11.50", "13.50", "17.00")))
        menus.add(Menu( "vitality", "Quornwürfeln mit Tomatensauce mit Gnocchi", arrayOf("14.50")))
        menus.add(Menu( "chefs world", "Saiblingsfilet mit Pesto Genovese", arrayOf("21.50")))
        menus.add(Menu( "hot&cold", "Tägliche warme und kalte Spezialitäten im Angebot", arrayOf("0.00")))
        menus.add(Menu( "soup world", "Tomaten Kalt Schale", arrayOf("7.50")))

        val mensa = Mensa(UUID.randomUUID(), "Dozentenfoyer", "11:30 - 13:30", false, menus)
        MENSA_MAP[mensa.id] = mensa;
    }

    private fun addMensaPolyterrasse() {
        val menus: MutableList<Menu> = ArrayList();
        menus.add(Menu( "LOCAL", "Dieses Menu servieren wir Ihnen gerne bald wieder!", arrayOf("10.50", "11.50", "15.50")))
        menus.add(Menu( "STREET", "Tuna Bowl mit Sesam, Basmatireis Mango, Spinatblätter, Ingwer, Edamame und Spicy-Teriyakisauce", arrayOf("12.50","14.50","17.00")))
        menus.add(Menu( "GARDEN", "Zucchetti Piccata mit Tomatensugo. Tagliatelle. Champignons mit Balsamico.", arrayOf("9.90","11.90","15.00")))
        menus.add(Menu( "HOME", "Metzger-Rösti mit verschiedenen Fleischsorten, und gerösteten Zwiebeln. Tagessalat.", arrayOf("6.20","9.30","12.70")))
        menus.add(Menu( "SOUP", "Basler Mehlsuppe mit gerösteten Zwiebelstreifen und Gruyère.", arrayOf("3.50","3.50","4.50")))

        val mensa = Mensa(UUID.randomUUID(), "Polyterrasse", "11:30 - 13:30", true, menus)
        MENSA_MAP[mensa.id] = mensa;
    }

    data class Menu(val title: String, val description: String, val price: Array<String>) {
        override fun toString(): String = title + description
    }

    data class Mensa(val id: UUID, val title: String, val openingTimes: String, val isFavorite: Boolean, val menus: MutableList<Menu>) {
        override fun toString(): String = title
    }
}
