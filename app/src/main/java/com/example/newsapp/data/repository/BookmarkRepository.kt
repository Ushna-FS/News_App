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

    suspend fun removeBookmark(article: Article) {
        bookmarkDao.deleteBookmarkByUrl(article.url)
        _bookmarkUpdates.emit(article.url)
    }

    suspend fun removeBookmark(url: String) {
        bookmarkDao.deleteBookmarkByUrl(url)
        _bookmarkUpdates.emit(url)
    }

    suspend fun addBookmark(article: Article) {
        bookmarkDao.insertBookmark(article.toBookmarkedArticle())
        _bookmarkUpdates.emit(article.url)
    }

    suspend fun isBookmarked(url: String): Boolean {
        return bookmarkDao.isArticleBookmarked(url) > 0
    }

    fun getAllBookmarks(): Flow<List<BookmarkedArticle>> {
        return bookmarkDao.getAllBookmarks()
    }
}