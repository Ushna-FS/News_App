package com.example.shared.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.example.shared.ui.components.MainToolbar
import com.example.shared.ui.screens.LoginScreen
import com.example.shared.ui.screens.SignupScreen
import com.example.shared.ui.screens.SplashScreen
import com.example.shared.viewmodels.AuthViewModel
import com.example.shared.viewmodels.NewsViewModel
import org.koin.compose.viewmodel.koinViewModel
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.launch
import me.sample.library.resources.Res
import me.sample.library.resources.*
import org.jetbrains.compose.resources.stringResource


enum class MainTab {
    HOME,
    DISCOVER,
    BOOKMARK
}

@Composable
fun RootNavigation(padding: PaddingValues, modifier: Modifier = Modifier) {

    val navController = rememberNavController()

    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = Routes.Splash
    ) {

        composable<Routes.Splash> {
            SplashScreen(navController)
        }
        composable<Routes.Login> {
            LoginScreen(navController)
        }

        composable<Routes.Signup> {
            SignupScreen(navController)
        }

        composable<Routes.MainTabs> {
            MainTabs(navController, padding)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTabs(rootNavController: NavHostController, padding: PaddingValues) {

    val newsViewModel: NewsViewModel = koinViewModel()
    val authViewModel: AuthViewModel = koinViewModel()
    val user = Firebase.auth.currentUser

    LaunchedEffect(user?.uid) {
        user?.uid?.let {
            newsViewModel.setCurrentUser(it)
            newsViewModel.startBookmarkSync(it)
        }
    }

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var selectedTab by rememberSaveable { mutableStateOf(MainTab.HOME) }

    // Separate NavControllers for each tab to preserve back stacks
    val homeNavController = rememberNavController()
    val discoverNavController = rememberNavController()
    val bookmarkNavController = rememberNavController()
    val currentNavController = when (selectedTab) {
        MainTab.HOME -> homeNavController
        MainTab.DISCOVER -> discoverNavController
        MainTab.BOOKMARK -> bookmarkNavController
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
    val isDetailScreen = currentRoute.contains("ArticleDetail")

    handleBackPress {
        when {
            drawerState.isOpen -> {
                scope.launch { drawerState.close() }
            }

            currentNavController.popBackStack() -> {

            }

            selectedTab != MainTab.HOME -> {
                selectedTab = MainTab.HOME
            }

            else -> {
                exitApp()
            }
        }
    }

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
                    rootNavController.navigate(Routes.Login) {
                        popUpTo(Routes.MainTabs) { inclusive = true }
                    }
                }
            )
        }
    ) {

        Scaffold(

            topBar = {
                if (!isDetailScreen) {

                    MainToolbar(
                        title = stringResource(Res.string.newsmate),
                        onMenuClick = {
                            scope.launch {
                                drawerState.open()
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
                        label = { Text(stringResource(Res.string.home)) })

                    NavigationBarItem(
                        selected = selectedTab == MainTab.DISCOVER,
                        onClick = { selectedTab = MainTab.DISCOVER },
                        icon = { Icon(Icons.Default.Search, null) },
                        label = { Text(stringResource(Res.string.discover)) }
                    )

                    NavigationBarItem(
                        selected = selectedTab == MainTab.BOOKMARK,
                        onClick = { selectedTab = MainTab.BOOKMARK },
                        icon = { Icon(Icons.Default.Bookmark, null) },
                        label = { Text(stringResource(Res.string.saved)) }
                    )
                }
            }
        ) { padding ->
            // Show the appropriate NavGraph based on selected tab
            when (selectedTab) {
                MainTab.HOME -> {
                    AppNavGraph(
                        navController = homeNavController,
                        modifier = Modifier.padding(padding),
                        startDestination = Routes.Home
                    )
                }

                MainTab.DISCOVER -> {
                    AppNavGraph(
                        navController = discoverNavController,
                        modifier = Modifier.padding(padding),
                        startDestination = Routes.Discover
                    )
                }

                MainTab.BOOKMARK -> {
                    AppNavGraph(
                        navController = bookmarkNavController,
                        modifier = Modifier.padding(padding),
                        startDestination = Routes.Bookmarks
                    )
                }
            }
        }
    }
}