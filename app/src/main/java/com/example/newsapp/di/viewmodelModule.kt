@file:Suppress("DEPRECATION")

package com.example.newsapp.di

import com.example.newsapp.viewmodels.ArticleDetailViewModel
import com.example.newsapp.viewmodels.AuthViewModel
import com.example.newsapp.viewmodels.NewsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {

    viewModel {
        NewsViewModel(
            repository = get(),
            bookmarkRepository = get()
        )
    }
    viewModel { AuthViewModel(get()) }
    viewModel { ArticleDetailViewModel(get()) }
}