package com.example.shared.data.local

import android.content.Context
import androidx.room.Room

fun createDatabase(context: Context): NewsDatabase {

    return Room.databaseBuilder<NewsDatabase>(
        context = context,
        name = NewsDatabase.DATABASE_NAME
    )
        .addMigrations(
            MIGRATION_1_2,
            MIGRATION_2_3,
            MIGRATION_3_4
        )
        .build()
}