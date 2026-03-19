package com.example.newsapp.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.newsapp.data.models.Article
import com.example.newsapp.ui.components.ArticleWebView
import com.example.newsapp.viewmodels.ArticleDetailViewModel
import com.example.newsapp.viewmodels.NewsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleDetailContent(
    article: Article,
    isBookmarked: Boolean,
    isLoading: Boolean,
    showError: Boolean,
    onBackClick: () -> Unit,
    onBookmarkClick: () -> Unit,
    onPageLoaded: () -> Unit,
    onError: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = article.title,
                        maxLines = 1
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onBookmarkClick) {
                        Icon(
                            imageVector = if (isBookmarked)
                                Icons.Filled.Bookmark
                            else
                                Icons.Outlined.BookmarkBorder,
                            contentDescription = "Bookmark",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Blue,
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            ArticleWebView(
                url = article.url,
                onPageLoaded = onPageLoaded,
                onError = onError
            )

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            if (showError) {
                Text(
                    text = "Failed to load article",
                    color = Color.Red,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

@Composable
fun ArticleDetailScreen(
    article: Article,
    newsViewModel: NewsViewModel = hiltViewModel(),
    viewModel: ArticleDetailViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {

    var isLoading by remember { mutableStateOf(true) }
    var showError by remember { mutableStateOf(false) }

    val bookmarkFlow = remember(article.url) {
        newsViewModel.isArticleBookmarked(article.url)
    }

    val isBookmarked by bookmarkFlow.collectAsState(initial = false)

    LaunchedEffect(article) {
        viewModel.setArticle(article)
    }

    ArticleDetailContent(
        article = article,
        isBookmarked = isBookmarked,
        isLoading = isLoading,
        showError = showError,
        onBackClick = onBackClick,
        onBookmarkClick = {
            newsViewModel.toggleBookmark(article)
        },
        onPageLoaded = {
            isLoading = false
        },
        onError = {
            showError = true
            isLoading = false
        }
    )
}