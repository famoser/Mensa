package com.example.mensa.repositories.tasks

import android.os.AsyncTask
import com.example.mensa.events.MensaMenuUpdatedEvent
import com.example.mensa.events.RefreshMensaProgressEvent
import com.example.mensa.events.RefreshMensaFinishedEvent
import com.example.mensa.events.RefreshMensaStartedEvent
import com.example.mensa.services.providers.AbstractMensaProvider
import com.example.mensa.models.Mensa
import org.greenrobot.eventbus.EventBus
import java.time.LocalDate
import java.util.*

class RefreshMensaTask(private val mensaProvider: AbstractMensaProvider, private val date: LocalDate) :
    AsyncTask<Mensa, Int, Unit>() {

    private val asyncTaskId = UUID.randomUUID()

    override fun doInBackground(vararg mensas: Mensa) {
        for ((current, mensa) in mensas.withIndex()) {
            val menus = mensaProvider.getMenus(mensa, date);
            mensa.menus.clear()
            mensa.menus.addAll(menus)

            if (isCancelled) return
            publishProgress(mensas.size, current)

            EventBus.getDefault().post(MensaMenuUpdatedEvent(mensa.id))
        }
    }

    override fun onPreExecute() {
        super.onPreExecute()
        EventBus.getDefault().post(RefreshMensaStartedEvent(asyncTaskId))
    }

    override fun onPostExecute(result: Unit?) {
        super.onPostExecute(result)
        EventBus.getDefault().post(RefreshMensaFinishedEvent(asyncTaskId))
    }

    override fun onProgressUpdate(vararg values: Int?) {
        super.onProgressUpdate(*values)
        EventBus.getDefault().post(RefreshMensaProgressEvent(asyncTaskId, values.get(0)!!, values.get(1)!!))
    }
}
