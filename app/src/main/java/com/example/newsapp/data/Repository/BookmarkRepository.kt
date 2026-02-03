package com.example.newsapp.data.Repository


import com.example.newsapp.data.local.BookmarkDao
import com.example.newsapp.data.local.BookmarkedArticle
import com.example.newsapp.data.local.toBookmarkedArticle
import com.example.newsapp.data.models.Article
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class BookmarkRepository @Inject constructor(
    private val bookmarkDao: BookmarkDao
) {
    suspend fun addBookmark(article: Article) {
        bookmarkDao.insertBookmark(article.toBookmarkedArticle())
    }

    suspend fun removeBookmark(article: Article) {
        val bookmarkedArticle = bookmarkDao.getBookmarkByUrl(article.url ?: "")
        bookmarkedArticle?.let {
            bookmarkDao.deleteBookmark(it)
        }
    }

    suspend fun isBookmarked(url: String): Boolean {
        return bookmarkDao.isArticleBookmarked(url) > 0
    }

    fun getAllBookmarks(): Flow<List<BookmarkedArticle>> {
        return bookmarkDao.getAllBookmarks()
    }
}