package com.example.newsapp.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.newsapp.data.models.Article
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.example.newsapp.NewsAppTheme
import com.example.newsapp.data.ArticleCategoryMapper
import com.example.newsapp.data.models.Source
import com.example.newsapp.utils.DateFormatter

@Composable
fun HomeArticleItem(
    article: Article,
    isBookmarked: Boolean,
    dateFormatter: DateFormatter,
    onClick: () -> Unit,
    onBookmarkClick: () -> Unit
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {

            // SOURCE + TIME
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = article.source.name,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 14.sp
                )

                Text(
                    text = dateFormatter.formatDisplayDate(article.publishedAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(8.dp))

            // TITLE + BOOKMARK
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = article.title,
                    modifier = Modifier.weight(1f),
                    maxLines = 2,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )

                IconButton(onClick = onBookmarkClick) {
                    Icon(
                        imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(Modifier.height(4.dp))

            // DESCRIPTION
            if (!article.description.isNullOrEmpty()) {
                Text(
                    text = article.description,
                    maxLines = 2,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
            }

            // CATEGORY + READ MORE
            val category = ArticleCategoryMapper.getCategory(article)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AssistChip(
                    onClick = {},
                    label = { Text(category) },
                    border = BorderStroke(1.dp, Color.Blue)
                )

                Spacer(Modifier.weight(1f))

                TextButton(onClick = onClick) {
                    Text("Read More")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ArticleItemPreview() {
    NewsAppTheme {
        val sampleArticle = Article(
            title = "Jetpack Compose is transforming Android UI development",
            source = Source(
                id = "techcrunch",
                name = "TechCrunch"
            ),
            author = null,
            description = "Compose simplifies UI creation with less code and real-time previews.",
            url = "https://example.com",
            urlToImage = null,
            publishedAt = "2026-03-10T12:00:00Z",
            content = null
        )

        HomeArticleItem(
            article = sampleArticle,
            isBookmarked = false,
            onClick = {},
            onBookmarkClick = {},
            dateFormatter = DateFormatter()
        )
    }
}