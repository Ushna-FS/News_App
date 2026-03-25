package com.example.newsapp.di

import android.content.Context
import androidx.room.Room
import androidx.work.WorkManager
import com.example.newsapp.BuildConfig
import com.example.newsapp.data.local.BookmarkDao
import com.example.newsapp.data.local.NewsDatabase
import com.example.shared.data.api.NewsApiService
import com.example.shared.data.network.createHttpClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideHttpClient(): HttpClient {
        return createHttpClient()
    }

    @Provides
    @Singleton
    fun provideNewsApiService(
        client: HttpClient
    ): NewsApiService {
        return NewsApiService(client, BuildConfig.NEWS_API_KEY)
    }

    @Provides
    @Singleton
    fun provideApiKey(): String {
        return BuildConfig.NEWS_API_KEY
    }

    @Singleton
    @Provides
    fun provideNewsDatabase(@ApplicationContext context: Context): NewsDatabase {
        return Room.databaseBuilder(
            context, NewsDatabase::class.java, NewsDatabase.DATABASE_NAME
        ).fallbackToDestructiveMigration(false).build()
    }

    @Singleton
    @Provides
    fun provideBookmarkDao(database: NewsDatabase): BookmarkDao {
        return database.bookmarkDao()
    }
    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager {
        return WorkManager.getInstance(context)
    }
}