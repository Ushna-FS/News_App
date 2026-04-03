package com.example.shared.data.di

import com.example.shared.data.repository.BookmarkRepository
import com.example.shared.data.repository.BookmarkRepositoryImpl
import com.example.shared.data.repository.NewsRepository
import org.koin.dsl.module

actual val repositoryModule = module {

    single {
        NewsRepository(
            apiService = get(),
            dateFormatter = get()
        )
    }

    single<BookmarkRepository> {
        BookmarkRepositoryImpl(
            bookmarkDao = get(),
            workManager = get()
        )
    }
}