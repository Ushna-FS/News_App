package com.example.newsapp.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.newsapp.data.local.BookmarkedArticle
import com.example.newsapp.viewmodels.NewsViewModel
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.newsapp.ui.components.BookmarkItem


@Composable
fun BookmarksScreenContent(
    bookmarks: List<BookmarkedArticle>,
    onReadMore: (BookmarkedArticle) -> Unit,
    onToggleBookmark: (BookmarkedArticle) -> Unit,
    onShare: (BookmarkedArticle) -> Unit
) {

    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        Text(
            text = "Your Saved Articles",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(16.dp)
        )

        if (bookmarks.isEmpty()) {

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No bookmarks yet")
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


@Composable
fun BookmarksScreen(
    newsViewModel: NewsViewModel = hiltViewModel(),
    openArticleDetail: (BookmarkedArticle) -> Unit,
    toggleBookmark: (BookmarkedArticle) -> Unit
) {

    val context = LocalContext.current
    val currentContext by rememberUpdatedState(context)

    val bookmarks by newsViewModel.bookmarks.collectAsState(initial = emptyList())

    LaunchedEffect(Unit) {
        newsViewModel.uiMessage.collect { resId ->
            Toast.makeText(
                currentContext,
                currentContext.getString(resId),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    BookmarksScreenContent(
        bookmarks = bookmarks,
        onReadMore = openArticleDetail,
        onToggleBookmark = toggleBookmark,
        onShare = { bookmark ->

            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, bookmark.url)
                type = "text/plain"
            }

            context.startActivity(
                Intent.createChooser(shareIntent, "Share via")
            )
        }
    )
}

@Preview(showBackground = true)
@Composable
fun BookmarksScreenPreview() {

    val dummyList = listOf(
        BookmarkedArticle(
            id = 1,
            title = "Sample News 1",
            description = "This is a sample description for news 1",
            url = "https://example.com/1",
            urlToImage = "https://example.com/image1.jpg",
            publishedAt = "2024-01-01",
            content = "Sample content",
            sourceName = "BBC",
            sourceId = "bbc-news",
            author = "John Doe",
            bookmarkedAt = System.currentTimeMillis()
        ),
        BookmarkedArticle(
            id = 2,
            title = "Sample News 2",
            description = "This is a sample description for news 2",
            url = "https://example.com/2",
            urlToImage = "https://example.com/image2.jpg",
            publishedAt = "2024-01-02",
            content = "Sample content 2",
            sourceName = "CNN",
            sourceId = "cnn",
            author = "Jane Smith",
            bookmarkedAt = System.currentTimeMillis()
        )
    )

    BookmarksScreenContent(
        bookmarks = dummyList,
        onReadMore = {},
        onToggleBookmark = {},
        onShare = {}
    )
}