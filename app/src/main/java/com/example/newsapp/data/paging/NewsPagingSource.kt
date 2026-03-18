package com.example.newsapp.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.newsapp.utils.ArticleCategoryMapper
import com.example.newsapp.data.api.ApiService
import com.example.newsapp.data.models.Article
import com.example.newsapp.data.repository.SortType
import com.example.newsapp.utils.DateFormatter
import retrofit2.HttpException

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
    private val dateFormatter: DateFormatter
) : PagingSource<Int, Article>() {

    override fun getRefreshKey(state: PagingState<Int, Article>): Int? {
        return state.anchorPosition?.let { position ->
            state.closestPageToPosition(position)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(position)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Article> {
        return try {
            val page = params.key ?: 1
            val pageSize = params.loadSize

            // Build query from selected categories
            val query = if (categories.isNotEmpty() && !categories.contains("All")) {
                categories.joinToString(" OR ") { it.lowercase() }
            } else {
                "news"
            }

            val response = apiService.searchNews(
                query = query,
                page = page,
                pageSize = pageSize,
                sortBy = when (sortType) {
                    SortType.NEWEST_FIRST -> "publishedAt"
                    SortType.OLDEST_FIRST -> "publishedAt"
                }
            )

            if (!response.isSuccessful) {
                return LoadResult.Error(HttpException(response))
            }

            var articles = response.body()?.articles?.filter {
                it.title != "[Removed]"
            } ?: emptyList()

            // Apply category filter
            if (categories.isNotEmpty() && !categories.contains("All")) {
                articles = articles.filter { article ->
                    val articleCategory = ArticleCategoryMapper.getCategory(article)
                    categories.any { category ->
                        articleCategory.equals(category, ignoreCase = true)
                    }
                }
            }

            // source filtering
            if (sources.isNotEmpty()) {
                articles = articles.filter { article ->
                    val sourceName = article.source.name.lowercase()
                    sources.any { selectedSource ->
                        val selectedLower = selectedSource.lowercase()
                        // Check various matching conditions
                        sourceName.contains(selectedLower) || // CNN contains "cnn"
                                selectedLower.contains(sourceName) || // "CNN International" contains "cnn"
                                sourceName.split(" ")
                                    .any { it == selectedLower } || // Exact word match
                                selectedLower.split(" ")
                                    .any { it == sourceName } // Reverse word match
                    }
                }
            }

            // Check if we have more pages
            val hasMore = response.body()?.totalResults?.let { total ->
                total > page * pageSize
            } ?: false

            LoadResult.Page(
                data = articles.take(pageSize),
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (hasMore && articles.isNotEmpty()) page + 1 else null
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}