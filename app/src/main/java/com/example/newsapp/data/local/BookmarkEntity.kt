package com.example.newsapp.data.local


import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookmarked_articles")
data class BookmarkedArticle(
    @PrimaryKey(autoGenerate = false)
    val url: String,
    val title: String?,
    val description: String?,
    val urlToImage: String?,
    val publishedAt: String?,
    val content: String?,
    val sourceName: String?,
    val sourceId: String?,
    val author: String?,
    val bookmarkedAt: Long = System.currentTimeMillis()
)

// Extension function to convert Article to BookmarkedArticle
fun com.example.newsapp.data.models.Article.toBookmarkedArticle(): BookmarkedArticle {
    return BookmarkedArticle(
        url = this.url ?: "",
        title = this.title,
        description = this.description,
        urlToImage = this.urlToImage,
        publishedAt = this.publishedAt,
        content = this.content,
        sourceName = this.source?.name,
        sourceId = this.source?.id,
        author = this.author,
        bookmarkedAt = System.currentTimeMillis()
    )
}