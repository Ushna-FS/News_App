package com.example.shared.data.di

import com.example.shared.utils.NetworkMonitor
import org.koin.core.module.Module
import org.koin.dsl.module

actual val networkModule: Module = module {

    single { NetworkMonitor() }

}