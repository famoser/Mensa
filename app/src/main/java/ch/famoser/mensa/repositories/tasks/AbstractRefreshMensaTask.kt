package ch.famoser.mensa.repositories.tasks

import android.os.AsyncTask
import ch.famoser.mensa.events.RefreshMensaFinishedEvent
import ch.famoser.mensa.events.RefreshMensaProgressEvent
import ch.famoser.mensa.events.RefreshMensaStartedEvent
import org.greenrobot.eventbus.EventBus
import java.util.*

abstract class AbstractRefreshMensaTask<TArgument> : AsyncTask<TArgument, Int, Unit>() {

    private val asyncTaskId = UUID.randomUUID()

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
        EventBus.getDefault().post(RefreshMensaProgressEvent(asyncTaskId, values[0]!!, values[1]!!))
    }
}
