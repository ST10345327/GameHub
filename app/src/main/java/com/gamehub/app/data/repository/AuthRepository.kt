package com.gamehub.app.data.repository

import com.gamehub.app.data.local.SessionStore
import com.gamehub.app.data.model.User
import com.gamehub.app.data.remote.ApiCaller
import com.gamehub.app.data.remote.ApiError
import com.gamehub.app.data.remote.ApiResult
import com.gamehub.app.data.remote.GameHubApi
import com.gamehub.app.data.remote.dto.AuthResponse
import com.gamehub.app.data.remote.dto.ForgotPasswordRequest
import com.gamehub.app.data.remote.dto.LoginRequest
import com.gamehub.app.data.remote.dto.RegisterRequest
import com.gamehub.app.data.remote.dto.UserDto
import com.gamehub.app.data.remote.map
import com.gamehub.app.utils.AppLogger
import kotlinx.coroutines.flow.Flow

/**
 * Everything the UI needs for accounts. It is an interface so unit tests can swap in a fake
 * and test ViewModels without a network.
 */
interface AuthRepository {
    /** The signed-in user, or null when signed out. */
    val currentUser: Flow<User?>

    /** Whether the first-run onboarding has been shown already. */
    val onboardingSeen: Flow<Boolean>

    suspend fun register(username: String, email: String, password: String): ApiResult<User>
    suspend fun login(email: String, password: String): ApiResult<User>
    suspend fun forgotPassword(email: String): ApiResult<Unit>
    suspend fun signOut()
    suspend fun markOnboardingSeen()
}

class AuthRepositoryImpl(
    private val api: GameHubApi,
    private val apiCaller: ApiCaller,
    private val sessionStore: SessionStore
) : AuthRepository {

    override val currentUser: Flow<User?> = sessionStore.user
    override val onboardingSeen: Flow<Boolean> = sessionStore.onboardingSeen

    override suspend fun register(username: String, email: String, password: String): ApiResult<User> =
        authenticate { api.register(RegisterRequest(username.trim(), email.trim(), password)) }

    override suspend fun login(email: String, password: String): ApiResult<User> =
        authenticate { api.login(LoginRequest(email.trim(), password)) }

    override suspend fun forgotPassword(email: String): ApiResult<Unit> =
        apiCaller.call { api.forgotPassword(ForgotPasswordRequest(email.trim())) }.map { }

    override suspend fun signOut() {
        AppLogger.debug("Signing out")
        sessionStore.clear()
    }

    override suspend fun markOnboardingSeen() = sessionStore.markOnboardingSeen()

    /**
     * Shared by register and login: performs the call and, on success, saves the token so the
     * user stays signed in. The password is never logged or stored; only the token is kept.
     */
    private suspend fun authenticate(request: suspend () -> AuthResponse): ApiResult<User> {
        return when (val result = apiCaller.call(request)) {
            is ApiResult.Failure -> result
            is ApiResult.Success -> {
                val token = result.data.token
                val user = result.data.user?.toUser()
                if (token.isNullOrBlank() || user == null) {
                    AppLogger.error("Auth response was missing a token or a user")
                    ApiResult.Failure(ApiError.Unknown)
                } else {
                    sessionStore.save(token, user)
                    AppLogger.debug("Signed in as ${user.username}")
                    ApiResult.Success(user)
                }
            }
        }
    }

    /** Only builds a User when every field is present. */
    private fun UserDto.toUser(): User? =
        if (id != null && username != null && email != null) User(id, username, email) else null
}