package com.example.shared.data.api

import com.example.shared.data.models.NewsResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

class NewsApiService(
    private val client: HttpClient,
    private val apiKey: String
) {

    private val BASE_URL = "https://newsapi.org/v2/"

    suspend fun getTopHeadlines(
        country: String,
        category: String,
        page: Int,
        pageSize: Int
    ): NewsResponse {

        return client.get("${BASE_URL}top-headlines") {
            parameter("country", country)
            parameter("category", category)
            parameter("page", page)
            parameter("pageSize", pageSize)
            parameter("apiKey", apiKey)
        }.body()
    }

    suspend fun getTechCrunchHeadlines(
        page: Int,
        pageSize: Int
    ): NewsResponse {

        return client.get("${BASE_URL}top-headlines") {
            parameter("sources", "techcrunch")
            parameter("page", page)
            parameter("pageSize", pageSize)
            parameter("apiKey", apiKey)
        }.body()
    }

    suspend fun searchNews(
        query: String,
        page: Int,
        pageSize: Int
    ): NewsResponse {

        return client.get("${BASE_URL}everything") {
            parameter("q", query)
            parameter("sortBy", "publishedAt")
            parameter("language", "en")
            parameter("page", page)
            parameter("pageSize", pageSize)
            parameter("apiKey", apiKey)
        }.body()
    }
}