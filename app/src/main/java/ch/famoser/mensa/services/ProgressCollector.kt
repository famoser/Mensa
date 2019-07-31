package ch.famoser.mensa.services

import android.view.View
import android.widget.ProgressBar
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import ch.famoser.mensa.events.RefreshMensaFinishedEvent
import ch.famoser.mensa.events.RefreshMensaProgressEvent
import ch.famoser.mensa.events.RefreshMensaStartedEvent
import java.util.*
import kotlin.collections.HashMap

class ProgressCollector(private val swipeRefreshLayout: SwipeRefreshLayout, private val progressBar: ProgressBar) {

    private var taskActive: MutableMap<UUID, Boolean> = HashMap()
    private var progressByTask: MutableMap<UUID, Int> = HashMap()
    private var maxByTask: HashMap<UUID, Int> = HashMap()

    fun onStarted(event: RefreshMensaStartedEvent) {
        if (!taskActive.any()) {
            showIndeterminateProgress()
        }

        taskActive[event.taskId] = true
    }

    fun onProgress(event: RefreshMensaProgressEvent) {
        progressByTask[event.taskId] = event.progress
        maxByTask[event.taskId] = event.max

        showDeterminateProgress()
    }

    fun onFinished(event: RefreshMensaFinishedEvent) {
        taskActive[event.taskId] = false
        progressByTask[event.taskId] = maxByTask[event.taskId]!!

        if (taskActive.values.none { it }) {
            taskActive = HashMap()
            progressByTask = HashMap()
            maxByTask = HashMap()

            hideProgress()
        } else {
            showDeterminateProgress()
        }
    }

    private fun showIndeterminateProgress() {
        progressBar.visibility = View.VISIBLE
        progressBar.isIndeterminate = true
    }

    private fun showDeterminateProgress() {
        progressBar.visibility = View.VISIBLE
        progressBar.isIndeterminate = false

        progressBar.max = maxByTask.values.sum()
        progressBar.progress = progressByTask.values.sum()
    }

    private fun hideProgress() {
        swipeRefreshLayout.isRefreshing = false
        progressBar.visibility = View.GONE
    }
}