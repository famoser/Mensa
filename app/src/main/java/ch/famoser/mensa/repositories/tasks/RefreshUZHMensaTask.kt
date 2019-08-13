package ch.famoser.mensa.repositories.tasks

import ch.famoser.mensa.events.MensaMenuUpdatedEvent
import ch.famoser.mensa.models.Mensa
import ch.famoser.mensa.services.providers.UZHHtmlMensaProvider
import org.greenrobot.eventbus.EventBus
import java.util.*

class RefreshUZHMensaTask(
    private val htmlMensaProvider: UZHHtmlMensaProvider,
    private val date: Date,
    private val language: String,
    private val ignoreCache: Boolean
) : AbstractRefreshMensaTask<Mensa>() {

    override fun doInBackground(vararg mensas: Mensa) {
        for ((current, mensa) in mensas.withIndex()) {
            val refreshSuccessful = htmlMensaProvider.getMenus(mensa, date, language, ignoreCache)

            if (isCancelled) return
            publishProgress(mensas.size, current)

            if (refreshSuccessful) {
                EventBus.getDefault().post(MensaMenuUpdatedEvent(mensa.id))
            }
        }
    }
}
