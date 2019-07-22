package com.example.mensa.models

import java.util.*
import kotlin.collections.ArrayList

data class Mensa(
    val id: UUID,
    val title: String,
    val mealTime: String,
    val url: String
) {
    public val isFavorite: Boolean = false
    public val menus: MutableList<Menu> = ArrayList()

    override fun toString(): String = title
}