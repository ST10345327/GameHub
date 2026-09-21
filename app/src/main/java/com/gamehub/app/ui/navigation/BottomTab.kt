package com.gamehub.app.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VideogameAsset
import androidx.compose.ui.graphics.vector.ImageVector
import com.gamehub.app.R

/** The five tabs from the design: Discover, Search, Library, Wishlist, Profile. */
enum class BottomTab(
    val route: String,
    @StringRes val labelRes: Int,
    val icon: ImageVector
) {
    Discover(Screen.Home.route, R.string.nav_discover, Icons.Filled.Home),
    Search(Screen.Search.route, R.string.nav_search, Icons.Filled.Search),
    Library(Screen.Library.route, R.string.nav_library, Icons.Filled.VideogameAsset),
    Wishlist(Screen.Wishlist.route, R.string.nav_wishlist, Icons.Filled.Bookmark),
    Profile(Screen.Profile.route, R.string.nav_profile, Icons.Filled.Person)
}