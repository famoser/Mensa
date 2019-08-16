package ch.famoser.mensa.repositories.tasks

import ch.famoser.mensa.events.MensaMenuUpdatedEvent
import ch.famoser.mensa.services.providers.AbstractMensaProvider
import ch.famoser.mensa.services.providers.ETHMensaProvider
import org.greenrobot.eventbus.EventBus
import java.util.*

class RefreshETHMensaTask(
    private val mensaProvider: ETHMensaProvider,
    private val date: Date,
    private val language: AbstractMensaProvider.Language,
    private val ignoreCache: Boolean
) : AbstractRefreshMensaTask<String>() {

    override fun doInBackground(vararg times: String) {
        for ((index, source) in times.withIndex()) {
            val refreshedMensas = mensaProvider.getMenus(source, date, language, ignoreCache)

            if (isCancelled) return
            publishProgress(times.size, index)

            for (mensa in refreshedMensas) {
                EventBus.getDefault().post(MensaMenuUpdatedEvent(mensa.id))
            }
        }
    }
}
