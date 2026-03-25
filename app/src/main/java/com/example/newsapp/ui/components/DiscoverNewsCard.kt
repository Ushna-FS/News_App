package com.example.newsapp.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.newsapp.R
import com.example.shared.data.models.Article
import com.example.newsapp.utils.DateFormatter

@Composable
fun DiscoverNewsCard(
    article: Article,
    isBookmarked: Boolean,
    onClick: () -> Unit,
    onBookmarkClick: () -> Unit
) {
    val dateFormatter = remember { DateFormatter() }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // News Image
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
//                    .data(article.urlToImage)
                    .data(article.urlToImage ?: "")
                    .crossfade(true)

                    .placeholder(R.drawable.ic_newspaper)
                    .error(R.drawable.ic_newspaper)
                    .build(),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Title and Bookmark
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
//                    text = article.title,
                    text = article.title ?: "No title",
                    fontWeight = FontWeight.Bold,
                    fontSize = MaterialTheme.typography.titleLarge.fontSize,
                    modifier = Modifier.weight(1f),
                    maxLines = 2
                )

                IconButton(onClick = onBookmarkClick) {
                    Icon(
                        imageVector = if (isBookmarked)
                            Icons.Default.Bookmark
                        else
                            Icons.Default.BookmarkBorder,
                        contentDescription = if (isBookmarked) "Remove bookmark" else "Add bookmark",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Description
            article.description?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = it,
                    maxLines = 3,
                    fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Source and Time
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = article.source.name,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelLarge
                )

                Spacer(modifier = Modifier.weight(1f))

                article.publishedAt?.let {
                    Text(
                        text = dateFormatter.formatDisplayDate(it),
                        fontSize = MaterialTheme.typography.labelSmall.fontSize,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}