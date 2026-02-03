package com.example.newsapp.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BookmarkDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(article: BookmarkedArticle)

    @Delete
    suspend fun deleteBookmark(article: BookmarkedArticle)

    @Query("SELECT * FROM bookmarked_articles ORDER BY bookmarkedAt DESC")
    fun getAllBookmarks(): Flow<List<BookmarkedArticle>>

    @Query("SELECT * FROM bookmarked_articles WHERE url = :url")
    suspend fun getBookmarkByUrl(url: String): BookmarkedArticle?

    @Query("SELECT COUNT(*) FROM bookmarked_articles WHERE url = :url")
    suspend fun isArticleBookmarked(url: String): Int
}