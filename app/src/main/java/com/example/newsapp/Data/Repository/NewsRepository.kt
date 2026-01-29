package com.example.newsapp.Data.Repository


import com.example.newsapp.Data.api.ApiService
import com.example.newsapp.Data.api.RetrofitInstance
import com.example.newsapp.Data.models.NewsResponse
import retrofit2.Response

class NewsRepository {

    private val apiService: ApiService = RetrofitInstance.apiService

    suspend fun getTopHeadlines(): Response<NewsResponse> {
        return apiService.getTopHeadlines()
    }
}