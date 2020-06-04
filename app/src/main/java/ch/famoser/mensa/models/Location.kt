package ch.famoser.mensa.models

import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
data class Location(val title: String, val mensas: List<Mensa>) {
    init {
        for (mensa in mensas) {
            mensa.location = this
        }
    }
    override fun toString(): String = title
}