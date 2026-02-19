package com.example.newsapp.di


import com.example.newsapp.utils.DateFormatter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UtilsModule {

    @Provides
    @Singleton
    fun provideDateFormatter(): DateFormatter {
        return DateFormatter()
    }
}