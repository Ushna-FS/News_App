package com.example.shared.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BookmarkDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(article: BookmarkedArticle)

    @Query("SELECT * FROM bookmarked_articles WHERE url = :url")
    suspend fun getBookmarkByUrl(url: String): BookmarkedArticle?

    @Query("SELECT * FROM bookmarked_articles WHERE userId = :userId ORDER BY bookmarkedAt DESC")
    fun getAllBookmarks(userId: String): Flow<List<BookmarkedArticle>>

    @Query("SELECT COUNT(*) FROM bookmarked_articles WHERE url = :url AND userId = :userId")
    suspend fun isArticleBookmarked(url: String, userId: String): Int

    @Query("DELETE FROM bookmarked_articles WHERE url = :url AND userId = :userId")
    suspend fun deleteBookmarkByUrl(url: String, userId: String)

    @Query("SELECT * FROM bookmarked_articles WHERE userId = :userId AND isSynced = 0")
    suspend fun getUnsyncedBookmarks(userId: String): List<BookmarkedArticle>

    @Query("UPDATE bookmarked_articles SET isSynced = 1 WHERE url = :url")
    suspend fun markAsSynced(url: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmarks(bookmarks: List<BookmarkedArticle>)

    @Query("DELETE FROM bookmarked_articles")
    suspend fun clearBookmarks()

    @Query("SELECT * FROM bookmarked_articles WHERE userId = :userId")
    suspend fun getAllBookmarksOnce(userId: String): List<BookmarkedArticle>

    @Query("DELETE FROM bookmarked_articles WHERE userId = :userId")
    suspend fun clearBookmarks(userId: String)
}