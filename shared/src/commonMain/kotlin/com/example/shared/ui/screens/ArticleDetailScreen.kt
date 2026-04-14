package com.example.shared.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.shared.data.models.Article
import com.example.shared.ui.components.ArticleWebView
import com.example.shared.viewmodels.ArticleDetailViewModel
import com.example.shared.viewmodels.NewsViewModel
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import me.sample.library.resources.Res
import me.sample.library.resources.article_load_error
import me.sample.library.resources.bookmark
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

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
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = article.title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.surface
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
                            contentDescription = stringResource(Res.string.bookmark),
                            tint = MaterialTheme.colorScheme.surface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
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
                    text = stringResource(Res.string.article_load_error),
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
    newsViewModel: NewsViewModel = koinViewModel(),
    viewModel: ArticleDetailViewModel = koinViewModel(),
    onBackClick: () -> Unit
) {

    var isLoading by remember { mutableStateOf(true) }
    var showError by remember { mutableStateOf(false) }

    val bookmarkedUrls by newsViewModel
        .getAllBookmarkedUrls()
        .collectAsState(initial = emptySet())

    val isBookmarked = bookmarkedUrls.contains(article.url)

    val uiMessage by newsViewModel.uiMessage.collectAsState(null)
    uiMessage?.let {
        Text(
            text = stringResource(it),
            color = Color.Red,
            modifier = Modifier.padding(8.dp)
        )
    }

    LaunchedEffect(article) {
        viewModel.setArticle(article)
    }

    LaunchedEffect(Unit) {
        val userId = Firebase.auth.currentUser?.uid
        if (userId != null) {
            newsViewModel.setCurrentUser(userId)
        }
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