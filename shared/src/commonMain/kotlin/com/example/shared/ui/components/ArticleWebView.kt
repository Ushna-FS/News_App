package com.example.shared.ui.components

import androidx.compose.runtime.Composable


@Composable
expect fun ArticleWebView(
    url: String,
    onPageLoaded: () -> Unit,
    onError: () -> Unit
)