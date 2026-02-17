package com.example.newsapp.DI

import android.content.Context
import androidx.room.Room
import com.example.newsapp.data.Repository.BookmarkRepository
import com.example.newsapp.data.Repository.NewsRepository
import com.example.newsapp.data.api.ApiService
import com.example.newsapp.data.local.BookmarkDao
import com.example.newsapp.data.local.NewsDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // Network dependencies
    @Singleton
    @Provides
    fun provideApiService(): ApiService {
        return Retrofit.Builder()
            .baseUrl("https://newsapi.org/v2/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    @Singleton
    @Provides
    fun provideNewsRepository(apiService: ApiService): NewsRepository {
        return NewsRepository(apiService)
    }

    // Database dependencies
    @Singleton
    @Provides
    fun provideNewsDatabase(@ApplicationContext context: Context): NewsDatabase {
        return Room.databaseBuilder(
            context,
            NewsDatabase::class.java,
            NewsDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration() // Optional: handle database migrations
            .build()
    }

    @Singleton
    @Provides
    fun provideBookmarkDao(database: NewsDatabase): BookmarkDao {
        return database.bookmarkDao()
    }

    // Repository dependencies
    @Singleton
    @Provides
    fun provideBookmarkRepository(bookmarkDao: BookmarkDao): BookmarkRepository {
        return BookmarkRepository(bookmarkDao)
    }
}