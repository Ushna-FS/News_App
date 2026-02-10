package com.example.newsapp.data.repository

import com.example.newsapp.data.local.BookmarkDao
import com.example.newsapp.data.local.BookmarkedArticle
import com.example.newsapp.data.local.toBookmarkedArticle
import com.example.newsapp.data.models.Article
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject

class BookmarkRepository @Inject constructor(
    private val bookmarkDao: BookmarkDao
) {
    private val _bookmarkUpdates = MutableSharedFlow<String>()
    val bookmarkUpdates: SharedFlow<String> = _bookmarkUpdates.asSharedFlow()

    suspend fun addBookmark(article: Article) {
        bookmarkDao.insertBookmark(article.toBookmarkedArticle())
        _bookmarkUpdates.emit(article.url) // url is not nullable, so no need for ?:
    }

    suspend fun removeBookmark(article: Article) {
        val bookmarkedArticle = bookmarkDao.getBookmarkByUrl(article.url)
        bookmarkedArticle?.let {
            bookmarkDao.deleteBookmark(it)
            _bookmarkUpdates.emit(article.url)
        }
    }

    // Add this overloaded function that takes just the URL string
    suspend fun removeBookmark(url: String) {
        val bookmarkedArticle = bookmarkDao.getBookmarkByUrl(url)
        bookmarkedArticle?.let {
            bookmarkDao.deleteBookmark(it)
            _bookmarkUpdates.emit(url)
        }
    }

    suspend fun isBookmarked(url: String): Boolean {
        return bookmarkDao.isArticleBookmarked(url) > 0
    }

    fun getAllBookmarks(): Flow<List<BookmarkedArticle>> {
        return bookmarkDao.getAllBookmarks()
    }
}