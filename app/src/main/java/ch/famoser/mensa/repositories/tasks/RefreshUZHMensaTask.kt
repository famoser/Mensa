package ch.famoser.mensa.repositories.tasks

import ch.famoser.mensa.events.MensasUpdatedEvent
import ch.famoser.mensa.models.Mensa
import ch.famoser.mensa.services.providers.AbstractMensaProvider
import ch.famoser.mensa.services.providers.UZHMensaProvider3
import org.greenrobot.eventbus.EventBus
import java.util.*

class RefreshUZHMensaTask(
    private val mensaProvider: UZHMensaProvider3,
    private val language: AbstractMensaProvider.Language,
    private val ignoreCache: Boolean
) : AbstractRefreshMensaTask<String>() {

    @Deprecated("Deprecated in Java")
    override fun doInBackground(vararg args: String) {
        val date = Date()
        val refreshedMensas = mensaProvider.getMenus(language, date, ignoreCache)

        if (isCancelled) return

        EventBus.getDefault().post(MensasUpdatedEvent(refreshedMensas))
    }
}
