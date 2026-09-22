package com.gamehub.app.ui.splash

import androidx.lifecycle.ViewModel
import com.gamehub.app.data.repository.AuthRepository
import com.gamehub.app.ui.navigation.Screen
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first

class SplashViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    suspend fun resolveStartRoute(): String {
        return combine(
            authRepository.onboardingSeen,
            authRepository.currentUser
        ) { onboardingSeen, currentUser ->
            when {
                !onboardingSeen -> Screen.Onboarding.route
                currentUser != null -> Screen.Home.route
                else -> Screen.Login.route
            }
        }.first()
    }
}