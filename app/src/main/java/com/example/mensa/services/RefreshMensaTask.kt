package com.example.mensa.services

import android.content.Intent
import android.os.AsyncTask
import com.example.mensa.services.providers.AbstractMensaProvider
import com.example.mensa.models.Mensa
import java.time.LocalDate

class RefreshMensaTask(private val mensaProvider: AbstractMensaProvider, private val date: LocalDate, private val eventBus: EventBus) :
    AsyncTask<Mensa, Int, Unit>() {

    // Do the long-running work in here
    override fun doInBackground(vararg mensas: Mensa) {
        for ((current, mensa) in mensas.withIndex()) {
            val menus = mensaProvider.getMenus(mensa, date);
            mensa.menus.addAll(menus)

            if (isCancelled) return
            publishProgress((current / mensas.size * 100))

            eventBus.publishMensaRefreshed(mensa.id)
        }
    }
}
