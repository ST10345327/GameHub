package com.gamehub.app.ui.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Guards the navigation table so a typo or duplicate route is caught by CI, not on a phone. */
class ScreenRoutesTest {

    @Test
    fun allRoutes_areUnique() {
        val routes = allScreens.map { it.route }
        assertEquals(routes.size, routes.toSet().size)
    }

    @Test
    fun allRoutes_areNotBlank() {
        assertTrue(allScreens.all { it.route.isNotBlank() })
    }

    @Test
    fun bottomBar_hasFiveTabs() {
        assertEquals(5, bottomBarRoutes.size)
    }

    @Test
    fun bottomBar_routesAllExist() {
        val known = allScreens.map { it.route }.toSet()
        assertTrue(known.containsAll(bottomBarRoutes))
    }

    @Test
    fun authScreens_neverShowBottomBar() {
        val authScreens = listOf(Screen.Splash, Screen.Onboarding, Screen.Login, Screen.Register)
        authScreens.forEach { screen ->
            assertFalse("${screen.route} must hide the bottom bar", screen.route in bottomBarRoutes)
        }
    }

    @Test
    fun settings_isNotATab() {
        assertFalse(Screen.Settings.route in bottomBarRoutes)
    }

    @Test
    fun publicRoutes_areNeverBottomBarTabs() {
        assertTrue(publicRoutes.none { it in bottomBarRoutes })
        assertTrue(Screen.Home.route !in publicRoutes)
    }
}