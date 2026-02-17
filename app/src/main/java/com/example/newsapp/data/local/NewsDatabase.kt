package com.example.newsapp.data.local


import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [BookmarkedArticle::class],
    version = 3,
    exportSchema = false
)
abstract class NewsDatabase : RoomDatabase() {
    abstract fun bookmarkDao(): BookmarkDao

    companion object {
        const val DATABASE_NAME = "news_database"
    }
}