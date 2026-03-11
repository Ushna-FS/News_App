package com.example.newsapp.navigation

sealed class Routes(val route: String) {

    object Splash : Routes("splash")
    object Home : Routes("home")

    object Discover : Routes("discover")

    object Bookmarks : Routes("bookmarks")

    object ArticleDetail : Routes("article_detail/{url}") {
        fun createRoute(url: String) = "article_detail/$url"
    }
}