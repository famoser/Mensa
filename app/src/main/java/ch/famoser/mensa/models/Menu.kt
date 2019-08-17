package ch.famoser.mensa.models

class Menu(val title: String, val description: String, val price: Array<String>, val allergens: String?) {
    override fun toString(): String = title + description

    @Transient
    var mensa: Mensa? = null
}
