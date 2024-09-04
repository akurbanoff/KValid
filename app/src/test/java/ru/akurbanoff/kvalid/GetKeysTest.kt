package ru.akurbanoff.kvalid

import android.util.Log
import com.google.gson.Gson
import junit.framework.TestCase.assertEquals
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.add
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class GetKeysTest {
    private val jsonHelper = JsonHelper()

    private val mockDataHolder = MockDataHolder()

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

    @Test
    fun `should return empty set if json is null`(){
        val jsonObject = buildJsonObject {}

        val result = jsonHelper.collectKeys(jsonObject)

        assertEquals(
            result.size,
            0
        )
    }

    @Test
    fun `should collect keys from jsonObject`(){
        val jsonObject = buildJsonObject {
            put("name", "Artem")
            put("secondName", "Kurbanov")
            put("age", 20)
            putJsonObject("cities"){
                put("city", "Krasnodar")
            }
            putJsonArray("items"){
                for(i in 1..5){
                    addJsonObject {
                        put("info${i}", "Krasnodar${i + 1}")
                    }
                }
            }
        }

        val result = jsonHelper.collectKeys(jsonObject)

        assertEquals(
            result.size,
            11
        )
    }

    @Test
    fun `should create inner set for keys if it inner JsonObject`(){
        val jsonObject = buildJsonObject {
            putJsonObject("cities"){
                put("city", "Krasnodar")
            }
        }
        val result = jsonHelper.collectKeys(jsonObject)

        assertThat(result)
            .usingRecursiveComparison()
            .isEqualTo(setOf("cities", setOf("city")))
    }

    @Test
    fun `should get keys from real model`(){
        val routeTaskStatusResponse = mockDataHolder.createRouteTaskResponse()
        val json = Json { ignoreUnknownKeys = true }
        val jsonObject = json.decodeFromString(
            deserializer = strategy,
            string = routeTaskStatusResponse
        )

        val result = jsonHelper.collectKeys(jsonObject)

        assertEquals(
            53,
            result.size
        )
    }
}