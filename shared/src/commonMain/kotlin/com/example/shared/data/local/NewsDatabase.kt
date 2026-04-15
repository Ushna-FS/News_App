package com.example.shared.data.local

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration

@Database(
    entities = [BookmarkedArticle::class],
    version = 5,
    exportSchema = true
)
abstract class NewsDatabase : RoomDatabase() {
    abstract fun bookmarkDao(): BookmarkDao

    companion object {
        const val DATABASE_NAME = "news_database"
    }
}