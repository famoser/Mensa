package com.example.mensa.dummy

import java.util.*

/**
 * Helper class for providing sample content for user interfaces.
 */
object DummyContent {

    val ITEMS: MutableList<Menu> = ArrayList()
    val ITEM_MAP: MutableMap<UUID, Menu> = HashMap()

    init {
        addDozentenfoyer()
        addMensaPolyterrasse()
    }

    private fun addDozentenfoyer() {
        addItem(Menu(UUID.randomUUID(), "favorite", "Meatballs vom Rind mit Senfsauce, Spätzli und zweierlei Karotten mit Schnittlauch", arrayOf("11.50", "13.50", "17.00")))
        addItem(Menu(UUID.randomUUID(), "vitality", "Quornwürfeln mit Tomatensauce mit Gnocchi", arrayOf("14.50")))
        addItem(Menu(UUID.randomUUID(), "chefs world", "Saiblingsfilet mit Pesto Genovese", arrayOf("21.50")))
        addItem(Menu(UUID.randomUUID(), "hot&cold", "Tägliche warme und kalte Spezialitäten im Angebot", arrayOf("0.00")))
        addItem(Menu(UUID.randomUUID(), "soup world", "Tomaten Kalt Schale", arrayOf("7.50")))
    }

    private fun addMensaPolyterrasse() {
        addItem(Menu(UUID.randomUUID(), "LOCAL", "Dieses Menu servieren wir Ihnen gerne bald wieder!", arrayOf("10.50", "11.50", "15.50")))
        addItem(Menu(UUID.randomUUID(), "STREET", "Tuna Bowl mit Sesam, Basmatireis Mango, Spinatblätter, Ingwer, Edamame und Spicy-Teriyakisauce", arrayOf("12.50","14.50","17.00")))
        addItem(Menu(UUID.randomUUID(), "GARDEN", "Zucchetti Piccata mit Tomatensugo. Tagliatelle. Champignons mit Balsamico.", arrayOf("9.90","11.90","15.00")))
        addItem(Menu(UUID.randomUUID(), "HOME", "Metzger-Rösti mit verschiedenen Fleischsorten, und gerösteten Zwiebeln. Tagessalat.", arrayOf("6.20","9.30","12.70")))
        addItem(Menu(UUID.randomUUID(), "SOUP", "Basler Mehlsuppe mit gerösteten Zwiebelstreifen und Gruyère.", arrayOf("3.50","3.50","4.50")))
    }

    private fun addItem(item: Menu) {
        ITEMS.add(item)
        ITEM_MAP[item.id] = item
    }

    /**
     * A dummy item representing a piece of content.
     */
    data class Menu(val id: UUID, val title: String, val description: String, val price: Array<String>) {
        override fun toString(): String = title + description
    }
}
