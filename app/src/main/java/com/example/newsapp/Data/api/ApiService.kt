package com.example.newsapp.Data.api

import com.example.newsapp.Data.models.NewsResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {

    @GET("top-headlines")
    suspend fun getTopHeadlines(
        @Query("country") country: String = "us",
        @Query("category") category: String = "business",
        @Query("apiKey") apiKey: String = "472693187c0e4b38809079b25108e5a0"
    ): Response<NewsResponse>

    // You can add more endpoints later
}