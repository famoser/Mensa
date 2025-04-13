package ch.famoser.mensa.services.providers

import ch.famoser.mensa.services.SerializationService
import ch.famoser.mensa.testServices.InMemoryAssetService
import ch.famoser.mensa.testServices.NoCacheService
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.util.*

abstract class BaseProviderTest {
    protected fun isWeekday(): Boolean {
        val c = Calendar.getInstance()
        val dayOfWeek = c.get(Calendar.DAY_OF_WEEK)
        if (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) {
            return false
        }

        return true
    }

    protected fun getNextWeekdayDate(): Date {
        val c = Calendar.getInstance()
        val dayOfWeek = c.get(Calendar.DAY_OF_WEEK)
        if (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) {
            c.add(Calendar.DAY_OF_WEEK, 2);
        }

        return Date.from(c.toInstant())
    }
}