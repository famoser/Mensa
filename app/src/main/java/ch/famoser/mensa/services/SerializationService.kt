package ch.famoser.mensa.services

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import java.lang.reflect.Type

interface ISerializationService {
    fun <T> deserializeList(json: String, type: Class<T>): List<T>
    fun <T> deserializeList(json: String, type: Type): List<T>
    fun <T : Any> serialize(request: T): String
}

class SerializationService : ISerializationService {
    override fun <T> deserializeList(json: String, type: Class<T>): List<T> {
        return deserializeList(json, type as Type)
    }

    override fun <T> deserializeList(json: String, type: Type): List<T> {
        val moshi = Moshi.Builder().build()
        val listOfT = Types.newParameterizedType(List::class.java, type)
        val jsonAdapter = moshi.adapter<List<T>>(listOfT)

        return jsonAdapter.fromJson(json)!!
    }

    override fun <T : Any> serialize(request: T): String {
        val moshi = Moshi.Builder().build()
        val jsonAdapter = moshi.adapter(request.javaClass)

        return jsonAdapter.toJson(request)
    }
}