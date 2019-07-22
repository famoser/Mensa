package com.example.mensa.data


data class Location(val title: String, val mensas: MutableList<Mensa>) {
    override fun toString(): String = title
}