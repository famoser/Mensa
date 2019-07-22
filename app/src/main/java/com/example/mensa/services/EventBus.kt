package com.example.mensa.services

import android.content.Intent
import java.util.*

class EventBus(private val sendBroadcast: (Intent) -> Unit) {
    companion object {
        const val MENSA_MENUS_REFRESHED = "mensa.menus_refreshed"
        const val MENSA_MENUS_REFRESHED_MENSA_ID = "mensa.menus_refreshed.mensa_id"
    }

    fun publishMensaRefreshed(mensaId: UUID) {
        val intent = Intent()
        intent.action = MENSA_MENUS_REFRESHED
        intent.putExtra(MENSA_MENUS_REFRESHED_MENSA_ID, mensaId.toString())
        sendBroadcast(intent)
    }
}