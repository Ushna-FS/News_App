package com.example.shared.data.di


import org.koin.dsl.module


fun apiModule(apiKey: String) = module {

    single { apiKey }

}