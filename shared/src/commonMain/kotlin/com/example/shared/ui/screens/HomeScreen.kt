package com.example.shared.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WavingHand
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import app.cash.paging.compose.LazyPagingItems
import app.cash.paging.compose.collectAsLazyPagingItems
import com.example.shared.NewsAppTheme
import com.example.shared.data.mock.articleMock
import com.example.shared.data.models.Article
import com.example.shared.data.models.NetworkError
import com.example.shared.ui.components.CategoryChips
import com.example.shared.ui.components.EmptyState
import com.example.shared.ui.components.HomeArticleItem
import com.example.shared.utils.Category
import com.example.shared.utils.DateFormatter
import com.example.shared.utils.ErrorMapper.mapToNetworkError
import com.example.shared.utils.mapErrorToMessage
import com.example.shared.viewmodels.AuthViewModel
import com.example.shared.viewmodels.NewsViewModel
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import me.sample.library.resources.Res
import me.sample.library.resources.hello_user
import me.sample.library.resources.home_desc
import me.sample.library.resources.pagination_end
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeScreen(
    viewModel: NewsViewModel = koinViewModel(),
    authViewModel: AuthViewModel = koinViewModel(),
    onArticleClick: (Article) -> Unit
) {
    val articles = viewModel.homeNewsPagingData.collectAsLazyPagingItems()

    val bookmarkedUrls by viewModel.getAllBookmarkedUrls()
        .collectAsState(initial = emptySet())

    val snackbarHostState = remember { SnackbarHostState() }

    val uiMessage by viewModel.uiMessage.collectAsState(null)

//    var username by remember { mutableStateOf<String?>(null) }
    val username by authViewModel.username.collectAsState()

    LaunchedEffect(Unit) {
        authViewModel.loadUsername()
    }

    uiMessage?.let { message ->

        val messageText = stringResource(message)

        LaunchedEffect(message) {
            snackbarHostState.showSnackbar(messageText)
        }
    }

    LaunchedEffect(Unit) {
        val userId = Firebase.auth.currentUser?.uid
        if (userId != null) {
            viewModel.setCurrentUser(userId)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) {
        padding ->
        HomeScreenContent(
            articles = articles,
            bookmarkedUrls = bookmarkedUrls,
            onArticleClick = onArticleClick,
            onBookmarkClick = { viewModel.toggleBookmark(it) },
            onCategorySelected = { viewModel.setHomeCategory(it) },
            username = username
        )
    }
}

//stateless
@Composable
fun HomeScreenContent(
    articles: LazyPagingItems<Article>,
    bookmarkedUrls: Set<String>,
    onArticleClick: (Article) -> Unit,
    onBookmarkClick: (Article) -> Unit,
    onCategorySelected: (String) -> Unit,
    username: String?
) {

    val dateFormatter = remember { DateFormatter() }

    var selectedCategory by rememberSaveable { mutableStateOf(Category.All) }

    val listState = rememberLazyListState()


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .background(MaterialTheme.colorScheme.background)
    ) {

        Row(verticalAlignment = Alignment.CenterVertically) {

            Text(
                text = username?.let { "Hello, $it" } ?: "",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(top = 16.dp)
            )

            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                imageVector = Icons.Default.WavingHand,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(42.dp)
                    .padding(top = 12.dp)
            )
        }

            Text(
                text = (stringResource(Res.string.home_desc)),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
            )

            CategoryChips(
                categories = Category.names,
                selectedCategory = selectedCategory.displayName,  // selected string
                onCategorySelected = { categoryName ->
                    val category = Category.values().first { it.displayName == categoryName }
                    selectedCategory = category
                    onCategorySelected(category.displayName)
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

                    val error = (articles.loadState.refresh as LoadState.Error).error
                    val mappedError = mapToNetworkError(error)

                    EmptyState(
                        error = mappedError,
                        message = stringResource(mapErrorToMessage(mappedError)),
                        onRetry = { articles.retry() }
                    )
                }

                else -> {

                    LazyColumn(
                        state = listState,
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {

                        // 🔹 Initial Loading
                        if (articles.loadState.refresh is LoadState.Loading) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillParentMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                        }

                        // 🔹 Content
                        items(
                            count = articles.itemCount,
                            key = { index ->
                                val article = articles[index]
                                "${article?.url}_${index}"
                            }
                        ) { index ->

                            val article = articles[index]


                            if (article != null) {

                                val bookmarked = bookmarkedUrls.contains(article.url)

                                HomeArticleItem(
                                    article = article,
                                    isBookmarked = bookmarked,
                                    onClick = { onArticleClick(article) },
                                    onBookmarkClick = { onBookmarkClick(article) },
                                    dateFormatter = dateFormatter
                                )
                            }
                        }

                        // 🔹 Pagination
                        when (articles.loadState.append) {

                            is LoadState.Loading -> {
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

                            is LoadState.Error -> {

                                val error = (articles.loadState.append as LoadState.Error).error
                                val mappedError = mapToNetworkError(error)

                                item {
                                    EmptyState(
                                        error = mappedError,
                                        message = stringResource(mapErrorToMessage(mappedError)),
                                        onRetry = { articles.retry() }
                                    )
                                }
                            }

                            else -> Unit
                        }
                        //end reached
                     if (articles.loadState.append is LoadState.NotLoading &&
                        articles.loadState.append.endOfPaginationReached &&
                        articles.itemCount > 0
                    ) {

                             println("UI DEBUG -> End reached triggered. itemCount=${articles.itemCount}")
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(stringResource(Res.string.pagination_end))
                            }
                        }
                    }
                    }
                }
            }
        }
    }

@Composable
fun HomeScreenPreviewContent(
    articles: List<Article>
) {
    val dateFormatter = remember { DateFormatter() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {

        Row {
        Text(
            text = stringResource(Res.string.hello_user),
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(top = 16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Icon(
            imageVector = Icons.Default.WavingHand,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(42.dp)
                .padding(top = 12.dp)
        )

    }

        Text(
            text = stringResource(Res.string.home_desc),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
        )

        CategoryChips(
            categories = Category.names,
            selectedCategory = Category.All.displayName,
            onCategorySelected = {}
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(articles) { article ->
                HomeArticleItem(
                    article = article,
                    isBookmarked = false,
                    onClick = {},
                    onBookmarkClick = {},
                    dateFormatter = dateFormatter
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    NewsAppTheme {

        val articles = List(5) { articleMock() }

        HomeScreenPreviewContent(articles = articles)
    }
}