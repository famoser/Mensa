package ch.famoser.mensa.services

import android.content.SharedPreferences
import ch.famoser.mensa.models.Menu

interface ICacheService {
    fun startObserveCacheUsage()
    fun removeAllUntouchedCacheEntries()

    fun saveMenus(key: String, menus: List<Menu>)
    fun readMenus(key: String): List<Menu>?
    fun saveMensaIds(key: String, mensaIds: List<String>)
    fun readMensaIds(key: String): List<String>?

    fun saveString(key: String, value: String)
    fun readString(key: String): String?
}

class CacheService(
    private val preferences: SharedPreferences,
    private val serializationService: SerializationService
) : ICacheService {
    companion object {
        const val CACHE_PREFIX = "cache"
    }

    private val touchedCacheKeys = HashSet<String>()

    override fun startObserveCacheUsage() {
        touchedCacheKeys.clear()
    }

    override fun saveMenus(key: String, menus: List<Menu>) {
        val cacheKey = getCacheKey(key, CacheType.Menu)
        val json = serializationService.serialize(menus.toTypedArray())

        preferences.edit().putString(cacheKey, json).apply()
    }

    override fun readMenus(key: String): List<Menu>? {
        val cacheKey = getCacheKey(key, CacheType.Menu)
        val json = preferences.getString(cacheKey, null) ?: return null

        return serializationService.deserializeList(json)
    }

    override fun saveMensaIds(key: String, mensaIds: List<String>) {
        val cacheKey = getCacheKey(key, CacheType.MensaIds)
        val json = serializationService.serialize(mensaIds.toTypedArray())

        preferences.edit().putString(cacheKey, json).apply()
    }

    override fun readMensaIds(key: String): List<String>? {
        val cacheKey = getCacheKey(key, CacheType.MensaIds)
        val json = preferences.getString(cacheKey, null) ?: return null

        return serializationService.deserializeList(json)
    }

    override fun saveString(key: String, value: String) {
        val cacheKey = getCacheKey(key, CacheType.String)

        preferences.edit().putString(cacheKey, value).apply()
    }

    override fun readString(key: String): String? {
        val cacheKey = getCacheKey(key, CacheType.String)
        return preferences.getString(cacheKey, null)
    }

    override fun removeAllUntouchedCacheEntries() {
        val obsoleteKeys =
            preferences.all.keys.filter { k -> k.startsWith(CACHE_PREFIX) && !touchedCacheKeys.contains(k) }

        val editor = preferences.edit()
        for (key in obsoleteKeys) {
            editor.remove(key)
        }
        editor.apply()
    }

    private fun getCacheKey(key: String, cacheType: CacheType): String {
        val cacheKey = "$CACHE_PREFIX.$cacheType.$key"
        touchedCacheKeys.add(cacheKey)

        return cacheKey
    }

    private enum class CacheType {
        Menu,
        MensaIds,
        String
    }

}