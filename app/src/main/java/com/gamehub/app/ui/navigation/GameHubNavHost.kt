package com.gamehub.app.ui.navigation

import android.util.Log
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.gamehub.app.ui.auth.LoginScreen
import com.gamehub.app.ui.auth.RegisterScreen
import com.gamehub.app.ui.home.HomeScreen
import com.gamehub.app.ui.library.LibraryScreen
import com.gamehub.app.ui.onboarding.OnboardingScreen
import com.gamehub.app.ui.profile.ProfileScreen
import com.gamehub.app.ui.search.SearchScreen
import com.gamehub.app.ui.settings.SettingsScreen
import com.gamehub.app.ui.splash.SplashScreen
import com.gamehub.app.ui.wishlist.WishlistScreen

private const val TAG = "GameHubNav"

/**
 * Root composable that owns the NavController, the bottom bar and every route.
 *
 * Pattern adapted from Android Developers (2026) "Navigation with Compose" and
 * "Bottom navigation bar" guidance:
 * https://developer.android.com/develop/ui/compose/navigation
 */
@Composable
fun GameHubNavHost(navController: NavHostController = rememberNavController()) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = currentRoute in bottomBarRoutes

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (showBottomBar) {
                GameHubBottomBar(
                    currentRoute = currentRoute,
                    onTabSelected = { route ->
                        Log.d(TAG, "Tab selected: $route")
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
                // The bottom bar already handles the navigation-bar inset when it is visible.
                .then(if (showBottomBar) Modifier else Modifier.navigationBarsPadding())
        ) {
            composable(Screen.Splash.route) {
                SplashScreen(
                    onFinished = {
                        // TODO Phase 2: if a saved token exists, go straight to Home.
                        navController.navigate(Screen.Onboarding.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Onboarding.route) {
                OnboardingScreen(
                    onFinished = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Login.route) {
                LoginScreen(
                    // TODO Phase 2: call the REST API; only navigate when login succeeds.
                    onLoginSuccess = { navController.navigateToHomeAfterAuth() },
                    onNavigateToRegister = { navController.navigate(Screen.Register.route) }
                )
            }

            composable(Screen.Register.route) {
                RegisterScreen(
                    // TODO Phase 2: call the REST API; only navigate when registration succeeds.
                    onRegistered = { navController.navigateToHomeAfterAuth() },
                    onNavigateToLogin = { navController.popBackStack() }
                )
            }

            composable(Screen.Home.route) {
                HomeScreen(
                    onSearchClick = { navController.navigateToTab(Screen.Search.route) },
                    onSurpriseMeClick = { Log.d(TAG, "Surprise Me tapped (built in Phase 3)") }
                )
            }
            composable(Screen.Search.route) { SearchScreen() }
            composable(Screen.Library.route) { LibraryScreen() }
            composable(Screen.Wishlist.route) { WishlistScreen() }

            composable(Screen.Profile.route) {
                ProfileScreen(
                    onOpenSettings = { navController.navigate(Screen.Settings.route) },
                    onSignOut = {
                        Log.d(TAG, "Sign out tapped")
                        // TODO Phase 2: clear the saved token before leaving.
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}

@Composable
private fun GameHubBottomBar(
    currentRoute: String?,
    onTabSelected: (String) -> Unit
) {
    val colors = MaterialTheme.colorScheme
    NavigationBar(containerColor = colors.surface) {
        BottomTab.entries.forEach { tab ->
            NavigationBarItem(
                selected = currentRoute == tab.route,
                onClick = { onTabSelected(tab.route) },
                icon = { Icon(imageVector = tab.icon, contentDescription = null) },
                label = {
                    Text(
                        text = stringResource(tab.labelRes),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
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

/** Switch between bottom-bar tabs while keeping each tab's state. */
private fun NavHostController.navigateToTab(route: String) {
    navigate(route) {
        popUpTo(Screen.Home.route) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

/** After login/registration, make Home the root so Back leaves the app instead of returning to Login. */
private fun NavHostController.navigateToHomeAfterAuth() {
    navigate(Screen.Home.route) {
        popUpTo(Screen.Login.route) { inclusive = true }
    }
}