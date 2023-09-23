package ch.famoser.mensa.repositories.tasks

import ch.famoser.mensa.events.MensasUpdatedEvent
import ch.famoser.mensa.services.providers.AbstractMensaProvider
import ch.famoser.mensa.services.providers.ETHMensaProvider2
import org.greenrobot.eventbus.EventBus
import java.util.*

class RefreshETHMensaTask(
    private val mensaProvider: ETHMensaProvider2,
    private val date: Date,
    private val language: AbstractMensaProvider.Language,
    private val ignoreCache: Boolean
) : AbstractRefreshMensaTask<String>() {

    override fun doInBackground(vararg args: String) {
        val refreshedMensas = mensaProvider.getMenus(date, language, ignoreCache)

        if (isCancelled) return

        EventBus.getDefault().post(MensasUpdatedEvent(refreshedMensas))
    }
}
