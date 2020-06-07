package ch.famoser.mensa.services.providers

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UzhLocation<T: UzhMensa>(val title: String, val mensas: List<T>)

@JsonClass(generateAdapter = true)
open class UzhMensa(
    val id: String,
    val title: String,
    val mealTime: String,
    val infoUrlSlug: String
)

@JsonClass(generateAdapter = true)
class RSSUZHMensa(id: String, title: String, mealTime: String, val idSlugDe: Int, val idSlugEn: Int, infoUrlSlug: String) :
    UzhMensa(id, title, mealTime, infoUrlSlug)