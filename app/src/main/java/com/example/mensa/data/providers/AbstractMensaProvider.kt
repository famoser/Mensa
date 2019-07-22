package com.example.mensa.data.providers

import android.content.res.AssetManager
import com.example.mensa.data.MensaProvider
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import java.io.IOException
import java.nio.charset.Charset


abstract class AbstractMensaProvider(private val assetManager: AssetManager): MensaProvider {

    protected fun <T> readJsonAssetFileToListOfT(rawFileName: String, classOfT: Class<T>): List<T> {
        val json: String = readStringAssetFile(rawFileName) ?: return ArrayList()

        val moshi = Moshi.Builder().build()
        val listOfT = Types.newParameterizedType(List::class.java, classOfT)
        val jsonAdapter = moshi.adapter<List<T>>(listOfT)

        return jsonAdapter.fromJson(json)!!
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