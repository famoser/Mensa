package ch.famoser.mensa.activities

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.text.method.LinkMovementMethod
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import ch.famoser.mensa.R
import ch.famoser.mensa.adapters.LocationAdapter
import ch.famoser.mensa.adapters.MensaAdapter
import ch.famoser.mensa.events.*
import ch.famoser.mensa.models.Location
import ch.famoser.mensa.repositories.LocationRepository
import ch.famoser.mensa.services.ProgressCollector
import ch.famoser.mensa.services.providers.AbstractMensaProvider
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.frame_location_list.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*


/**
 * On phones, the activity presents a list of items, which when touched,
 * lead to a [MensaActivity] representing mensa details.
 *
 * On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
class MainActivity : AppCompatActivity() {

    companion object {
        var locationListScrollState: Parcelable? = null
        private const val InvertLanguageSetting = "InvertLanguage"

        fun saveInvertLanguage(context: Activity, value: Boolean) {
            val sharedPreferences = context.getPreferences(Context.MODE_PRIVATE) ?: return
            sharedPreferences
                .edit()
                .putBoolean(InvertLanguageSetting, value)
                .apply()
        }

        fun invertLanguage(context: Activity): Boolean {
            val sharedPreferences = context.getPreferences(Context.MODE_PRIVATE) ?: return false
            return sharedPreferences.getBoolean(InvertLanguageSetting, false)
        }
    }

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private var twoPane: Boolean = false

    private lateinit var refreshMensaEventProcessor: ProgressCollector
    private lateinit var locationListAdapter: LocationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.AppTheme)
        setContentView(R.layout.activity_main)

        if (details_container != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            twoPane = true
        }

        source.movementMethod = LinkMovementMethod.getInstance()

        this.refreshMensaEventProcessor = ProgressCollector(swipeContainer, downloadProgress)

        if (locationListScrollState != null) {
            location_list_scroll_viewer.onRestoreInstanceState(locationListScrollState)
        }

        EventBus.getDefault().register(this)

        swipeContainer.setOnRefreshListener { forceRefresh() }

        settings.setOnClickListener {
            val popup = PopupMenu(this, it)
            popup.menuInflater.inflate(R.menu.settings, popup.menu)

            val showOnlyExpandedMensaItem = popup.menu.findItem(R.id.show_only_expanded)
            val showInOtherLanguageItem = popup.menu.findItem(R.id.show_in_other_language)

            // change value on click
            popup.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.show_only_expanded -> {
                        showOnlyExpandedMensaItem.isChecked = !showOnlyExpandedMensaItem.isChecked
                        toggleShowAllMensas()
                    }
                    R.id.show_in_other_language -> {
                        showInOtherLanguageItem.isChecked = !showInOtherLanguageItem.isChecked
                        toggleShowInOtherLanguage()
                    }
                    else -> return@setOnMenuItemClickListener false
                }

                true
            }

            // set initial values
            showInOtherLanguageItem.isChecked = invertLanguage(this)
            showOnlyExpandedMensaItem.isChecked = MensaAdapter.showOnlyFavoriteMensas(this)

            popup.show()
        }
    }

    private fun forceRefresh() {
        val locationRepository = LocationRepository.getInstance(this.applicationContext)
        val date = Date(System.currentTimeMillis())
        locationRepository.refresh(date, getLanguage(), true)
    }

    private fun initializeLocationList(locations: MutableList<Location>, initializeFully: Boolean) {
        val locationAdapter = LocationAdapter(this, twoPane, locations, initializeFully)
        location_list.adapter = locationAdapter
        this.locationListAdapter = locationAdapter

        val noFavorites = findViewById<View>(R.id.no_favorites)
        noFavorites.visibility = if (locationAdapter.itemCount == 0) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    private fun getLanguage(): AbstractMensaProvider.Language {
        val systemLanguage = Locale.getDefault().language
        var language = if (systemLanguage === "de") {
            AbstractMensaProvider.Language.German
        } else {
            AbstractMensaProvider.Language.English
        }

        if (invertLanguage(this)) {
            language = if (language == AbstractMensaProvider.Language.English) {
                AbstractMensaProvider.Language.German
            } else {
                AbstractMensaProvider.Language.English
            }
        }

        return language
    }

    private fun toggleShowInOtherLanguage() {
        val invertLanguage = invertLanguage(this)
        val newValue = !invertLanguage
        saveInvertLanguage(this, newValue)

        forceRefresh()
    }

    private fun toggleShowAllMensas() {
        val currentValue = MensaAdapter.showOnlyFavoriteMensas(this)
        MensaAdapter.saveOnlyFavoriteMensasSetting(this, !currentValue)

        this.locationListAdapter.reset()
        this.locationListAdapter.notifyDataSetChanged()
    }

    public override fun onResume() {
        val locationRepository = LocationRepository.getInstance(this)
        val isRefreshPending = locationRepository.isRefreshPending()
        initializeLocationList(locationRepository.getLocations(), !isRefreshPending)

        if (isRefreshPending) {
            window.decorView.post {
                locationRepository.refresh(Date(System.currentTimeMillis()), getLanguage())
            }
        }

        super.onResume()
    }

    public override fun onPause() {
        // Save ListView locationListScrollState @ onPause
        locationListScrollState = location_list_scroll_viewer.onSaveInstanceState()
        super.onPause()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMensaUpdatedEvent(event: MensaUpdatedEvent) {
        locationListAdapter.mensaUpdated(event.mensa)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMensasUpdatedEvent(event: MensasUpdatedEvent) {
        locationListAdapter.mensasUpdated(event.mensas)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRefreshMensaStartedEvent(event: RefreshMensaStartedEvent) {
        refreshMensaEventProcessor.onStarted(event)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRefreshMensaProgressEvent(event: RefreshMensaProgressEvent) {
        refreshMensaEventProcessor.onProgress(event)
    }

    @Subscribe(threadMode = ThreadMode.MAIN, priority = -1)
    fun onRefreshMensaFinishedEvent(event: RefreshMensaFinishedEvent) {
        refreshMensaEventProcessor.onFinished(event)

        // if progress hidden then forceRefresh finished
        val locationRepository = LocationRepository.getInstance(this)
        if (!locationRepository.refreshActive() && !locationRepository.someMenusLoaded()) {
            Toast.makeText(this, R.string.no_menus_loaded, Toast.LENGTH_SHORT).show()
        }
    }

    public override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }
}
