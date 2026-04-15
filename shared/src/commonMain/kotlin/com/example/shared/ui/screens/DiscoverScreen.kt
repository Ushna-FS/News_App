package com.example.shared.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import app.cash.paging.compose.LazyPagingItems
import app.cash.paging.compose.collectAsLazyPagingItems
import com.example.shared.NewsAppTheme
import com.example.shared.data.models.Article
import com.example.shared.data.models.NetworkError
import com.example.shared.data.repository.SortType
import com.example.shared.ui.components.DiscoverNewsCard
import com.example.shared.ui.components.EmptyState
import com.example.shared.ui.components.FilterPanel
import com.example.shared.ui.components.SortMenuDialog
import com.example.shared.utils.mapErrorToMessage
import com.example.shared.viewmodels.NewsViewModel
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.delay
import me.sample.library.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoverScreen(
    viewModel: NewsViewModel = koinViewModel(),
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
    val snackbarHostState = remember { SnackbarHostState() }

    val uiMessage by viewModel.uiMessage.collectAsState(null)

    LaunchedEffect(Unit) {
        val userId = Firebase.auth.currentUser?.uid
        if (userId != null) {
            viewModel.setCurrentUser(userId)
            viewModel.startBookmarkSync(userId)
        }
    }
    // Update local search when ViewModel search changes
    LaunchedEffect(searchQuery) {
        localSearchText = searchQuery
    }

    LaunchedEffect(localSearchText) {
        delay(800)
        if (localSearchText.isNotBlank() && localSearchText != searchQuery) {
            viewModel.searchNews(localSearchText)
        }
    }

    uiMessage?.let { message ->

        val messageText = stringResource(message)

        LaunchedEffect(message) {
            snackbarHostState.showSnackbar(messageText)
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(8.dp))
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
                modifier = Modifier.weight(1f)
            )
        }
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

                    pagingItems.refresh()

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
            .padding(horizontal = 16.dp, vertical = 3.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )

            Spacer(Modifier.width(6.dp))

            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart
            ) {

                if (text.isEmpty()) {
                    Text(
                        stringResource(Res.string.search_news),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                    )
                }

                BasicTextField(
                    value = text,
                    onValueChange = onTextChange,
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (text.isNotEmpty()) {
                IconButton(
                    onClick = onClear,
                    modifier = Modifier.size(35.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
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
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    val bookmarkedUrls by viewModel.getAllBookmarkedUrls()
        .collectAsState(initial = emptySet())
    val searchQuery by viewModel.searchQuery.collectAsState()


    LazyColumn(
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
            val errorState = pagingItems.loadState.refresh as LoadState.Error

            val error = errorState.error
                .let { it as? NetworkError ?: NetworkError.Unknown(it) }

            item {
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
                { index ->
                    val article = pagingItems.peek(index)
                    "${article?.url}_${article?.publishedAt}_$index"
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
            //end reached
            else if (pagingItems.loadState.append is LoadState.NotLoading &&
                pagingItems.loadState.append.endOfPaginationReached &&
                pagingItems.itemCount > 0
            ) {
                item {
                    println("UI DEBUG -> End reached triggered. itemCount=${pagingItems.itemCount}")

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(stringResource(Res.string.pagination_end))
                    }
                }
            } else if (pagingItems.loadState.append is LoadState.Error) {
                val errorState = pagingItems.loadState.append as LoadState.Error

                val error = errorState.error
                    .let { it as? NetworkError ?: NetworkError.Unknown(it) }

                item {
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
                            stringResource(Res.string.no_results_found_for, searchQuery)
                        else (stringResource(Res.string.no_articles_found))
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
        searchQuery.isNotEmpty() -> "Search results for \"$searchQuery\""
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
            colors = if (hasActiveFilters) {
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