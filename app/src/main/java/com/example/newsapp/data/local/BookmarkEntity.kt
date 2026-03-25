package com.example.newsapp.data.local


import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.shared.data.models.Article

@Entity(
    tableName = "bookmarked_articles",
    indices = [Index(value = ["url"], unique = true)]
)
data class BookmarkedArticle(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    val url: String = "",

    val title: String = "",

    val description: String? = null,

    val urlToImage: String? = null,

    val publishedAt: String? = null,

    val content: String? = null,

    val sourceName: String = "",

    val sourceId: String? = null,

    val author: String? = null,

    val bookmarkedAt: Long = System.currentTimeMillis(),

    val isSynced: Boolean = false
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