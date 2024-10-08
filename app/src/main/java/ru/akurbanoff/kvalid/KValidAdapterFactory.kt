package ru.akurbanoff.kvalid

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.gson.Gson
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
import retrofit2.CallAdapter
import retrofit2.Retrofit
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import kotlin.reflect.full.memberProperties

class KValidAdapterFactory(
    private val baseResponseModel: Class<*>? = null
): CallAdapter.Factory(){
    @RequiresApi(Build.VERSION_CODES.N)
    override fun get(type: Type, annotations: Array<out Annotation>, retrofit: Retrofit): CallAdapter<*, *>? {
        val jsonHelper = JsonHelper()
        val expectedModelKeys = jsonHelper.getJsonKeys()
        var responseType = getParameterUpperBound(0, type as ParameterizedType)
        var responseClass = getRawType(responseType)

        if(responseClass.isInterface || responseClass.name == baseResponseModel?.name) {
            try {
                responseType = getParameterUpperBound(0, responseType as ParameterizedType)
                responseClass = responseType.javaClass
            } catch (e: Exception){
                Log.e(e.javaClass.toString(), e.message ?: "")
            }
        }

        //val expectedClass = Class.forName("$modelsDirectoryPath/${responseType.typeName}").getDeclaredConstructor().newInstance()
        if(responseHolder.apiResponse != null) {
            try {
                val expectedResponseConvert =
                    Gson().fromJson(responseHolder.apiResponse, responseClass::class.java)

                val expectedResponseProperties = expectedResponseConvert::class.memberProperties.map { it.name }

                // Сравниваем наличие параметров
                expectedResponseProperties.all {
                    if(it !in expectedModelKeys) {
                        Log.w("NotFoundModelProperty", it)
                        false
                    } else true
                }
            } catch (e: IllegalArgumentException){
                Log.w("IllegalArgument", "can`t convert response data to expected model")
                return null
            }
        }

        return null
    }
}