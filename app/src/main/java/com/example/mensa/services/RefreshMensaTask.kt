package com.example.mensa.services

import android.os.AsyncTask
import com.example.mensa.events.MensaMenuUpdatedEvent
import com.example.mensa.events.RefreshMensaFinishedEvent
import com.example.mensa.services.providers.AbstractMensaProvider
import com.example.mensa.models.Mensa
import org.greenrobot.eventbus.EventBus
import java.time.LocalDate

class RefreshMensaTask(private val mensaProvider: AbstractMensaProvider, private val date: LocalDate) :
    AsyncTask<Mensa, Int, Unit>() {

    override fun doInBackground(vararg mensas: Mensa) {
        for ((current, mensa) in mensas.withIndex()) {
            val menus = mensaProvider.getMenus(mensa, date);
            mensa.menus.clear()
            mensa.menus.addAll(menus)

            if (isCancelled) return
            publishProgress((current / mensas.size * 100))

            EventBus.getDefault().post(MensaMenuUpdatedEvent(mensa.id))
        }
    }

    override fun onPostExecute(result: Unit?) {
        EventBus.getDefault().post(RefreshMensaFinishedEvent())
    }
}
