package com.example.mensa.models

data class Menu(val title: String, val description: String, val price: Array<String>) {
    override fun toString(): String = title + description

    var mensa: Mensa? = null
}
