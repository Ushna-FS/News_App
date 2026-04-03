package com.example.shared.data.local

import android.content.Context
import androidx.room.Room

fun createDatabase(context: Context): NewsDatabase =
    Room.databaseBuilder<NewsDatabase>(
        context = context,
        name = NewsDatabase.DATABASE_NAME
    )
        .build()