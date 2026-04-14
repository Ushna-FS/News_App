package com.example.shared.data.repository

import com.example.shared.data.models.Article
import com.example.shared.data.local.BookmarkedArticle
import kotlinx.coroutines.flow.Flow

interface BookmarkRepository {
    val bookmarkUpdates: Flow<String>
    suspend fun addBookmark(article: Article, userId: String)
    suspend fun removeBookmark(url: String, userId: String)

    suspend fun isBookmarked(url: String, userId: String): Boolean

    fun getAllBookmarks(userId: String): Flow<List<BookmarkedArticle>>
    suspend fun fetchBookmarksFromFirebase(userId: String)
    fun startRealtimeSync(userId: String)
    fun stopRealtimeSync()
}