package com.example.newsapp.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.compose.*
import com.example.newsapp.ui.screens.LoginScreen
import com.example.newsapp.ui.screens.SignupScreen
import com.example.newsapp.ui.screens.SplashScreen
import com.example.newsapp.viewmodels.NewsViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@Composable
fun RootNavigation() {

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.Splash.route
    ) {

        composable(Routes.Splash.route) {

            SplashScreen(navController)
        }
        composable(Routes.Login.route) {
            LoginScreen(navController)
        }

        composable(Routes.Signup.route) {
            SignupScreen(navController)
        }

        composable(Routes.MainTabs.route) {

            MainTabs()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTabs() {

    val newsViewModel: NewsViewModel = hiltViewModel()

    val user = FirebaseAuth.getInstance().currentUser

    LaunchedEffect(user?.uid) {
        user?.uid?.let {
            newsViewModel.setCurrentUser(it)
            newsViewModel.startBookmarkSync(it)
        }
    }

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var selectedTab by rememberSaveable { mutableStateOf("home") }

    // Separate NavControllers for each tab
    val homeNavController = rememberNavController()
    val discoverNavController = rememberNavController()
    val bookmarkNavController = rememberNavController()
    val currentNavController = when (selectedTab) {
        "home" -> homeNavController
        "discover" -> discoverNavController
        "bookmark" -> bookmarkNavController
        else -> homeNavController
    }

    // Observe back stack safely
    val navBackStackEntry by currentNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: ""
    val isDetailScreen = currentRoute.contains("article_detail")
    ModalNavigationDrawer(
        drawerState = drawerState,

        gesturesEnabled = !isDetailScreen,
        drawerContent = {

            AppDrawer(
                onHomeClick = {
                    selectedTab = "home"
                },
                onDiscoverClick = {
                    selectedTab = "discover"
                },
                onBookmarksClick = {
                    selectedTab = "bookmark"
                }
            )
        }
    ) {

        Scaffold(

            topBar = {
                if (!isDetailScreen) {

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

                NavigationBar {

                    NavigationBarItem(
                        selected = selectedTab == "home",
                        onClick = { selectedTab = "home" },
                        icon = { Icon(Icons.Default.Home, null) },
                        label = { Text("Home") }
                    )

                    NavigationBarItem(
                        selected = selectedTab == "discover",
                        onClick = { selectedTab = "discover" },
                        icon = { Icon(Icons.Default.Search, null) },
                        label = { Text("Discover") }
                    )

                    NavigationBarItem(
                        selected = selectedTab == "bookmark",
                        onClick = { selectedTab = "bookmark" },
                        icon = { Icon(Icons.Default.Bookmark, null) },
                        label = { Text("Saved") }
                    )
                }
            }

        ) { padding ->

            when (selectedTab) {

                "home" -> {

                    HomeNavGraph(
                        navController = homeNavController,
                        modifier = Modifier.padding(padding)
                    )
                }

                "discover" -> {

                    DiscoverNavGraph(
                        navController = discoverNavController,
                        modifier = Modifier.padding(padding)
                    )
                }

                "bookmark" -> {

                    BookmarkNavGraph(
                        navController = bookmarkNavController,
                        modifier = Modifier.padding(padding),
                        newsViewModel = newsViewModel
                    )
                }
            }
        }
    }
}