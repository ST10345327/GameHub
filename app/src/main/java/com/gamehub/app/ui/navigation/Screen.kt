package com.gamehub.app.ui.navigation

/**
 * Every destination in the app, following the navigation structure in Part 1 (section 6.1):
 * Splash -> Onboarding -> Login/Register -> Home (five bottom-bar tabs) -> Settings.
 *
 * Kept free of Android/Compose types so it can be unit tested on the JVM.
 * Later phases add routes such as "game/{gameId}" and "compare".
 */
sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Onboarding : Screen("onboarding")
    data object Login : Screen("login")
    data object Register : Screen("register")
    data object Home : Screen("home")
    data object Search : Screen("search")
    data object Library : Screen("library")
    data object Wishlist : Screen("wishlist")
    data object Profile : Screen("profile")
    data object Settings : Screen("settings")
}

val allScreens: List<Screen> = listOf(
    Screen.Splash, Screen.Onboarding, Screen.Login, Screen.Register,
    Screen.Home, Screen.Search, Screen.Library, Screen.Wishlist,
    Screen.Profile, Screen.Settings
)

/** Routes that show the bottom navigation bar. Must match [BottomTab]. */
val bottomBarRoutes: Set<String> = setOf(
    Screen.Home.route,
    Screen.Search.route,
    Screen.Library.route,
    Screen.Wishlist.route,
    Screen.Profile.route
)