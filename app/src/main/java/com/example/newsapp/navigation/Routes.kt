package com.example.newsapp.navigation

sealed class Routes(val route: String) {

    object Splash : Routes("splash")
    object Home : Routes("home")

    object Discover : Routes("discover")

    object Bookmarks : Routes("bookmarks")

    object Login : Routes("login")

    object Signup : Routes("signup")

    object MainTabs : Routes("main_tabs")

    object ArticleDetail : Routes("article_detail/{article}") {

        fun createRoute(articleJson: String) = "article_detail/$articleJson"
    }
}