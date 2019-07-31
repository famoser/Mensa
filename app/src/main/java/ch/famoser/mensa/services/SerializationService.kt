package ch.famoser.mensa.services

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types

interface ISerializationService {
    fun <T> deserializeList(json: String, entryClassOfT: Class<T>): List<T>
    fun <T : Any> serialize(request: T): String
}

class SerializationService : ISerializationService {
    override fun <T> deserializeList(json: String, entryClassOfT: Class<T>): List<T> {
        val moshi = Moshi.Builder().build()
        val listOfT = Types.newParameterizedType(List::class.java, entryClassOfT)
        val jsonAdapter = moshi.adapter<List<T>>(listOfT)

        return jsonAdapter.fromJson(json)!!
    }

    override fun <T : Any> serialize(request: T): String {
        val moshi = Moshi.Builder().build()
        val jsonAdapter = moshi.adapter(request.javaClass)

        return jsonAdapter.toJson(request)
    }
}