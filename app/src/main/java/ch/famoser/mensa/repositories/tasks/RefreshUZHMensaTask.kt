package ch.famoser.mensa.repositories.tasks

import ch.famoser.mensa.events.MensaUpdatedEvent
import ch.famoser.mensa.models.Mensa
import ch.famoser.mensa.services.providers.AbstractMensaProvider
import ch.famoser.mensa.services.providers.UZHMensaProvider
import ch.famoser.mensa.services.providers.UzhMensa
import org.greenrobot.eventbus.EventBus
import java.util.*

class RefreshUZHMensaTask<T : UzhMensa>(
    private val htmlMensaProvider: UZHMensaProvider<T>,
    private val date: Date,
    private val language: AbstractMensaProvider.Language,
    private val ignoreCache: Boolean
) : AbstractRefreshMensaTask<Mensa>() {

    override fun doInBackground(vararg mensas: Mensa) {
        for ((current, mensa) in mensas.withIndex()) {
            val refreshSuccessful = htmlMensaProvider.getMenus(mensa, date, language, ignoreCache)

            if (isCancelled) return
            publishProgress(mensas.size, current)

            if (refreshSuccessful) {
                EventBus.getDefault().post(MensaUpdatedEvent(mensa))
            }
        }
    }
}
