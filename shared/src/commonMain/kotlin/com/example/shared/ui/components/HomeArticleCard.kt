package com.example.shared.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shared.NewsAppTheme
import com.example.shared.data.mock.articleMock
import com.example.shared.data.models.Article
import com.example.shared.data.models.getCategory
import com.example.shared.utils.Category
import com.example.shared.utils.DateFormatter
import me.sample.library.resources.Res
import me.sample.library.resources.read_more
import org.jetbrains.compose.resources.stringResource

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
        elevation = CardDefaults.cardElevation(1.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
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
                    text = article.description ?: "",
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
            val category = article.getCategory()
            val displayCategoryName =
                if (category == Category.All) "General" else category.displayName

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AssistChip(
                    onClick = {},
                    label = { Text(displayCategoryName) },
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                )

                Spacer(Modifier.weight(1f))

                TextButton(onClick = onClick) {
                    Text(stringResource(Res.string.read_more))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeArticleItemPreviewWithMock() {
    NewsAppTheme {
        // Use the mock article from your data.mock
        val sampleArticle = articleMock()

        HomeArticleItem(
            article = sampleArticle,
            isBookmarked = false,
            onClick = {},
            onBookmarkClick = {},
            dateFormatter = DateFormatter()
        )
    }
}