package com.example.newsapp.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
                        text = article.sourceName ,
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
                    text = article.title ,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(22.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(
                        text = "Read More >>",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onReadMore() }
                    )}
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
                        Icon(Icons.Default.Share, contentDescription = "Share")
                    }

                    IconButton(onClick = onBookmark) {
                        Icon(
                            Icons.Default.Bookmark,
                            contentDescription = "Bookmark",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

    }}
}
@Preview(showBackground = true)
@Composable
fun BookmarkItemPreview() {

    val dummyArticle = BookmarkedArticle(
        title = "Breaking News: Jetpack Compose is Awesome",
        description = "Compose simplifies UI development",
        url = "https://example.com",
        urlToImage = "https://via.placeholder.com/150",
        publishedAt = "2024-01-01",
        content = "Full content here",
        sourceName = "TechCrunch",
        sourceId = "techcrunch",
        author = "John Doe",
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