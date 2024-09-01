package ru.akurbanoff.kvalid

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response

class KValidInterceptor: Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        responseHolder.apiResponse = null
        val request = chain.request()
        val response = chain.proceed(request)

        val responseBodyString = response.body?.string()
        responseHolder.apiResponse = responseBodyString

        Log.w("API_RESPONSE", responseBodyString ?: "empty")

        return chain.proceed(request)
    }
}