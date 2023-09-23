package ch.famoser.mensa.testServices

import ch.famoser.mensa.models.Menu
import ch.famoser.mensa.services.ICacheService

class NoCacheService : ICacheService {
    override fun removeAllUntouchedCacheEntries() {
        // implementation skipped
    }

    override fun saveMenus(key: String, menus: List<Menu>) {
        // implementation skipped
    }

    override fun readMenus(key: String): List<Menu>? {
        return null
    }

    override fun saveMensaIds(key: String, mensaIds: List<String>) {
        // implementation skipped
    }

    override fun readMensaIds(key: String): List<String>? {
        return null
    }

    override fun saveString(key: String, value: String) {
        // implementation skipped
    }

    override fun readString(key: String): String? {
        return null
    }

    override fun startObserveCacheUsage() {
        // implementation skipped
    }
}