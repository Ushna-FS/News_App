package com.example.newsapp.data.mock

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.newsapp.R
import com.example.shared.data.local.BookmarkedArticle

@Composable
fun bookmarkedArticleMock(): BookmarkedArticle {
    return BookmarkedArticle(
        id = 1,
        title = stringResource(R.string.breaking_news_jetpack_compose_is_awesome),
        description = stringResource(R.string.sampleDescription),
        url = "https://example.com",
        urlToImage = "https://via.placeholder.com/150",
        publishedAt = stringResource(R.string.sampledate),
        content = stringResource(R.string.sampleContent),
        sourceName = stringResource(R.string.samplesource),
        sourceId = stringResource(R.string.samplesourceId),
        author = stringResource(R.string.sampleAuthorname),
        bookmarkedAt = System.currentTimeMillis() - 60 * 60 * 1000 // 1 hour ago
    )
}