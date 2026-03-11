package com.example.newsapp.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.*
import com.example.newsapp.ui.screens.HomeScreen
import com.example.newsapp.ui.screens.SplashScreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {

    val navController = rememberNavController()

    val drawerState = rememberDrawerState(DrawerValue.Closed)

    val scope = rememberCoroutineScope()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    ModalNavigationDrawer(

        drawerState = drawerState,

        drawerContent = {

            AppDrawer(

                onHomeClick = {
                    scope.launch { drawerState.close() }
                    navController.navigate(Routes.Home.route)
                },

                onDiscoverClick = {
                    scope.launch { drawerState.close() }
                    navController.navigate(Routes.Discover.route)
                },

                onBookmarksClick = {
                    scope.launch { drawerState.close() }
                    navController.navigate(Routes.Bookmarks.route)
                }
            )
        }

    ) {

        Scaffold(

            topBar = {
                if (currentRoute != Routes.Splash.route) {
                    TopAppBar(

                        title = { Text("NewsMate") },

                        navigationIcon = {

                            IconButton(

                                onClick = {
                                    scope.launch {

                                        drawerState.open()

                                    }

                                }

                            ) {

                                Icon(Icons.Default.Menu, null)

                            }
                        }
                    )
                }
            },

            bottomBar = {
                if (currentRoute != Routes.Splash.route) {
                    NavigationBar {

                        NavigationBarItem(
                            selected = false,
                            onClick = { navController.navigate(Routes.Home.route) },
                            icon = { Icon(Icons.Default.Home, null) },
                            label = { Text("Home") }
                        )

                        NavigationBarItem(
                            selected = false,
                            onClick = { navController.navigate(Routes.Discover.route) },
                            icon = { Icon(Icons.Default.Search, null) },
                            label = { Text("Discover") }
                        )

                        NavigationBarItem(
                            selected = false,
                            onClick = { navController.navigate(Routes.Bookmarks.route) },
                            icon = { Icon(Icons.Default.Bookmark, null) },
                            label = { Text("Saved") }
                        )

                    }
                }
            }

        ) { padding ->

            NavHost(

                navController = navController,

                startDestination = Routes.Splash.route,

                modifier = Modifier.padding(padding)

            ) {
                composable(Routes.Splash.route) {
                    SplashScreen(navController)
                }
                composable(Routes.Home.route) {

                    HomeScreen(
                        onArticleClick = {}
                    )
                }
            }
        }
    }
}