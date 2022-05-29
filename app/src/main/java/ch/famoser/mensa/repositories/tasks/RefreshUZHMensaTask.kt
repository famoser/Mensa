package ch.famoser.mensa.repositories.tasks

import ch.famoser.mensa.events.MensaUpdatedEvent
import ch.famoser.mensa.models.Mensa
import ch.famoser.mensa.services.providers.AbstractMensaProvider
import ch.famoser.mensa.services.providers.UZHMensaProvider
import org.greenrobot.eventbus.EventBus
import java.util.*

class RefreshUZHMensaTask(
    private val htmlMensaProvider: UZHMensaProvider,
    private val date: Date,
    private val language: AbstractMensaProvider.Language,
    private val ignoreCache: Boolean
) : AbstractRefreshMensaTask<Mensa>() {

    override fun doInBackground(vararg mensas: Mensa) {
        for ((current, mensa) in mensas.withIndex()) {
            htmlMensaProvider.getMenus(mensa, date, language, ignoreCache)

            if (isCancelled) return
            publishProgress(mensas.size, current)

            EventBus.getDefault().post(MensaUpdatedEvent(mensa))
        }
    }
}
