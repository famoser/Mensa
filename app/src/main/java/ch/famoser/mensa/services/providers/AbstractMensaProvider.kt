package ch.famoser.mensa.services.providers

import android.content.res.AssetManager
import ch.famoser.mensa.models.Location
import ch.famoser.mensa.models.Mensa
import ch.famoser.mensa.models.Menu
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import java.io.IOException
import java.nio.charset.Charset
import java.time.LocalDate


abstract class AbstractMensaProvider(private val assetManager: AssetManager) {
    abstract fun getLocations(): List<Location>

    abstract fun getMenus(mensa: Mensa, date: LocalDate): List<Menu>

    protected fun <T> readJsonAssetFileToListOfT(rawFileName: String, classOfT: Class<T>): List<T> {
        val json: String = readStringAssetFile(rawFileName) ?: return ArrayList()
        return jsonToListOfT(json, classOfT)
    }

    protected fun <T> jsonToListOfT(json: String, classOfT: Class<T>): List<T> {
        val moshi = Moshi.Builder().build()
        val listOfT = Types.newParameterizedType(List::class.java, classOfT)
        val jsonAdapter = moshi.adapter<List<T>>(listOfT)

        return jsonAdapter.fromJson(json)!!
    }

    protected fun <T> jsonToT(json: String, classOfT: Class<T>): T {
        val moshi = Moshi.Builder().build()
        val jsonAdapter = moshi.adapter<T>(classOfT)

        return jsonAdapter.fromJson(json)!!
    }

    protected fun normalizeText(text: String): String {
        // remove too much whitespace
        var normalized = text.replace("  ", " ")
        normalized = normalized.replace(" \n", "\n")
        normalized = normalized.replace("\n ", "\n")

        return normalized;
    }

    private fun readStringAssetFile(rawFileName: String): String? {
        var json: String? = null
        try {
            val inputStream = assetManager.open(rawFileName)
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            json = String(buffer, Charset.forName("UTF-8"))
        } catch (ex: IOException) {
            ex.printStackTrace()
        }

        return json;
    }
}