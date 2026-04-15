package com.example.shared.data.paging

import app.cash.paging.*
import com.example.shared.data.api.NewsApiService
import com.example.shared.data.models.Article
import com.example.shared.data.repository.SortType
import com.example.shared.utils.ArticleCategoryMapper
import com.example.shared.utils.DateFormatter
import com.example.shared.utils.ErrorMapper

sealed class NewsType {
    object Business : NewsType()
    object TechCrunch : NewsType()
    object Everything : NewsType()
    data class NewsCategory(val name: String) : NewsType()
    data class Search(val query: String) : NewsType()
}

class NewsPagingSource(
    private val apiService: NewsApiService,
    private val newsType: NewsType
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

            val response = when (newsType) {
                is NewsType.Business -> apiService.getTopHeadlines(
                    country = "us",
                    category = "business",
                    page = page,
                    pageSize = pageSize
                )

                is NewsType.TechCrunch -> apiService.getTechCrunchHeadlines(
                    page = page,
                    pageSize = pageSize
                )

                is NewsType.Everything -> apiService.searchNews(
                    query = "general",
                    page = page,
                    pageSize = pageSize
                )

                is NewsType.Search -> apiService.searchNews(
                    query = newsType.query,
                    page = page,
                    pageSize = pageSize
                )

                is NewsType.NewsCategory -> apiService.getTopHeadlines(
                    country = "us",
                    category = newsType.name,
                    page = page,
                    pageSize = pageSize
                )
            }

            val rawArticles = response.articles
            val totalResults = response.totalResults

            val currentLoadedItems = page * pageSize

            val MAX_PAGE =  10 // News Api limitation(can't load more than 100 articles at once means max pages 10)

            val endReached = currentLoadedItems >= totalResults || page >= MAX_PAGE

            // added logs to debug actual issue
            println(
                ("""
            PagingDebug ->
            page: $page
            rawArticles.size: ${rawArticles.size}
            totalResults: $totalResults
            currentLoadedItems: $currentLoadedItems
            endReached: $endReached
            """.trimIndent())
            )
            LoadResult.Page(
                data = rawArticles,
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (endReached) null else page + 1
            )

        } catch (e: Throwable) {

            val error = ErrorMapper.mapToNetworkError(e)

            LoadResult.Error(error)
        }
    }
}


class FilteredCombinedNewsPagingSource(
    private val apiService: NewsApiService,
    private val categories: List<String> = emptyList(),
    private val sources: List<String> = emptyList(),
    private val sortType: SortType,
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
                pageSize = pageSize
            )

            val rawArticles = response.articles

            var articles = rawArticles.filter {
                it.title != "[Removed]"
            }

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

            val totalResults = response.totalResults
            val currentLoadedItems = page * pageSize

            val MAX_PAGE = 10

            val endReached = currentLoadedItems >= totalResults || page >= MAX_PAGE

            println(
                ("""
            PagingDebug ->
            page: $page
            rawArticles.size: ${rawArticles.size}
            totalResults: $totalResults
            currentLoadedItems: $currentLoadedItems
            endReached: $endReached
            """.trimIndent())
            )


            LoadResult.Page(
                data = articles,
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (endReached) null else page + 1
            )

        } catch (e: Throwable) {

            val error = ErrorMapper.mapToNetworkError(e)

            LoadResult.Error(error)
        }
    }
}