package com.example.shared.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.shared.data.models.Article
import com.example.shared.ui.screens.ArticleDetailScreen
import com.example.shared.ui.screens.BookmarksScreen
import com.example.shared.ui.screens.DiscoverScreen
import com.example.shared.ui.screens.HomeScreen
import com.example.shared.utils.toArticle
import com.example.shared.viewmodels.NewsViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier,
    startDestination: Routes = Routes.Splash
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // Home graph
        composable<Routes.Home> {
            val viewModel: NewsViewModel = koinViewModel()
            HomeScreen(
                onArticleClick = { article ->
                    val articleJson = encodeToJsonString(article)
                    navController.navigate(Routes.ArticleDetail(articleJson))
                }
            )
        }

        // Discover graph
        composable<Routes.Discover> {
            DiscoverScreen(
                onArticleClick = { article ->
                    val articleJson = encodeToJsonString(article)
                    navController.navigate(Routes.ArticleDetail(articleJson))
                }
            )
        }

        // Bookmarks graph
        composable<Routes.Bookmarks> {
            val viewModel: NewsViewModel = koinViewModel()
            BookmarksScreen(
                openArticleDetail = { bookmarkedArticle ->
                    // Convert BookmarkedArticle to Article before serializing
                    val article = bookmarkedArticle.toArticle()
                    val articleJson = encodeToJsonString(article)
                    navController.navigate(Routes.ArticleDetail(articleJson))
                },
                toggleBookmark = { bookmarkedArticle ->
                    viewModel.toggleBookmark(bookmarkedArticle.toArticle())
                }
            )
        }

        // Article detail - common for all
        composable<Routes.ArticleDetail> { backStackEntry ->
            val articleDetail = backStackEntry.toRoute<Routes.ArticleDetail>()
            val article = decodeFromJsonString<Article>(articleDetail.articleJson)

            ArticleDetailScreen(
                article = article,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}