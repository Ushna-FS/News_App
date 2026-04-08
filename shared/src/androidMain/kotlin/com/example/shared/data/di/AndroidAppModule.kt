package com.example.shared.data.di

import androidx.room.Room
import androidx.work.WorkManager
import com.example.shared.data.local.NewsDatabase
import com.example.shared.data.local.BookmarkDao
import com.example.shared.utils.DateFormatter
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val androidModule = module {

    single {
        Room.databaseBuilder(
            androidContext(),
            NewsDatabase::class.java,
            NewsDatabase.DATABASE_NAME
        ).fallbackToDestructiveMigration(false).build()
    }

    single<BookmarkDao> {
        get<NewsDatabase>().bookmarkDao()
    }

    single {
        WorkManager.getInstance(androidContext())
    }

    single {
        DateFormatter()
    }
}