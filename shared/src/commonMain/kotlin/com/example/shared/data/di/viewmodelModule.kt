@file:Suppress("DEPRECATION")

package com.example.shared.data.di

import com.example.shared.viewmodels.AppViewModel
import com.example.shared.viewmodels.ArticleDetailViewModel
import com.example.shared.viewmodels.AuthViewModel
import com.example.shared.viewmodels.NetworkViewModel
import com.example.shared.viewmodels.NewsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {

    viewModel {
        NewsViewModel(
            repository = get(),
            bookmarkRepository = get()
        )
    }
    viewModel { AuthViewModel(get(), get()) }
    viewModel { ArticleDetailViewModel(get()) }
    viewModel { NetworkViewModel(get()) }
    viewModel { AppViewModel(get()) }
}