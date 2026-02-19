package com.example.newsapp.data.api

import com.example.newsapp.BuildConfig
import okhttp3.Interceptor
import okhttp3.Response

class ApiKeyInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {

        val originalRequest = chain.request()

        val originalUrl = originalRequest.url

        val newUrl =
            originalUrl.newBuilder().addQueryParameter("apiKey", BuildConfig.NEWS_API_KEY).build()

        val newRequest = originalRequest.newBuilder().url(newUrl).build()

        return chain.proceed(newRequest)
    }
}
