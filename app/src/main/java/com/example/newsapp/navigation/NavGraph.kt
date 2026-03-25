package com.example.newsapp.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.shared.data.models.Article
import com.example.newsapp.ui.screens.ArticleDetailScreen
import com.example.newsapp.ui.screens.BookmarksScreen
import com.example.newsapp.ui.screens.DiscoverScreen
import com.example.newsapp.ui.screens.HomeScreen
import com.example.newsapp.utils.toArticle
import com.example.newsapp.viewmodels.NewsViewModel
import com.google.gson.Gson

@Composable
fun HomeNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {

    NavHost(
        navController = navController,
        startDestination = Routes.Home.route,
        modifier = modifier
    ) {

        composable(Routes.Home.route) {

            HomeScreen(
                onArticleClick = { article ->

                    val articleJson = Uri.encode(Gson().toJson(article))

                    navController.navigate(
                        Routes.ArticleDetail.createRoute(articleJson)
                    )
                }
            )
        }

        composable(Routes.ArticleDetail.route) { backStackEntry ->

            val articleJson =
                backStackEntry.arguments?.getString("article")

            val article =
                Gson().fromJson(articleJson, Article::class.java)

            ArticleDetailScreen(
                article = article,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}

@Composable
fun DiscoverNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {

    NavHost(
        navController = navController,
        startDestination = Routes.Discover.route,
        modifier = modifier
    ) {

        composable(Routes.Discover.route) {

            DiscoverScreen(
                onArticleClick = { article ->

                    val articleJson =
                        Uri.encode(Gson().toJson(article))

                    navController.navigate(
                        Routes.ArticleDetail.createRoute(articleJson)
                    )
                }
            )
        }

        composable(Routes.ArticleDetail.route) { backStackEntry ->

            val articleJson =
                backStackEntry.arguments?.getString("article")

            val article =
                Gson().fromJson(articleJson, Article::class.java)

            ArticleDetailScreen(
                article = article,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}

@Composable
fun BookmarkNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    newsViewModel:
    NewsViewModel
) {

    NavHost(
        navController = navController,
        startDestination = Routes.Bookmarks.route,
        modifier = modifier
    ) {

        composable(Routes.Bookmarks.route) {

            BookmarksScreen(
                openArticleDetail = { article ->

                    val articleJson =
                        Uri.encode(Gson().toJson(article))

                    navController.navigate(
                        Routes.ArticleDetail.createRoute(articleJson)
                    )
                },
                toggleBookmark = {
                    newsViewModel.toggleBookmark(it.toArticle())
                }
            )
        }

        composable(Routes.ArticleDetail.route) { backStackEntry ->

            val articleJson =
                backStackEntry.arguments?.getString("article")

            val article =
                Gson().fromJson(articleJson, Article::class.java)

            ArticleDetailScreen(
                article = article,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}