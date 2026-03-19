package com.example.newsapp.utils

import com.example.newsapp.data.local.BookmarkedArticle
import com.example.newsapp.data.models.Article
import com.example.newsapp.data.models.Source

fun BookmarkedArticle.toArticle(): Article {
    return Article(
        source = Source(id = null, name = this.sourceName),
        author = this.author,
        title = this.title,
        description = this.description,
        url = this.url,
        urlToImage = this.urlToImage,
        publishedAt = this.publishedAt,
        content = this.content
    )
}