
package com.example.newsapp.data.Repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.newsapp.data.api.ApiService
import com.example.newsapp.data.models.Article
import com.example.newsapp.data.models.NewsResponse
import com.example.newsapp.data.paging.NewsPagingSource
import com.example.newsapp.data.paging.NewsType
import kotlinx.coroutines.flow.Flow
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class NewsRepository @Inject constructor(
    private val apiService: ApiService
) {
    // Backward compatibility functions
    suspend fun getTopHeadlines(page: Int = 1, pageSize: Int = 5): Response<NewsResponse> {
        return apiService.getTopHeadlines(page = page, pageSize = pageSize)
    }

    suspend fun getTechCrunchHeadlines(page: Int = 1, pageSize: Int = 5): Response<NewsResponse> {
        return apiService.getTechCrunchHeadlines(page = page, pageSize = pageSize)
    }

    suspend fun searchNews(
        query: String,
        page: Int = 1,
        pageSize: Int = 5
    ): Response<NewsResponse> {
        return apiService.searchNews(query = query, page = page, pageSize = pageSize)
    }

    // UPDATED: Accept filter and sort parameters
    fun getCombinedNewsStream(
        categories: List<String> = emptyList(),
        sources: List<String> = emptyList(),
        sortType: SortType = SortType.NEWEST_FIRST
    ): Flow<PagingData<Article>> {
        return Pager(
            config = PagingConfig(
                pageSize = 5,
                enablePlaceholders = false,
                prefetchDistance = 1,
                initialLoadSize = 5
            ),
            pagingSourceFactory = {
                FilteredCombinedNewsPagingSource(
                    apiService = apiService,
                    categories = categories,
                    sources = sources,
                    sortType = sortType
                )
            }
        ).flow
    }

    // For HomeFragment - Only Business news
    fun getBusinessNewsStream(): Flow<PagingData<Article>> {
        return Pager(
            config = PagingConfig(
                pageSize = 5,
                enablePlaceholders = false,
                prefetchDistance = 1,
                initialLoadSize = 5
            ),
            pagingSourceFactory = {
                NewsPagingSource(apiService, NewsType.Business)
            }
        ).flow
    }

    // Search stream
    fun searchNewsStream(query: String): Flow<PagingData<Article>> {
        return Pager(
            config = PagingConfig(
                pageSize = 5,
                enablePlaceholders = false,
                prefetchDistance = 1,
                initialLoadSize = 5
            ),
            pagingSourceFactory = {
                NewsPagingSource(apiService, NewsType.Search(query))
            }
        ).flow
    }
}

// UPDATED: Paging source that accepts filters and sort
class FilteredCombinedNewsPagingSource(
    private val apiService: ApiService,
    private val categories: List<String> = emptyList(),
    private val sources: List<String> = emptyList(),
    private val sortType: SortType = SortType.NEWEST_FIRST,
    private val pageSize: Int = 5
) : PagingSource<Int, Article>() {

    override fun getRefreshKey(state: PagingState<Int, Article>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Article> {
        return try {
            val page = params.key ?: 1
            val currentPageSize = params.loadSize.coerceAtMost(pageSize)

            // Determine what to fetch based on categories
            val shouldFetchBusiness = categories.isEmpty() ||
                    categories.any { it.equals("Business", ignoreCase = true) }
            val shouldFetchTech = categories.isEmpty() ||
                    categories.any { it.equals("Technology", ignoreCase = true) }

            // Fetch business news if needed
            val businessArticles = if (shouldFetchBusiness) {
                kotlin.runCatching {
                    apiService.getTopHeadlines(page = page, pageSize = currentPageSize)
                }.getOrNull()?.body()?.articles ?: emptyList()
            } else {
                emptyList()
            }

            // Fetch tech news if needed
            val techArticles = if (shouldFetchTech) {
                kotlin.runCatching {
                    apiService.getTechCrunchHeadlines(page = page, pageSize = currentPageSize)
                }.getOrNull()?.body()?.articles ?: emptyList()
            } else {
                emptyList()
            }

            // Combine articles
            var filteredArticles = (businessArticles + techArticles)
                .distinctBy { it.url }

            // Apply source filter if sources are selected
            if (sources.isNotEmpty()) {
                filteredArticles = filteredArticles.filter { article ->
                    val sourceName = article.source?.name ?: ""
                    sources.any { source ->
                        sourceName.contains(source, ignoreCase = true)
                    }
                }
            }

            // FIXED: Sort properly by converting dates to comparable timestamps
            filteredArticles = when (sortType) {
                SortType.NEWEST_FIRST -> filteredArticles.sortedByDescending {
                    it.publishedAt?.let { dateStr ->
                        parseDateToLong(dateStr)
                    } ?: 0L
                }

                SortType.OLDEST_FIRST -> filteredArticles.sortedBy {
                    it.publishedAt?.let { dateStr ->
                        parseDateToLong(dateStr)
                    } ?: 0L
                }
            }

            // Check if we have more data
            val hasMore = (businessArticles.isNotEmpty() || techArticles.isNotEmpty()) &&
                    filteredArticles.isNotEmpty()

            LoadResult.Page(
                data = filteredArticles,
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (hasMore) page + 1 else null
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    private fun parseDateToLong(dateString: String?): Long {
        return try {
            if (dateString.isNullOrEmpty()) 0L else {
                val formats = listOf(
                    "yyyy-MM-dd'T'HH:mm:ss'Z'",
                    "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                    "yyyy-MM-dd HH:mm:ss",
                    "yyyy-MM-dd"
                )
                for (format in formats) {
                    try {
                        val sdf = SimpleDateFormat(format, Locale.getDefault())
                        sdf.timeZone = TimeZone.getTimeZone("UTC")
                        return sdf.parse(dateString)?.time ?: 0L
                    } catch (e: Exception) {
                        continue
                    }
                }
                0L
            }
        } catch (e: Exception) {
            0L
        }
    }
}

enum class SortType {
    NEWEST_FIRST,
    OLDEST_FIRST
}