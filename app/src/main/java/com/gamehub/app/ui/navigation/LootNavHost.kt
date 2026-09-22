package com.gamehub.app.ui.navigation

import com.gamehub.app.ui.settings.SettingsViewModel
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.gamehub.app.di.AppContainer
import com.gamehub.app.ui.auth.LoginScreen
import com.gamehub.app.ui.auth.RegisterScreen
import com.gamehub.app.ui.details.GameDetailsScreen
import com.gamehub.app.ui.details.GameDetailsViewModel
import com.gamehub.app.ui.lootViewModelFactory
import com.gamehub.app.ui.home.HomeScreen
import com.gamehub.app.ui.home.HomeViewModel
import com.gamehub.app.ui.library.LibraryScreen
import com.gamehub.app.ui.library.LibraryViewModel
import com.gamehub.app.ui.onboarding.OnboardingScreen
import com.gamehub.app.ui.profile.ProfileScreen
import com.gamehub.app.ui.search.SearchScreen
import com.gamehub.app.ui.search.SearchViewModel
import com.gamehub.app.ui.session.SessionViewModel
import com.gamehub.app.ui.settings.SettingsScreen
import com.gamehub.app.ui.splash.SplashScreen
import com.gamehub.app.ui.wishlist.WishlistScreen
import com.gamehub.app.ui.wishlist.WishlistViewModel
import com.gamehub.app.ui.compare.CompareScreen
import com.gamehub.app.ui.compare.CompareViewModel
import com.gamehub.app.ui.surprise.SurpriseMeScreen
import com.gamehub.app.ui.surprise.SurpriseMeViewModel
import com.gamehub.app.utils.AppLogger

/** Root composable: owns the NavController, the bottom bar and every route. */
@Composable
fun LootNavHost(
    container: AppContainer,
    navController: NavHostController = rememberNavController()
) {
    val viewModelFactory = remember { lootViewModelFactory(container) }
    val sessionViewModel: SessionViewModel = viewModel(factory = viewModelFactory)
    val isLoggedIn by sessionViewModel.isLoggedIn.collectAsStateWithLifecycle()
    val currentUser by sessionViewModel.currentUser.collectAsStateWithLifecycle()

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = currentRoute in bottomBarRoutes

    LaunchedEffect(isLoggedIn) {
        val route = navController.currentDestination?.route
        if (isLoggedIn == false && route != null && route !in publicRoutes) {
            AppLogger.debug("Session ended on '$route', returning to Login")
            navController.navigate(Screen.Login.route) { popUpTo(navController.graph.id) { inclusive = true } }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (showBottomBar) {
                LootBottomBar(
                    currentRoute = currentRoute,
                    onTabSelected = { route ->
                        AppLogger.debug("Tab selected: $route")
                        navController.navigateToTab(route)
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier
                .padding(innerPadding)
                .statusBarsPadding()
                .then(if (showBottomBar) Modifier else Modifier.navigationBarsPadding())
        ) {
            composable(Screen.Splash.route) {
                SplashScreen(
                    viewModel = viewModel(factory = viewModelFactory),
                    onFinished = { startRoute ->
                        navController.navigate(startRoute) { popUpTo(Screen.Splash.route) { inclusive = true } }
                    }
                )
            }

            composable(Screen.Onboarding.route) {
                OnboardingScreen(
                    onFinished = {
                        sessionViewModel.markOnboardingSeen()
                        navController.navigate(Screen.Login.route) { popUpTo(Screen.Onboarding.route) { inclusive = true } }
                    }
                )
            }

            composable(Screen.Login.route) {
                LoginScreen(
                    viewModel = viewModel(factory = viewModelFactory),
                    onLoggedIn = { navController.navigateToHomeAfterAuth() },
                    onNavigateToRegister = { navController.navigate(Screen.Register.route) }
                )
            }

            composable(Screen.Register.route) {
                RegisterScreen(
                    viewModel = viewModel(factory = viewModelFactory),
                    onRegistered = { navController.navigateToHomeAfterAuth() },
                    onNavigateToLogin = { navController.popBackStack() }
                )
            }

            composable(Screen.Home.route) {
                val homeViewModel: HomeViewModel = viewModel(factory = viewModelFactory)
                HomeScreen(
                    viewModel = homeViewModel,
                    userName = currentUser?.username.orEmpty(),
                    onSearchClick = { navController.navigateToTab(Screen.Search.route) },
                    onSurpriseMeClick = { navController.navigate(Screen.SurpriseMe.route) },
                    onGameClick = { gameId -> navController.navigate(Screen.GameDetails.createRoute(gameId)) }
                )
            }

            composable(Screen.Search.route) {
                val searchViewModel: SearchViewModel = viewModel(factory = viewModelFactory)
                SearchScreen(
                    viewModel = searchViewModel,
                    onGameClick = { gameId -> navController.navigate(Screen.GameDetails.createRoute(gameId)) }
                )
            }

            composable(Screen.Library.route) {
                val libraryViewModel: LibraryViewModel = viewModel(factory = viewModelFactory)
                LibraryScreen(
                    viewModel = libraryViewModel,
                    onGameClick = { gameId -> navController.navigate(Screen.GameDetails.createRoute(gameId)) }
                )
            }

            composable(Screen.Wishlist.route) {
                val wishlistViewModel: WishlistViewModel = viewModel(factory = viewModelFactory)
                WishlistScreen(
                    viewModel = wishlistViewModel,
                    onGameClick = { gameId -> navController.navigate(Screen.GameDetails.createRoute(gameId)) }
                )
            }

            composable(Screen.Profile.route) {
                ProfileScreen(
                    onOpenSettings = { navController.navigate(Screen.Settings.route) },
                    onSignOut = { sessionViewModel.signOut() }
                )
            }

            composable(Screen.Settings.route) {
                val settingsViewModel: SettingsViewModel = viewModel(factory = viewModelFactory)
                SettingsScreen(viewModel = settingsViewModel, onBack = { navController.popBackStack() })
            }

            composable(
                route = Screen.GameDetails.route,
                arguments = listOf(navArgument("gameId") { type = NavType.IntType })
            ) { entry ->
                val gameId = entry.arguments?.getInt("gameId") ?: return@composable
                val detailsViewModel: GameDetailsViewModel = viewModel(
                    factory = GameDetailsViewModel.factory(gameId, container.gameRepository, container.libraryRepository)
                )
                GameDetailsScreen(
                    viewModel = detailsViewModel, 
                    onBack = { navController.popBackStack() },
                    onCompareClick = { navController.navigate(Screen.Compare.createRoute(gameId)) }
                )
            }

            composable(Screen.SurpriseMe.route) {
                val surpriseViewModel: SurpriseMeViewModel = viewModel(factory = viewModelFactory)
                SurpriseMeScreen(
                    viewModel = surpriseViewModel,
                    onBack = { navController.popBackStack() },
                    onViewDetails = { gameId -> navController.navigate(Screen.GameDetails.createRoute(gameId)) }
                )
            }

            composable(
                route = Screen.Compare.route,
                arguments = listOf(navArgument("firstGameId") { type = NavType.IntType })
            ) { entry ->
                val firstGameId = entry.arguments?.getInt("firstGameId") ?: return@composable
                val compareViewModel: CompareViewModel = viewModel(
                    factory = CompareViewModel.factory(firstGameId, container.gameRepository)
                )
                CompareScreen(viewModel = compareViewModel, onBack = { navController.popBackStack() })
            }
        }
    }
}

@Composable
private fun LootBottomBar(currentRoute: String?, onTabSelected: (String) -> Unit) {
    val colors = MaterialTheme.colorScheme
    NavigationBar(containerColor = colors.surface) {
        BottomTab.entries.forEach { tab ->
            NavigationBarItem(
                selected = currentRoute == tab.route,
                onClick = { onTabSelected(tab.route) },
                icon = { Icon(imageVector = tab.icon, contentDescription = null) },
                label = { Text(text = stringResource(tab.labelRes), maxLines = 1, overflow = TextOverflow.Ellipsis) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = colors.primary,
                    selectedTextColor = colors.primary,
                    indicatorColor = Color.Transparent,
                    unselectedIconColor = colors.onSurfaceVariant,
                    unselectedTextColor = colors.onSurfaceVariant
                )
            )
        }
    }
}

private fun NavHostController.navigateToTab(route: String) {
    navigate(route) {
        popUpTo(Screen.Home.route) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

private fun NavHostController.navigateToHomeAfterAuth() {
    navigate(Screen.Home.route) { popUpTo(Screen.Login.route) { inclusive = true } }
}
