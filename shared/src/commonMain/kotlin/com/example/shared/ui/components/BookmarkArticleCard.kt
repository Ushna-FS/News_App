package com.example.shared.ui.components

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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.shared.data.local.BookmarkedArticle
import com.example.shared.data.mock.bookmarkedArticleMock
import com.example.shared.utils.DateFormatter
import me.sample.library.resources.Res
import me.sample.library.resources.bookmarks
import me.sample.library.resources.ic_newspaper
import me.sample.library.resources.read_more__
import me.sample.library.resources.share
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

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
                        text = stringResource(Res.string.read_more__),
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
                    model = article.urlToImage,
                    contentDescription = null,
                    placeholder = painterResource(Res.drawable.ic_newspaper),
                    error = painterResource(Res.drawable.ic_newspaper),
                    modifier = Modifier
                        .size(width = 96.dp, height = 75.dp)
                        .clip(RoundedCornerShape(8.dp))
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row {

                    IconButton(onClick = onShare) {
                        Icon(
                            Icons.Default.Share,
                            contentDescription = stringResource(Res.string.share)
                        )
                    }

                    IconButton(onClick = onBookmark) {
                        Icon(
                            Icons.Default.Bookmark,
                            contentDescription = stringResource(Res.string.bookmarks),
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
    MaterialTheme {
        BookmarkItem(
            article = bookmarkedArticleMock(),
            onReadMore = {},
            onBookmark = {},
            onShare = {}
        )
    }
}