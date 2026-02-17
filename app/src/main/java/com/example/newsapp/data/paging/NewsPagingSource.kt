package com.example.newsapp.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.newsapp.data.api.ApiService
import retrofit2.HttpException
import java.io.IOException

sealed class NewsType {
    object Business : NewsType()
    object TechCrunch : NewsType()
    data class Search(val query: String) : NewsType()
}

class NewsPagingSource(
    private val apiService: ApiService,
    private val newsType: NewsType,
    private val pageSize: Int = 5
) : PagingSource<Int, com.example.newsapp.data.models.Article>() {

    override fun getRefreshKey(state: PagingState<Int, com.example.newsapp.data.models.Article>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, com.example.newsapp.data.models.Article> {
        return try {
            val page = params.key ?: 1
            val pageSize = params.loadSize.coerceAtMost(this.pageSize)

            val response = when (newsType) {
                is NewsType.Business -> apiService.getTopHeadlines(
                    page = page,
                    pageSize = pageSize
                )
                is NewsType.TechCrunch -> apiService.getTechCrunchHeadlines(
                    page = page,
                    pageSize = pageSize
                )
                is NewsType.Search -> apiService.searchNews(
                    query = newsType.query,
                    page = page,
                    pageSize = pageSize
                )
            }

            if (response.isSuccessful) {
                val articles = response.body()?.articles ?: emptyList()
                val totalResults = response.body()?.totalResults ?: 0

                // Calculate if there are more pages
                val hasMore = page * pageSize < totalResults
                val nextPage = if (hasMore && articles.isNotEmpty()) page + 1 else null

                LoadResult.Page(
                    data = articles,
                    prevKey = if (page == 1) null else page - 1,
                    nextKey = nextPage
                )
            } else {
                LoadResult.Error(HttpException(response))
            }
        } catch (e: IOException) {
            LoadResult.Error(e)
        } catch (e: HttpException) {
            LoadResult.Error(e)
        }
    }
}