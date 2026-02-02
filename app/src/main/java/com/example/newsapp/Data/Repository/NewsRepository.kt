package com.example.newsapp.Data.Repository

import com.example.newsapp.Data.api.ApiService
import com.example.newsapp.Data.models.NewsResponse
import retrofit2.Response
import javax.inject.Inject

class NewsRepository @Inject constructor(
    private val apiService: ApiService
) {

    suspend fun getTopHeadlines(page: Int = 1, pageSize: Int = 5): Response<NewsResponse> {
        return apiService.getTopHeadlines(page = page, pageSize = pageSize)
    }

    suspend fun getTechCrunchHeadlines(page: Int = 1, pageSize: Int = 5): Response<NewsResponse> {
        return apiService.getTechCrunchHeadlines(page = page, pageSize = pageSize)
    }

    suspend fun searchNews(query: String, page: Int = 1, pageSize: Int = 5): Response<NewsResponse> {
        return apiService.searchNews(query = query, page = page, pageSize = pageSize)
    }
}