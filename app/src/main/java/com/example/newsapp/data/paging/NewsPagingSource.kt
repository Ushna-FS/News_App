package com.example.newsapp.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.newsapp.data.api.ApiService
import com.example.newsapp.data.models.Article
import com.example.newsapp.data.repository.SortType
import com.example.newsapp.utils.DateFormatter
import retrofit2.HttpException
import java.io.IOException

sealed class NewsType {
    object Business : NewsType()
    object TechCrunch : NewsType()
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

                is NewsType.Search -> apiService.searchNews(
                    query = newsType.query, page = page, pageSize = pageSize
                )
            }

            if (response.isSuccessful) {
                val articles = response.body()?.articles ?: emptyList()
                val nextPage = if (articles.isNotEmpty()) page + 1 else null

                LoadResult.Page(
                    data = articles, prevKey = if (page == 1) null else page - 1, nextKey = nextPage
                )
            } else {
                LoadResult.Error(HttpException(response))
            }
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}

class FilteredCombinedNewsPagingSource(
    private val apiService: ApiService,
    private val categories: List<String> = emptyList(),
    private val sources: List<String> = emptyList(),
    private val sortType: SortType = SortType.NEWEST_FIRST,
    private val dateFormatter: DateFormatter,
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
            val currentPageSize = params.loadSize

            // Determine what to fetch based on categories
            val shouldFetchBusiness =
                categories.isEmpty() || categories.any { it.equals("Business", ignoreCase = true) }
            val shouldFetchTech = categories.isEmpty() || categories.any {
                it.equals(
                    "Technology", ignoreCase = true
                )
            }

            if (!shouldFetchBusiness && !shouldFetchTech) {
                return LoadResult.Page(
                    data = emptyList(), prevKey = null, nextKey = null
                )
            }
            // Fetch business news if needed
            val businessResponse =
                apiService.getTopHeadlines(page = page, pageSize = currentPageSize)

            val businessArticles = if (businessResponse.isSuccessful) {
                businessResponse.body()?.articles ?: emptyList()
            } else {
                throw HttpException(businessResponse)
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
            var filteredArticles = (businessArticles + techArticles).distinctBy { it.url }

            // Apply source filter if sources are selected
            if (sources.isNotEmpty()) {
                filteredArticles = filteredArticles.filter { article ->
                    val sourceName = article.source.name ?: ""
                    sources.any { source ->
                        sourceName.contains(source, ignoreCase = true)
                    }
                }
            }

            filteredArticles = when (sortType) {
                SortType.NEWEST_FIRST ->
                    filteredArticles.sortedByDescending {
                        dateFormatter.parseToTimestamp(it.publishedAt)
                    }

                SortType.OLDEST_FIRST ->
                    filteredArticles.sortedBy {
                        dateFormatter.parseToTimestamp(it.publishedAt)
                    }
            }

            val hasMore =
                businessArticles.size == currentPageSize || techArticles.size == currentPageSize

            LoadResult.Page(
                data = filteredArticles,
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (hasMore) page + 1 else null
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}