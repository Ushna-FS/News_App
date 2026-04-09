package com.example.newsapp.data.paging

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.newsapp.data.api.ApiService
import com.example.newsapp.data.models.Article
import com.example.newsapp.data.models.NetworkError
import com.example.newsapp.data.models.getCategory
import com.example.newsapp.data.repository.SortType
import com.example.newsapp.utils.DateFormatter
import retrofit2.HttpException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.time.Instant
import java.time.ZoneId
import java.util.Locale

sealed class NewsType {
    object Business : NewsType()
    object TechCrunch : NewsType()
    object Everything : NewsType()
    data class NewsCategory(val name: String) : NewsType()
    data class Search(val query: String) : NewsType()
}

class NewsPagingSource(
    private val apiService: ApiService, private val newsType: NewsType
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
            val pageSize = params.loadSize

            val response = when (newsType) {
                is NewsType.Business -> apiService.getTopHeadlines(

                    page = page, pageSize = pageSize
                )

                is NewsType.TechCrunch -> apiService.getTechCrunchHeadlines(
                    page = page, pageSize = pageSize
                )

                is NewsType.Everything -> apiService.searchNews(
                    query = "general",
                    page = page,
                    pageSize = pageSize
                )

                is NewsType.Search -> apiService.searchNews(
                    query = newsType.query, page = page, pageSize = pageSize
                )

                is NewsType.NewsCategory -> apiService.getTopHeadlines(
                    category = newsType.name,
                    page = page,
                    pageSize = pageSize
                )

            }

            if (response.isSuccessful) {
                val articles = response.body()?.articles ?: emptyList()
                val total = response.body()?.totalResults ?: 0
                val endReached = page * pageSize >= total

                LoadResult.Page(
                    data = articles,
                    prevKey = if (page == 1) null else page - 1,
                    nextKey = if (endReached) null else page + 1
                )
            } else {
                LoadResult.Error(HttpException(response))
            }
        } catch (e: Exception) {

            val error = when (e) {

                is NetworkError -> e

                is UnknownHostException,
                is SocketTimeoutException -> NetworkError.NoInternet()

                is HttpException -> {
                    when (e.code()) {
                        401 -> NetworkError.Unauthorized()
                        404 -> NetworkError.NotFound()
                        429 -> NetworkError.RateLimit()
                        in 500..599 -> NetworkError.ServerError()
                        else -> NetworkError.Unknown(e)
                    }
                }

                else -> NetworkError.Unknown(e)
            }

            LoadResult.Error(error)
        }
    }
}

class FilteredCombinedNewsPagingSource(
    private val apiService: ApiService,
    private val categories: List<String> = emptyList(),
    private val sources: List<String> = emptyList(),
    private val sortType: SortType = SortType.NEWEST_FIRST,
    private val dateFormatter: DateFormatter
) : PagingSource<Int, Article>() {

    override fun getRefreshKey(state: PagingState<Int, Article>): Int? {
        return state.anchorPosition?.let { position ->
            state.closestPageToPosition(position)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(position)?.nextKey?.minus(1)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Article> {
        return try {
            val page = params.key ?: 1
            val pageSize = params.loadSize

            val query = if (categories.isNotEmpty() && !categories.contains("All")) {
                categories.joinToString(" OR ") { it.lowercase(Locale.getDefault()) }
            } else {
                "news"
            }

            // For OLDEST_FIRST, we need to fetch more pages to get complete month data
            val actualPageSize = if (sortType == SortType.OLDEST_FIRST) {
                pageSize * 3
            } else {
                pageSize
            }

            val response = apiService.searchNews(
                query = query,
                page = page,
                pageSize = actualPageSize,
                sortBy = "publishedAt"
            )

            if (!response.isSuccessful) return LoadResult.Error(HttpException(response))

            var articles =
                response.body()?.articles?.filter { it.title != "[Removed]" } ?: emptyList()

            // Apply filters BEFORE sorting
            if (categories.isNotEmpty() && !categories.contains("All")) {
                articles = articles.filter { article ->
                    val articleCategory = article.getCategory().displayName
                    categories.any { category ->
                        articleCategory.equals(
                            category,
                            ignoreCase = true
                        )
                    }
                }
            }

            if (sources.isNotEmpty()) {
                articles = articles.filter { article ->
                    val sourceName = article.source.name.lowercase(Locale.getDefault())
                    sources.any { selectedSource ->
                        sourceName.contains(selectedSource.lowercase(Locale.getDefault()))
                    }
                }
            }

            // Store original articles before sorting for pagination
            val originalArticles = articles

            // ---------- Oldest First Logic ----------
            if (sortType == SortType.OLDEST_FIRST) {
                val articlesWithDates = articles.mapNotNull { article ->
                    try {
                        val zonedDateTime = Instant.parse(article.publishedAt)
                            .atZone(ZoneId.systemDefault())
                        article to zonedDateTime
                    } catch (e: Exception) {
                        null
                    }
                }

                if (articlesWithDates.isNotEmpty()) {
                    // Find the latest month that has articles
                    val latestMonth = articlesWithDates.maxByOrNull { it.second }?.second

                    // Filter only articles from that latest month
                    val latestMonthArticles = articlesWithDates.filter { (_, date) ->
                        latestMonth?.let {
                            date.year == it.year && date.monthValue == it.monthValue
                        } ?: false
                    }

                    // Sort these articles from oldest to newest
                    articles = latestMonthArticles
                        .sortedBy { it.second }
                        .map { it.first }
                } else {
                    articles = emptyList()
                }
            }

            // Determine if there are more pages
            val hasMore = if (sortType == SortType.OLDEST_FIRST) {
                val hasNextMonth = originalArticles.any { article ->
                    try {
                        val date = Instant.parse(article.publishedAt).atZone(ZoneId.systemDefault())
                        val latestMonth = articles.firstOrNull()?.let {
                            Instant.parse(it.publishedAt).atZone(ZoneId.systemDefault())
                        }
                        latestMonth?.let {
                            date.year < it.year || (date.year == it.year && date.monthValue < it.monthValue)
                        } ?: false
                    } catch (e: Exception) {
                        false
                    }
                }
                hasNextMonth
            } else {
                response.body()?.totalResults?.let { total -> total > page * pageSize } ?: false
            }

            LoadResult.Page(
                data = articles,
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (hasMore && articles.isNotEmpty()) page + 1 else null
            )

        } catch (e: Exception) {

            val error = when (e) {

                is NetworkError -> e

                is UnknownHostException,
                is SocketTimeoutException -> NetworkError.NoInternet()

                is HttpException -> {
                    when (e.code()) {
                        401 -> NetworkError.Unauthorized()
                        404 -> NetworkError.NotFound()
                        429 -> NetworkError.RateLimit()
                        in 500..599 -> NetworkError.ServerError()
                        else -> NetworkError.Unknown(e)
                    }
                }

                else -> NetworkError.Unknown(e)
            }

            LoadResult.Error(error)
        }
    }
}