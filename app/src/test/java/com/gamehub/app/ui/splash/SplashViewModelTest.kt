package com.gamehub.app.ui.splash

import com.gamehub.app.testing.FakeAuthRepository
import com.gamehub.app.ui.navigation.Screen
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test


class SplashViewModelTest {

    private val repository = FakeAuthRepository()

    @Test
    fun signedInUser_goesStraightToHome() = runTest {
        repository.setSignedIn(FakeAuthRepository.TEST_USER)

        assertEquals(Screen.Home.route, SplashViewModel(repository).resolveStartRoute())
    }

    @Test
    fun firstRun_showsOnboarding() = runTest {
        assertEquals(Screen.Onboarding.route, SplashViewModel(repository).resolveStartRoute())
    }

    @Test
    fun returningSignedOutUser_goesToLogin() = runTest {
        repository.setOnboardingSeen(true)

        assertEquals(Screen.Login.route, SplashViewModel(repository).resolveStartRoute())
    }
}