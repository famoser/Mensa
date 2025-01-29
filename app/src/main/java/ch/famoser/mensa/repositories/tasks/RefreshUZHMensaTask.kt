package ch.famoser.mensa.repositories.tasks

import ch.famoser.mensa.events.MensaUpdatedEvent
import ch.famoser.mensa.events.MensasUpdatedEvent
import ch.famoser.mensa.models.Mensa
import ch.famoser.mensa.services.providers.AbstractMensaProvider
import ch.famoser.mensa.services.providers.UZHMensaProvider2
import org.greenrobot.eventbus.EventBus
import java.util.*

class RefreshUZHMensaTask(
    private val mensaProvider: UZHMensaProvider2,
    private val language: AbstractMensaProvider.Language,
    private val ignoreCache: Boolean
) : AbstractRefreshMensaTask<String>() {

    @Deprecated("Deprecated in Java")
    override fun doInBackground(vararg args: String) {
        val mensaGroups = mensaProvider.getMensaGroups()
        for ((current, mensaGroup) in mensaGroups.withIndex()) {
            val refreshedMensas = mensaProvider.getMenus(mensaGroup, language, ignoreCache)

            if (isCancelled) return
            publishProgress(mensaGroups.size, current)

            EventBus.getDefault().post(MensasUpdatedEvent(refreshedMensas))
        }
    }
}
