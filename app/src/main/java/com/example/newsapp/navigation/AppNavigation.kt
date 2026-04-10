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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.example.newsapp.R
import com.example.newsapp.ui.screens.LoginScreen
import com.example.newsapp.ui.screens.SignupScreen
import com.example.newsapp.ui.screens.SplashScreen
import com.example.newsapp.viewmodels.AuthViewModel
import com.example.newsapp.viewmodels.NewsViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch


enum class MainTab {
    HOME,
    DISCOVER,
    BOOKMARK
}

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


            MainTabs(navController)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTabs(navController: NavHostController) {

    val newsViewModel: NewsViewModel = hiltViewModel()
    val authViewModel: AuthViewModel = hiltViewModel()
    val user = FirebaseAuth.getInstance().currentUser

    LaunchedEffect(user?.uid) {
        user?.uid?.let {
            newsViewModel.setCurrentUser(it)
            newsViewModel.startBookmarkSync(it)
        }
    }
    val drawerState = rememberDrawerState(DrawerValue.Closed)

    val scope = rememberCoroutineScope()

    var selectedTab by rememberSaveable { mutableStateOf(MainTab.HOME) }

    // Separate NavControllers for each tab
    val homeNavController = rememberNavController()
    val discoverNavController = rememberNavController()
    val bookmarkNavController = rememberNavController()
    val currentNavController = when (selectedTab) {
        MainTab.HOME -> homeNavController
        MainTab.DISCOVER -> discoverNavController
        MainTab.BOOKMARK -> bookmarkNavController
        else -> homeNavController
    }

    fun closeDrawer(action: () -> Unit) {
        scope.launch {
            drawerState.close()
        }
        action()
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
                    closeDrawer { selectedTab = MainTab.HOME }
                },
                onDiscoverClick = {
                    closeDrawer { selectedTab = MainTab.DISCOVER }
                },
                onBookmarksClick = {
                    closeDrawer { selectedTab = MainTab.BOOKMARK }
                },
                onLogoutClick = {

                    authViewModel.logout()

                    navController.navigate(Routes.Login.route) {
                        popUpTo(Routes.MainTabs.route) { inclusive = true }
                    }
                }
            )
        }
    ) {

        Scaffold(

            topBar = {
                if (!isDetailScreen) {

                    TopAppBar(
                        title = { Text(stringResource(R.string.newsmate)) },
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
                        selected = selectedTab == MainTab.HOME,
                        onClick = { selectedTab = MainTab.HOME },
                        icon = { Icon(Icons.Default.Home, null) },

                        label = { Text(stringResource(R.string.home)) })

                    NavigationBarItem(
                        selected = selectedTab == MainTab.DISCOVER,
                        onClick = { selectedTab = MainTab.DISCOVER },
                        icon = { Icon(Icons.Default.Search, null) },

                        label = { Text(stringResource(R.string.discover)) }
                    )

                    NavigationBarItem(
                        selected = selectedTab == MainTab.BOOKMARK,
                        onClick = { selectedTab = MainTab.BOOKMARK },
                        icon = { Icon(Icons.Default.Bookmark, null) },
                        label = { Text(stringResource(R.string.saved)) }
                    )
                }
            }

        ) { padding ->

            when (selectedTab) {

                MainTab.HOME -> {

                    HomeNavGraph(
                        navController = homeNavController,
                        modifier = Modifier.padding(padding)
                    )
                }

                MainTab.DISCOVER -> {

                    DiscoverNavGraph(
                        navController = discoverNavController,
                        modifier = Modifier.padding(padding)
                    )
                }

                MainTab.BOOKMARK -> {

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