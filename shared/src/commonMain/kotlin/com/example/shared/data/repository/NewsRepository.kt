package com.example.shared.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.shared.data.paging.FilteredCombinedNewsPagingSource
import com.example.shared.data.paging.NewsPagingSource
import com.example.shared.data.paging.NewsType
import com.example.shared.data.api.NewsApiService
import com.example.shared.data.models.Article
import com.example.shared.utils.DateFormatter
import kotlinx.coroutines.flow.Flow


class NewsRepository(
    val apiService: NewsApiService,
    private val dateFormatter: DateFormatter
) {
    fun getCombinedNewsStream(
        categories: List<String> = emptyList(),
        sources: List<String> = emptyList(),
        sortType: SortType = SortType.NEWEST_FIRST
    ): Flow<PagingData<Article>> {

        return Pager(
            config = PagingConfig(
                pageSize = 10,
                prefetchDistance = 2,
                initialLoadSize = 10,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                FilteredCombinedNewsPagingSource(
                    apiService = apiService,
                    categories = categories,
                    sources = sources,
                    sortType = sortType,
                    dateFormatter = dateFormatter
                )
            }
        ).flow
    }

    fun getAllNewsStream(): Flow<PagingData<Article>> {
        return Pager(
            config = PagingConfig(
                pageSize = 10,
                enablePlaceholders = false,
                prefetchDistance = 2,
                initialLoadSize = 10
            ),
            pagingSourceFactory = {
                NewsPagingSource(apiService, NewsType.Everything)
            }
        ).flow
    }

    fun getCategoryNewsStream(category: String): Flow<PagingData<Article>> {
        return Pager(
            config = PagingConfig(
                pageSize = 10,
                enablePlaceholders = false,
                prefetchDistance = 2,
                initialLoadSize = 10
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
                pageSize = 10,
                enablePlaceholders = false,
                prefetchDistance = 2,
                initialLoadSize = 10
            ), pagingSourceFactory = {
                NewsPagingSource(apiService, NewsType.Search(query))
            }).flow
    }
}

enum class SortType {
    NEWEST_FIRST, OLDEST_FIRST
}