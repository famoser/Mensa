package com.example.mensa.data.providers

import android.content.res.AssetManager
import com.example.mensa.data.MensaProvider
import com.example.mensa.models.Location
import com.example.mensa.models.Mensa
import java.util.*
import kotlin.collections.ArrayList

class ETHMensaProvider(private val assetManager: AssetManager) : AbstractMensaProvider(assetManager) {
    override fun refresh() {
        TODO("not implemented")
    }

    override fun get(): List<Location> {
        val ethLocations = super.readJsonAssetFileToListOfT("eth_mensa.json", EthLocation::class.java);
        return ethLocations.map {
            Location(it.title, it.mensas.map {
                Mensa(UUID.fromString(it.id), it.title, it.mealTime, "https://www.ethz.ch/de/campus/gastronomie/restaurants-und-cafeterias/" + it.infoUrlSlug)
            })
        }

    }

    data class EthLocation(val title: String, val mensas: List<EthMensa>)
    data class EthMensa(val id: String, val title: String, val mealTime: String, val idSlug: Int, val timeSlug: String, val infoUrlSlug: String)

}