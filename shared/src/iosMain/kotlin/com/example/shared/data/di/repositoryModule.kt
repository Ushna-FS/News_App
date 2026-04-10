package com.example.shared.data.di

import com.example.shared.data.repository.BookmarkRepository
import com.example.shared.data.repository.BookmarkRepositoryImpl
import com.example.shared.data.repository.NewsRepository
import com.example.shared.data.sync.BookmarkSyncScheduler
import org.koin.dsl.module

actual val repositoryModule = module {

    // shared NewsRepository
    single {
        NewsRepository(
            apiService = get(),
            dateFormatter = get()
        )
    }
    single {
        BookmarkSyncScheduler()
    }

    single<BookmarkRepository> {
        BookmarkRepositoryImpl(
            bookmarkDao = get(),
            scheduler = get()
        )
    }
}