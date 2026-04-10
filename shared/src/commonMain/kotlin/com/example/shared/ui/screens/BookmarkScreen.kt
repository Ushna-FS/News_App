package com.example.shared.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.shared.data.local.BookmarkedArticle
import com.example.shared.data.mock.bookmarkedArticleMock
import com.example.shared.ui.actions.shareBookmark
import com.example.shared.ui.components.BookmarkItem
import com.example.shared.viewmodels.NewsViewModel
import me.sample.library.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun BookmarksScreenContent(
    bookmarks: List<BookmarkedArticle>,
    onReadMore: (BookmarkedArticle) -> Unit,
    onToggleBookmark: (BookmarkedArticle) -> Unit,
    onShare: (BookmarkedArticle) -> Unit,
    snackbarHostState: SnackbarHostState

) {

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize()
        ) {

            Text(
                text = stringResource(Res.string.your_saved_articles),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(16.dp)
            )

            if (bookmarks.isEmpty()) {

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(stringResource(Res.string.no_bookmarks_yet))
                }

            } else {

                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {

                    items(bookmarks) { bookmark ->

                        BookmarkItem(
                            article = bookmark,
                            onReadMore = { onReadMore(bookmark) },
                            onBookmark = { onToggleBookmark(bookmark) },
                            onShare = { onShare(bookmark) }
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun BookmarksScreen(
    newsViewModel: NewsViewModel = koinViewModel(),
    openArticleDetail: (BookmarkedArticle) -> Unit,
    toggleBookmark: (BookmarkedArticle) -> Unit
) {

    val bookmarks by newsViewModel.bookmarks.collectAsState(initial = emptyList())

    val snackbarHostState = remember { SnackbarHostState() }
    val uiMessage by newsViewModel.uiMessage.collectAsState(null)

    uiMessage?.let { message ->

        val messageText = stringResource(message)

        LaunchedEffect(message) {
            snackbarHostState.showSnackbar(messageText)
        }
    }

    BookmarksScreenContent(
        bookmarks = bookmarks,
        onReadMore = openArticleDetail,
        onToggleBookmark = toggleBookmark,
        onShare = { bookmark ->
            shareBookmark(bookmark.url)
        },
        snackbarHostState = snackbarHostState,
    )
}

@Preview(showBackground = true)
@Composable
fun BookmarksScreenPreview() {

    val dummyList = listOf(bookmarkedArticleMock())
    val snackbarHostState = remember { SnackbarHostState() }

    BookmarksScreenContent(
        bookmarks = dummyList,
        onReadMore = {},
        onToggleBookmark = {},
        onShare = {},
        snackbarHostState = snackbarHostState
    )
}