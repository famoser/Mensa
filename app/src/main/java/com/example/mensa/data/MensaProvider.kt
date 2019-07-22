package com.example.mensa.data

import com.example.mensa.models.Location

interface MensaProvider {
    fun get(): List<Location>

    fun refresh()
}