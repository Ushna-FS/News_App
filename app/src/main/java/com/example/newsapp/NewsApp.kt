package com.example.newsapp

import android.app.Application
import androidx.work.Configuration
import com.example.newsapp.di.androidModule
import com.example.newsapp.di.utilsModule
import com.example.newsapp.di.viewModelModule
import com.example.shared.data.di.apiModule
import com.example.shared.data.di.databaseModule
import com.example.shared.data.di.httpModule
import com.example.shared.data.di.networkModule
import com.example.shared.data.di.repositoryModule
import com.example.shared.data.di.workerModule
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.context.startKoin

class NewsApp : Application(), Configuration.Provider {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@NewsApp)
            workManagerFactory()
            modules(
                apiModule(BuildConfig.NEWS_API_KEY),
                httpModule,
                networkModule,
                androidModule,
                repositoryModule,
                databaseModule,
                utilsModule,
                viewModelModule,
                workerModule
            )
        }
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().build()
}