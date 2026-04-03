package com.example.shared.data.di

import com.example.shared.data.local.NewsDatabase
import com.example.shared.data.local.createDatabase
import org.koin.dsl.module

val databaseModule = module {

    single {
        createDatabase(get())
    }

    single {
        get<NewsDatabase>().bookmarkDao()
    }
}