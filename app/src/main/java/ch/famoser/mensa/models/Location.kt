package ch.famoser.mensa.models

import kotlinx.serialization.Serializable

@Serializable
data class Location(val title: String, val mensas: List<Mensa>) {
    init {
        for (mensa in mensas) {
            mensa.location = this
        }
    }
    override fun toString(): String = title
}