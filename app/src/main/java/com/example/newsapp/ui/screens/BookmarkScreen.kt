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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.shared.data.local.BookmarkedArticle
import com.example.newsapp.viewmodels.NewsViewModel
import com.example.newsapp.ui.components.BookmarkItem
import org.koin.androidx.compose.koinViewModel
import com.example.newsapp.R


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

    val dummyList = listOf(
        BookmarkedArticle(
            id = 1,
            title = stringResource(R.string.news_title_here),
            description = stringResource(R.string.sampleDescription),
            url = "https://example.com/1",
            urlToImage = "https://example.com/image1.jpg",
            publishedAt = stringResource(R.string.sampledate),
            content = stringResource(R.string.sampleContent),
            sourceName = stringResource(R.string.samplesource),
            sourceId = stringResource(R.string.samplesourceId),
            author = stringResource(R.string.sampleAuthorname),
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