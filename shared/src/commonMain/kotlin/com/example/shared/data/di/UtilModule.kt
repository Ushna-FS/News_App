package com.example.shared.data.di

import com.example.shared.utils.DateFormatter
import org.koin.dsl.module

val utilsModule = module {

    single {
        DateFormatter()
    }
}