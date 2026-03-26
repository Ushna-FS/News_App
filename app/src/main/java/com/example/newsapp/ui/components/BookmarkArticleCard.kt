package com.example.newsapp.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.newsapp.R
import com.example.newsapp.data.local.BookmarkedArticle
import com.example.newsapp.utils.DateFormatter

@Composable
fun BookmarkItem(
    article: BookmarkedArticle,
    onReadMore: () -> Unit,
    onBookmark: () -> Unit,
    onShare: () -> Unit,
    dateFormatter: DateFormatter = DateFormatter()
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {

        Row(
            modifier = Modifier
                .padding(16.dp)
        ) {

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Text(
                        text = article.sourceName ?: "",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = dateFormatter.getTimeAgo(article.bookmarkedAt),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = article.title ?: "",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(22.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(
                        text = stringResource(R.string.read_more__),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onReadMore() }
                    )
                }
            }
            Column(
                horizontalAlignment = Alignment.End,
            ) {

                AsyncImage(
                    model = article.urlToImage ?: R.drawable.ic_newspaper,
                    contentDescription = null,
                    modifier = Modifier
                        .size(width = 96.dp, height = 75.dp)
                        .clip(RoundedCornerShape(8.dp))

                )

                Spacer(modifier = Modifier.height(6.dp))

                Row {

                    IconButton(onClick = onShare) {
                        Icon(
                            Icons.Default.Share,
                            contentDescription = stringResource(R.string.share)
                        )
                    }

                    IconButton(onClick = onBookmark) {
                        Icon(
                            Icons.Default.Bookmark,
                            contentDescription = stringResource(R.string.bookmarks),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

        }
    }
}

@Preview(showBackground = true)
@Composable
fun BookmarkItemPreview() {

    val dummyArticle = BookmarkedArticle(
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

    MaterialTheme {
        BookmarkItem(
            article = dummyArticle,
            onReadMore = {},
            onBookmark = {},
            onShare = {}
        )
    }
}