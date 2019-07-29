package ch.famoser.mensa.services

import android.content.SharedPreferences
import ch.famoser.mensa.models.Menu

class CacheService(
    private val preferences: SharedPreferences,
    private val serializationService: SerializationService
) {
    private val touchedCacheKeys = HashSet<String>()

    fun startObserveUsedCacheUsage() {
        touchedCacheKeys.clear()
    }

    fun saveMenus(key: String, menus: List<Menu>) {
        val cacheKey = getCacheKey(key, CacheType.Menu)
        val json = serializationService.serialize(menus.toTypedArray())

        preferences.edit().putString(cacheKey, json).apply()
    }

    fun readMenus(key: String): List<Menu>? {
        val cacheKey = getCacheKey(key, CacheType.Menu)
        val json = preferences.getString(cacheKey, null) ?: return null

        return serializationService.deserializeList(json, Menu::class.java)
    }

    fun saveMensaIds(key: String, mensaIds: List<Int>) {
        val cacheKey = getCacheKey(key, CacheType.MensaIds)
        val json = serializationService.serialize(mensaIds.toTypedArray())

        preferences.edit().putString(cacheKey, json).apply()
    }

    fun readMensaIds(key: String): List<Int>? {
        val cacheKey = getCacheKey(key, CacheType.MensaIds)
        val json = preferences.getString(cacheKey, null) ?: return null

        return serializationService.deserializeList(json, Int::class.java)
    }

    fun removeAllUntouchedCacheEntries() {
        val obsoleteKeys = preferences.all.keys.filter { k -> !touchedCacheKeys.contains(k) }

        val editor = preferences.edit()
        for (key in obsoleteKeys) {
            editor.remove(key)
        }
        editor.apply()
    }

    private fun getCacheKey(key: String, cacheType: CacheType): String {
        val cacheKey = "cache.$cacheType.$key"
        touchedCacheKeys.add(cacheKey)

        return cacheKey
    }

    private enum class CacheType(private val value: String) {
        Menu("menu"),
        MensaIds("menuIds")
    }
}