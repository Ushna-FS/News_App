package com.example.shared.data.di

import com.example.shared.data.api.NewsApiService
import com.example.shared.data.network.createHttpClient
import org.koin.core.module.Module
import org.koin.dsl.module


val networkModule: Module = module {

    single { createHttpClient() }

    single {
        NewsApiService(
            client = get(),
            apiKey = get()
        )
    }
}