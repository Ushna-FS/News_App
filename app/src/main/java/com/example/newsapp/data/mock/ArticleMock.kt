package com.example.newsapp.data.mock

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.newsapp.R
import com.example.shared.data.models.Article
import com.example.shared.data.models.Source

@Composable
fun articleMock(): Article {
    return Article(
        source = Source(
            id = "sample-source-id",
            name = stringResource(R.string.samplesource)
        ),
        author = stringResource(R.string.sampleAuthorname),
        title = stringResource(R.string.breaking_news_jetpack_compose_is_awesome),
        description = stringResource(R.string.sampleDescription),
        url = "https://example.com/compose-article",
        urlToImage = "https://via.placeholder.com/300",
        publishedAt = stringResource(R.string.sampledate),
        content = stringResource(R.string.sampleContent)
    )
}