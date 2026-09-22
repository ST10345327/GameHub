package com.gamehub.app.data.remote

import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Adds "Authorization: Bearer <token>" to every request while the user is signed in, so
 * individual API calls never have to pass the token around.
 *
 * OkHttp calls interceptors on its own background threads (never the main thread), so
 * blocking briefly to read the saved token from DataStore is safe here.
 */
class AuthInterceptor(private val tokenProvider: suspend () -> String?) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking { tokenProvider() }
        val request = if (token.isNullOrBlank()) {
            chain.request()
        } else {
            chain.request().newBuilder().header("Authorization", "Bearer $token").build()
        }
        return chain.proceed(request)
    }
}