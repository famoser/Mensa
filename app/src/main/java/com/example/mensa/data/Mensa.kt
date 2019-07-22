package com.example.mensa.data

import java.util.*

data class Mensa(
    val id: UUID,
    val title: String,
    val openingTimes: String,
    val isFavorite: Boolean,
    val menus: MutableList<Menu>
) {
    override fun toString(): String = title
}