package com.example.shared.data.local

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import platform.Foundation.NSHomeDirectory

fun createDatabase(): NewsDatabase {

    val dbPath = NSHomeDirectory() + "/news_database.db"

    return Room.databaseBuilder<NewsDatabase>(
        name = dbPath
    )
        .setDriver(BundledSQLiteDriver())
        .addMigrations(
            MIGRATION_1_2,
            MIGRATION_2_3,
            MIGRATION_3_4
        )
        .build()
}