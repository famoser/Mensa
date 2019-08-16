package ch.famoser.mensa.services.providers

data class UzhLocation<T: UzhMensa>(val title: String, val mensas: List<T>)
open class UzhMensa(
    val id: String,
    val title: String,
    val mealTime: String,
    val infoUrlSlug: String
)

class RSSUZHMensa(id: String, title: String, mealTime: String, val idSlugDe: Int, val idSlugEn: Int, infoUrlSlug: String) :
    UzhMensa(id, title, mealTime, infoUrlSlug)

class HtmlUZHMensa(id: String, title: String, mealTime: String, val apiUrlSlug: String, infoUrlSlug: String) :
    UzhMensa(id, title, mealTime, infoUrlSlug)