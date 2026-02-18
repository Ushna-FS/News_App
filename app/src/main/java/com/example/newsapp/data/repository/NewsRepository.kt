package com.example.newsapp.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.newsapp.data.api.ApiService
import com.example.newsapp.data.models.Article
import com.example.newsapp.data.paging.NewsPagingSource
import com.example.newsapp.data.paging.NewsType
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import com.example.newsapp.data.paging.FilteredCombinedNewsPagingSource
import com.example.newsapp.utils.DateFormatter


class NewsRepository @Inject constructor(
    val apiService: ApiService,
    private val dateFormatter: DateFormatter
) {

    fun getCombinedNewsStream(
        categories: List<String> = emptyList(),
        sources: List<String> = emptyList(),
        sortType: SortType = SortType.NEWEST_FIRST
    ): Flow<PagingData<Article>> {
        return Pager(
            config = PagingConfig(
                pageSize = 5, enablePlaceholders = false, prefetchDistance = 1, initialLoadSize = 5
            ), pagingSourceFactory = {
                FilteredCombinedNewsPagingSource(
                    apiService = apiService,
                    categories = categories,
                    sources = sources,
                    sortType = sortType,
                    dateFormatter=dateFormatter
                )
            }).flow
    }

    fun getAllNewsStream(): Flow<PagingData<Article>> {
        return Pager(
            config = PagingConfig(
                pageSize = 5,
                enablePlaceholders = false,
                prefetchDistance = 1,
                initialLoadSize = 5
            ),
            pagingSourceFactory = {
                NewsPagingSource(apiService, NewsType.Everything)
            }
        ).flow
    }

    fun getCategoryNewsStream(category: String): Flow<PagingData<Article>> {
        return Pager(
            config = PagingConfig(
                pageSize = 5,
                enablePlaceholders = false,
                prefetchDistance = 1,
                initialLoadSize = 5
            ),
            pagingSourceFactory = {
                NewsPagingSource(apiService, NewsType.NewsCategory(category))
            }
        ).flow
    }


    // Search stream
    fun searchNewsStream(query: String): Flow<PagingData<Article>> {
        return Pager(
            config = PagingConfig(
                pageSize = 5, enablePlaceholders = false, prefetchDistance = 1, initialLoadSize = 5
            ), pagingSourceFactory = {
                NewsPagingSource(apiService, NewsType.Search(query))
            }).flow
    }
}

enum class SortType {
    NEWEST_FIRST, OLDEST_FIRST
}