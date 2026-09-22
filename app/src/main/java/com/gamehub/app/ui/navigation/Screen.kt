package com.gamehub.app.ui.navigation

/**
 * Every destination in the app (Part 1, section 6.1).
 * GameDetails carries a numeric gameId argument, so it has a route template and a builder.
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
    data object GameDetails : Screen("game/{gameId}") {
        fun createRoute(gameId: Int) = "game/$gameId"
    }
}

val allScreens: List<Screen> = listOf(
    Screen.Splash, Screen.Onboarding, Screen.Login, Screen.Register,
    Screen.Home, Screen.Search, Screen.Library, Screen.Wishlist,
    Screen.Profile, Screen.Settings, Screen.GameDetails
)

/** Routes that show the bottom navigation bar. Must match [BottomTab]. */
val bottomBarRoutes: Set<String> = setOf(
    Screen.Home.route, Screen.Search.route, Screen.Library.route, Screen.Wishlist.route, Screen.Profile.route
)

/** Screens a signed-out user may see. If the session ends anywhere else, the app returns to Login. */
val publicRoutes: Set<String> = setOf(
    Screen.Splash.route, Screen.Onboarding.route, Screen.Login.route, Screen.Register.route
)