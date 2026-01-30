package com.example.newsapp.Data.Repository

import com.example.newsapp.Data.api.ApiService
import com.example.newsapp.Data.models.NewsResponse
import retrofit2.Response
import javax.inject.Inject  // CHANGE THIS from jakarta.inject to javax.inject

class NewsRepository @Inject constructor(
    private val apiService: ApiService
) {

    suspend fun getTopHeadlines(): Response<NewsResponse> {
        return apiService.getTopHeadlines()
    }

    suspend fun getTechCrunchHeadlines(): Response<NewsResponse> {
        return apiService.getTechCrunchHeadlines()
    }

    suspend fun searchNews(query: String): Response<NewsResponse> {
        return apiService.searchNews(query = query)
    }
}
