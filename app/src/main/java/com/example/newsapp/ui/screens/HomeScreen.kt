package com.example.newsapp.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.newsapp.data.models.Article
import com.example.newsapp.viewmodels.NewsViewModel
import com.example.newsapp.ui.components.HomeArticleItem
import com.example.newsapp.ui.components.EmptyState
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.newsapp.data.ArticleCategoryMapper
import com.example.newsapp.ui.components.CategoryChips
import com.example.newsapp.utils.DateFormatter

@Composable
fun HomeScreen(
    viewModel: NewsViewModel = hiltViewModel(), onArticleClick: (Article) -> Unit
) {
    val articles = viewModel.homeNewsPagingData.collectAsLazyPagingItems()

    val bookmarkedUrls by viewModel.getAllBookmarkedUrls()
        .collectAsState(initial = emptySet())

    val context = LocalContext.current
    val currentContext by rememberUpdatedState(context)

    LaunchedEffect(viewModel.uiMessage) {
        viewModel.uiMessage.collect { resId ->
            Toast.makeText(
                currentContext, currentContext.getString(resId), Toast.LENGTH_SHORT
            ).show()
        }
    }

    HomeScreenContent(
        articles = articles,
        bookmarkedUrls = bookmarkedUrls,
        onArticleClick = onArticleClick,
        onBookmarkClick = { viewModel.toggleBookmark(it) },
                onCategorySelected = { viewModel.setHomeCategory(it) }
    )
}

//stateless
@Composable
fun HomeScreenContent(
    articles: LazyPagingItems<Article>,
    bookmarkedUrls: Set<String>,
    onArticleClick: (Article) -> Unit,
    onBookmarkClick: (Article) -> Unit,
    onCategorySelected: (String) -> Unit
) {

    val dateFormatter = remember { DateFormatter() }

    var selectedCategory by remember { mutableStateOf("All") }

    val listState = rememberLazyListState()

    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {

            Text(
                text = "Hello User",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(top = 16.dp)
            )

            Text(
                text = "Stay updated with latest news",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
            )

            CategoryChips(
                categories = ArticleCategoryMapper.categories,
                selectedCategory = selectedCategory,
                onCategorySelected = { category ->

                    selectedCategory = category
                    onCategorySelected(category)

                }
            )

            when (articles.loadState.refresh) {

                is LoadState.Loading -> {

                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is LoadState.Error -> {

                    EmptyState(
                        message = "Error loading articles",
                        onRetry = { articles.retry() }
                    )
                }

                else -> {

                    LazyColumn(
                        state = listState
                    ) {

                        items(
                            count = articles.itemCount,
                            key = { index -> articles[index]?.url ?: index }
                        ) { index ->

                            val article = articles[index]

                            article?.let {

                                val bookmarked =
                                    bookmarkedUrls.contains(it.url)

                                HomeArticleItem(
                                    article = it,
                                    isBookmarked = bookmarked,
                                    onClick = { onArticleClick(it) },
                                    onBookmarkClick = { onBookmarkClick(it) },
                                    dateFormatter = dateFormatter
                                )
                            }
                        }

                        item {

                            when (articles.loadState.append) {

                                is LoadState.Loading -> {

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator()
                                    }
                                }

                                is LoadState.Error -> {

                                    EmptyState(
                                        message = "Error loading more articles",
                                        onRetry = { articles.retry() }
                                    )
                                }

                                else -> {}
                            }
                        }
                    }
                }
            }
        }
    }
}