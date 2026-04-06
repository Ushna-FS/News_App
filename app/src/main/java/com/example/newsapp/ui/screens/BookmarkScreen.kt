package com.example.newsapp.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.newsapp.R
import com.example.newsapp.data.mock.bookmarkedArticleMock
import com.example.newsapp.ui.components.BookmarkItem
import com.example.newsapp.viewmodels.NewsViewModel
import com.example.shared.data.local.BookmarkedArticle
import org.koin.androidx.compose.koinViewModel

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
            text = stringResource(R.string.your_saved_articles),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(16.dp)
        )

        if (bookmarks.isEmpty()) {

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(stringResource(R.string.no_bookmarks_yet))
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
    newsViewModel: NewsViewModel = koinViewModel(),
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

    val dummyList = listOf(bookmarkedArticleMock())

    BookmarksScreenContent(
        bookmarks = dummyList,
        onReadMore = {},
        onToggleBookmark = {},
        onShare = {}
    )
}