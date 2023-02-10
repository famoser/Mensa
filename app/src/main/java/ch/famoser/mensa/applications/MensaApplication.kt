package ch.famoser.mensa.applications

import android.app.Application
import com.google.android.material.color.DynamicColors

class MensaApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        DynamicColors.applyToActivitiesIfAvailable(this)
    }
}