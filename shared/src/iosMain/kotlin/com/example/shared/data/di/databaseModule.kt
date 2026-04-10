package com.example.shared.data.di


import com.example.shared.data.local.NewsDatabase
import com.example.shared.data.local.createDatabase
import org.koin.core.module.Module
import org.koin.dsl.module

val databaseModule : Module = module {

    single {
        createDatabase()
    }

    single {
        get<NewsDatabase>().bookmarkDao()
    }
}