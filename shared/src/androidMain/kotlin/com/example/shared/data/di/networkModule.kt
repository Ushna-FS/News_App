package com.example.shared.data.di


import com.example.shared.utils.NetworkMonitor
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

actual val networkModule = module {

    single { NetworkMonitor(androidContext()) }
}