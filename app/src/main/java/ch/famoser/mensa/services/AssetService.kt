package ch.famoser.mensa.services

import android.content.res.AssetManager
import java.io.IOException
import java.nio.charset.Charset

interface IAssetService {
    fun readStringFile(fileName: String): String?
}

class AssetService(private val assetManager: AssetManager) : IAssetService {

    override fun readStringFile(fileName: String): String? {
        var json: String? = null
        try {
            val inputStream = assetManager.open(fileName)
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            json = String(buffer, Charset.forName("UTF-8"))
        } catch (ex: IOException) {
            ex.printStackTrace()
        }

        return json
    }
}