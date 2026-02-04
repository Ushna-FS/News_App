package com.example.newsapp.data.models

import kotlinx.serialization.Serializable


data class NewsResponse(
    val status: String,
    val totalResults: Int,
    val articles: List<Article>
)

@Serializable
data class Article(
    val source: Source?,
    val author: String?,
    val title: String?,
    val description: String?,
    val url: String,
    val urlToImage: String?,
    val publishedAt: String?,
    val content: String?
)

@Serializable
data class Source(
    val id: String?,
    val name: String?
)

fun Article.getFullContent(): String {
    return when {
        !content.isNullOrBlank() ->
            content.substringBefore("[")

        !description.isNullOrBlank() ->
            description

        else ->
            "Full article content is not available.\nTap \"Open in Browser\" to read more."
    }
}


fun Article.getFormattedDate(): String {
    return publishedAt?.substringBefore("T") ?: ""
}