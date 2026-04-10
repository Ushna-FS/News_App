package com.example.shared.data.mock

import androidx.compose.runtime.Composable
import me.sample.library.resources.Res
import me.sample.library.resources.*
import com.example.shared.data.models.Article
import com.example.shared.data.models.Source
import org.jetbrains.compose.resources.stringResource

@Composable
fun articleMock(): Article {
    return Article(
        source = Source(
            id = "sample-source-id",
            name = stringResource(Res.string.samplesource)
        ),
        author = stringResource(Res.string.sampleAuthorname),
        title = stringResource(Res.string.breaking_news_jetpack_compose_is_awesome),
        description = stringResource(Res.string.sampleDescription),
        url = "https://example.com/compose-article",
        urlToImage = "https://via.placeholder.com/300",
        publishedAt = stringResource(Res.string.sampledate),
        content = stringResource(Res.string.sampleContent)
    )
}