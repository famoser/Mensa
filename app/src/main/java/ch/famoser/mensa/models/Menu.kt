package ch.famoser.mensa.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class Menu(val title: String, val description: String, val price: Array<String>, val allergens: String?) {
    override fun toString(): String = title + description

    @Transient
    var mensa: Mensa? = null

    public fun isSomeEmpty(): Boolean {
        return this.title.isEmpty() || this.description.isEmpty();
    }

    public fun mergeWithFallback(other: Menu): Menu {
        val title = if (this.title.isNotEmpty()) this.title else other.title;
        val description = if (this.description.isNotEmpty()) this.description else other.description;
        val price = if (this.price.isNotEmpty()) this.price else other.price;
        val allergens = if (this.allergens !== null && this.allergens.isNotEmpty()) this.allergens else other.allergens;

        return Menu(title, description, price, allergens);
    }
}
