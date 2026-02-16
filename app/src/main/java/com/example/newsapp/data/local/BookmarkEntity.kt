package com.example.newsapp.data.local


import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.newsapp.data.models.Article

@Entity(tableName = "bookmarked_articles",
indices = [Index(value = ["url"], unique = true)]
)
data class BookmarkedArticle(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val url: String,
    val title: String,
    val description: String?,
    val urlToImage: String?,
    val publishedAt: String?,
    val content: String?,
    val sourceName: String,
    val sourceId: String?,
    val author: String?,
    val bookmarkedAt: Long = System.currentTimeMillis()
)

// Extension function to convert Article to BookmarkedArticle
fun Article.toBookmarkedArticle(): BookmarkedArticle {
    return BookmarkedArticle(
        id = 0,
        url = this.url,
        title = this.title,
        description = this.description,
        urlToImage = this.urlToImage,
        publishedAt = this.publishedAt,
        content = this.content,
        sourceName = this.source.name,
        sourceId = this.source.id,
        author = this.author,
        bookmarkedAt = System.currentTimeMillis()
    )
}