package com.example.newsapp.data.api

import com.example.newsapp.data.models.NewsResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {

    @GET("top-headlines")
    suspend fun getTopHeadlines(
        @Query("country") country: String = "us",
        @Query("category") category: String = "business",
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 5,
        @Query("apiKey") apiKey: String = "159b885b18104900b02c921bebf1fce8"
    ): Response<NewsResponse>

    @GET("top-headlines")
    suspend fun getTechCrunchHeadlines(
        @Query("sources") sources: String = "techcrunch",
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 5,
        @Query("apiKey") apiKey: String = "159b885b18104900b02c921bebf1fce8"
    ): Response<NewsResponse>

    @GET("everything")
    suspend fun searchNews(
        @Query("q") query: String,
        @Query("sortBy") sortBy: String = "publishedAt",
        @Query("language") language: String = "en",
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 5,
        @Query("apiKey") apiKey: String = "159b885b18104900b02c921bebf1fce8"
    ): Response<NewsResponse>
}