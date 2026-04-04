package com.example.newsapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.newsapp.NewsAppTheme
import com.example.newsapp.R
import com.example.newsapp.data.api.mapErrorToMessage
import com.example.newsapp.data.models.Article
import com.example.newsapp.data.models.NetworkError
import com.example.newsapp.data.repository.SortType
import com.example.newsapp.ui.components.DiscoverNewsCard
import com.example.newsapp.ui.components.EmptyState
import com.example.newsapp.ui.components.FilterPanel
import com.example.newsapp.ui.components.SortMenuDialog
import com.example.newsapp.viewmodels.NewsViewModel
import kotlinx.coroutines.delay
import retrofit2.HttpException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoverScreen(
    viewModel: NewsViewModel = hiltViewModel(),
    onArticleClick: (Article) -> Unit
) {

    // State
    val pagingItems = viewModel.newsPagingData.collectAsLazyPagingItems()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val sortType by viewModel.sortType.collectAsState()

    val hasUserSelectedSort by viewModel.hasUserSelectedSort.collectAsState()
    var showFilterSheet by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }

    // Local search text with debounce
    var localSearchText by remember { mutableStateOf(searchQuery) }

    // Update local search when ViewModel search changes
    LaunchedEffect(searchQuery) {
        localSearchText = searchQuery
    }

    // Debounce search
    LaunchedEffect(localSearchText) {
        delay(500)
        if (localSearchText != searchQuery) {
            viewModel.searchNews(localSearchText)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Search Bar
        DiscoverSearchBar(
            text = localSearchText,
            onTextChange = { localSearchText = it },
            onClear = {
                localSearchText = ""
                viewModel.clearSearch()
            }
        )

        // Filter/Sort Row
        FilterSortRow(
            hasActiveFilters = viewModel.hasActiveFilters(),
            isSearching = searchQuery.isNotEmpty(),
            hasUserSelectedSort = hasUserSelectedSort,
            sortType = sortType,
            onFilterClick = { showFilterSheet = true },
            onSortClick = { showSortMenu = true }
        )

        // Filter Summary
        FilterSummary(
            viewModel = viewModel,
            itemCount = pagingItems.itemCount
        )

        // News List
        DiscoverNewsList(
            pagingItems = pagingItems,
            viewModel = viewModel,
            onArticleClick = onArticleClick,
            modifier = Modifier.weight(1f),
            sortType = sortType
        )
    }

    // Filter Bottom Sheet
    if (showFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            FilterPanel(
                viewModel = viewModel,
                onApply = {
                    showFilterSheet = false

//                    pagingItems.refresh()

                },
                onClose = { showFilterSheet = false }
            )
        }
    }

    // Sort Menu Dialog
    if (showSortMenu) {
        SortMenuDialog(
            currentSortType = sortType,
            hasUserSelected = hasUserSelectedSort,
            onSortSelected = { type, userSelected ->
                if (userSelected) {
                    viewModel.setSortType(type)
                } else {
                    viewModel.resetSort()
                }
                pagingItems.refresh()
                showSortMenu = false
            },
            onDismiss = { showSortMenu = false }
        )
    }
}

@Composable
fun DiscoverSearchBar(
    text: String,
    onTextChange: (String) -> Unit,
    onClear: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )

            TextField(
                value = text,
                onValueChange = onTextChange,
                placeholder = { Text(stringResource(R.string.search_news)) },
                modifier = Modifier.weight(1f),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )

            if (text.isNotEmpty()) {
                IconButton(onClick = onClear) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = (stringResource(R.string.clear_search))
                    )
                }
            }
        }
    }
}

@Composable
fun DiscoverNewsList(
    pagingItems: LazyPagingItems<Article>,
    viewModel: NewsViewModel,
    onArticleClick: (Article) -> Unit,
    modifier: Modifier = Modifier,
    sortType: SortType
) {
    val listState = rememberLazyListState()
    val bookmarkedUrls by viewModel.getAllBookmarkedUrls()
        .collectAsState(initial = emptySet())
    val searchQuery by viewModel.searchQuery.collectAsState()

    // At the top of DiscoverNewsList function, before LazyColumn
    LaunchedEffect(sortType) {
        // When switching to oldest-first, ensure we scroll to top to show oldest articles
        if (sortType == SortType.OLDEST_FIRST && pagingItems.itemCount > 0) {
            delay(100) // Small delay to ensure items are loaded
            listState.scrollToItem(0)
        }
    }

    LazyColumn(
//        reverseLayout = (sortType == SortType.OLDEST_FIRST),
        state = listState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),

        ) {
        // Check for refresh loading state
        if (pagingItems.loadState.refresh is LoadState.Loading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
        // Check for refresh error state
        else if (pagingItems.loadState.refresh is LoadState.Error) {
            item {
                val rawError = (pagingItems.loadState.refresh as LoadState.Error).error
                val error = when (rawError) {
                    is NetworkError -> rawError
                    is HttpException -> when (rawError.code()) {
                        401 -> NetworkError.Unauthorized()
                        404 -> NetworkError.NotFound()
                        429 -> NetworkError.RateLimit()
                        in 500..599 -> NetworkError.ServerError()
                        else -> NetworkError.Unknown(rawError)
                    }

                    else -> NetworkError.Unknown(rawError)
                }

                EmptyState(
                    error = error,
                    message = stringResource(mapErrorToMessage(error)),
                    onRetry = { pagingItems.retry() }
                )

            }
        }
        // Show items if any exist
        else if (pagingItems.itemCount > 0) {
            items(
                count = pagingItems.itemCount,
                key = { index ->
                    val article = pagingItems[index]
                    "${article?.url}_${index}"
                }
            ) { index ->

                val article = pagingItems[index]

                if (article != null) {
                    viewModel.extractSourceFromArticle(article)

                    val isBookmarked = bookmarkedUrls.contains(article.url)

                    DiscoverNewsCard(
                        article = article,
                        isBookmarked = isBookmarked,
                        onClick = { onArticleClick(article) },
                        onBookmarkClick = { viewModel.toggleBookmark(article) }
                    )
                }
            }
            // Pagination loading
            if (pagingItems.loadState.append is LoadState.Loading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }

            // Pagination error
            if (pagingItems.loadState.append is LoadState.Error) {
                item {
                    val rawError = (pagingItems.loadState.append as LoadState.Error).error
                    val error = when (rawError) {
                        is NetworkError -> rawError
                        is HttpException -> when (rawError.code()) {
                            401 -> NetworkError.Unauthorized()
                            404 -> NetworkError.NotFound()
                            429 -> NetworkError.RateLimit()
                            in 500..599 -> NetworkError.ServerError()
                            else -> NetworkError.Unknown(rawError)
                        }

                        else -> NetworkError.Unknown(rawError)
                    }

                    EmptyState(
                        error = error,
                        message = stringResource(mapErrorToMessage(error)),
                        onRetry = { pagingItems.retry() }
                    )

                }
            }
        }
        // Empty state
        else {
            item {
                Box(
                    modifier = Modifier
                        .fillParentMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (searchQuery.isNotEmpty())
                            stringResource(R.string.no_results_found_for, searchQuery)
                        else (stringResource(R.string.no_articles_found))
                    )
                }
            }
        }
    }
}

@Composable
fun FilterSummary(
    viewModel: NewsViewModel,
    itemCount: Int
) {
    val (categories, sources, _) = viewModel.getFilterSummaryData()
    val searchQuery by viewModel.searchQuery.collectAsState()

    val summaryText = when {
        searchQuery.isNotEmpty() -> "Search: \"$searchQuery\""
        categories.isEmpty() && sources.isEmpty() -> "All News"
        categories.size == 1 && sources.isEmpty() -> "${categories.first()} News"
        categories.size > 1 && sources.isEmpty() -> "Selected Categories"
        sources.isNotEmpty() && categories.isEmpty() -> "${sources.size} Sources"
        else -> "${categories.joinToString(", ")} + ${sources.size} sources"
    }

    Text(
        text = if (itemCount > 0)
            "$summaryText • $itemCount articles"
        else
            summaryText,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
fun FilterSortRow(
    hasActiveFilters: Boolean,
    isSearching: Boolean,
    hasUserSelectedSort: Boolean,
    sortType: SortType,
    onFilterClick: () -> Unit,
    onSortClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Filter Button
        OutlinedButton(
            onClick = onFilterClick,
            modifier = Modifier.weight(1f),
            colors = if (hasActiveFilters || isSearching) {
                ButtonDefaults.outlinedButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                )
            } else {
                ButtonDefaults.outlinedButtonColors()
            }
        ) {
            Icon(
                Icons.Default.FilterList,
                contentDescription = null,
                modifier = Modifier.size(ButtonDefaults.IconSize)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = when {
                    hasActiveFilters && isSearching -> "Filter + Search"
                    hasActiveFilters -> "Filter"
                    isSearching -> "Searching"
                    else -> "Filter"
                }
            )
        }

        // Sort Button
        OutlinedButton(
            onClick = onSortClick,
            modifier = Modifier.weight(1f),
            colors = if (hasUserSelectedSort) {
                ButtonDefaults.outlinedButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                )
            } else {
                ButtonDefaults.outlinedButtonColors()
            }
        ) {
            Icon(
                Icons.AutoMirrored.Filled.Sort,
                contentDescription = null,
                modifier = Modifier.size(ButtonDefaults.IconSize)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (hasUserSelectedSort) {
                    when (sortType) {
                        SortType.NEWEST_FIRST -> "Newest"
                        SortType.OLDEST_FIRST -> "Oldest"
                    }
                } else {
                    "Sort"
                }
            )
        }
    }
}

@Composable
fun ErrorItem(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = message, color = MaterialTheme.colorScheme.error)
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DiscoverTopSectionPreview() {
    NewsAppTheme {

        Column {
            DiscoverSearchBar(
                text = "",
                onTextChange = {},
                onClear = {}
            )

            FilterSortRow(
                hasActiveFilters = true,
                isSearching = false,
                hasUserSelectedSort = true,
                sortType = SortType.NEWEST_FIRST,
                onFilterClick = {},
                onSortClick = {}
            )
        }
    }
}