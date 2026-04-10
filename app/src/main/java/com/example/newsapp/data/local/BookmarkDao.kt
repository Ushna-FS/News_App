package com.example.newsapp.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BookmarkDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(article: BookmarkedArticle)

    @Query("SELECT * FROM bookmarked_articles ORDER BY bookmarkedAt DESC")
    fun getAllBookmarks(): Flow<List<BookmarkedArticle>>

    @Query("SELECT * FROM bookmarked_articles WHERE url = :url")
    suspend fun getBookmarkByUrl(url: String): BookmarkedArticle?

    @Query("SELECT COUNT(*) FROM bookmarked_articles WHERE url = :url")
    suspend fun isArticleBookmarked(url: String): Int

    @Query("DELETE FROM bookmarked_articles WHERE url = :url")
    suspend fun deleteBookmarkByUrl(url: String)

    @Query("SELECT * FROM bookmarked_articles WHERE isSynced = 0")
    suspend fun getUnsyncedBookmarks(): List<BookmarkedArticle>

    @Query("UPDATE bookmarked_articles SET isSynced = 1 WHERE url = :url")
    suspend fun markAsSynced(url: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmarks(bookmarks: List<BookmarkedArticle>)

    @Query("DELETE FROM bookmarked_articles")
    suspend fun clearBookmarks()
    @Query("SELECT * FROM bookmarked_articles")
    suspend fun getAllBookmarksOnce(): List<BookmarkedArticle>
}