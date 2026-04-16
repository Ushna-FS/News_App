package com.example.shared.data.di


import com.example.newsapp.BuildKonfig
import com.example.shared.data.config.ApiKeyManager
import org.koin.dsl.module


fun apiModule() = module {

    single {
        ApiKeyManager(defaultKey = BuildKonfig.API_KEY)
    }
}