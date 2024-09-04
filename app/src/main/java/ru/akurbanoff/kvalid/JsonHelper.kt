package ru.akurbanoff.kvalid

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

class JsonHelper {
    @RequiresApi(Build.VERSION_CODES.N)
    fun getJsonKeys(): Set<String>{
        var keys = emptySet<String>()
        val json = Json { ignoreUnknownKeys = true }
        val strategy = object: DeserializationStrategy<JsonObject?> {
            override val descriptor: SerialDescriptor
                get() = buildClassSerialDescriptor("JsonObject")

            override fun deserialize(decoder: Decoder): JsonObject? {
                try {
                    val input = decoder as? JsonDecoder
                        ?: throw SerializationException("Expected JsonDecoder")
                    // Декодируем JSON в JsonElement
                    return input.decodeJsonElement().jsonObject
                } catch (e: IllegalArgumentException){
                    Log.w("IllegalArgument", "can`t convert response data to expected model")
                    return null
                }
            }
        }
        if(responseHolder.apiResponse != null) {
            val jsonObject = json.decodeFromString(
                deserializer = strategy,
                string = responseHolder.apiResponse!!
            )

            keys = collectKeys(jsonObject)
        }

        return keys
    }

    val keys = mutableSetOf<String>()

    @RequiresApi(Build.VERSION_CODES.N)
    fun collectKeys(json: JsonElement?): Set<String> {
        if(json == null) return emptySet()
        if(json is JsonArray) {
            if(json.size == 0) return emptySet()
        } else {
            if (json.jsonObject.keys.size == 0) return emptySet()
        }

        when (json) {
            is JsonObject -> {
                // Добавляем ключи текущего объекта
                keys.addAll(json.keys)
                // Рекурсивно обходим вложенные объекты
                json.forEach { s, value ->
                    if(value is JsonObject || value is JsonElement || value is JsonArray) {
                        try {
                            collectKeys(value)
                        } catch (e: IllegalArgumentException){
                            println(e.message + "-" + " $value")
                        }
                    }
                }
            }
            is JsonArray -> {
                // Обходим элементы массива
                json.forEach {
                    collectKeys(it)
                }
            }
            // Для других типов данных (числа, строки и т.д.) ничего не делаем
            else -> {}
        }
        return keys
    }
}