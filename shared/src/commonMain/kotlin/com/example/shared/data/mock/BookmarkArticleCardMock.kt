package com.example.shared.data.mock

import androidx.compose.runtime.Composable
import me.sample.library.resources.Res
import me.sample.library.resources.*
import com.example.shared.data.local.BookmarkedArticle
import org.jetbrains.compose.resources.stringResource

@Composable
fun bookmarkedArticleMock(): BookmarkedArticle {
    return BookmarkedArticle(
        id = 1,
        title = stringResource(Res.string.breaking_news_jetpack_compose_is_awesome),
        description = stringResource(Res.string.sampleDescription),
        url = "https://example.com",
        urlToImage = "https://via.placeholder.com/150",
        publishedAt = stringResource(Res.string.sampledate),
        content = stringResource(Res.string.sampleContent),
        sourceName = stringResource(Res.string.samplesource),
        sourceId = stringResource(Res.string.samplesourceId),
        author = stringResource(Res.string.sampleAuthorname),
        bookmarkedAt = System.currentTimeMillis() - 60 * 60 * 1000 // 1 hour ago
    )
}