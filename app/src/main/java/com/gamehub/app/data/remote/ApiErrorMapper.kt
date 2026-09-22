package com.gamehub.app.data.remote

import com.gamehub.app.data.remote.dto.ErrorResponse
import com.google.gson.Gson
import com.google.gson.JsonParseException

/**
 * Turns an HTTP error answer from our API into an [ApiError].
 * The backend always sends `{ "error": { "code": "..." } }`, and the code is what we
 * switch on, so the wording of the server's message never matters to the app.
 */
object ApiErrorMapper {

    private val gson = Gson()

    fun fromHttp(statusCode: Int, body: String?): ApiError {
        val error = parseBody(body)?.error
        return when (error?.code) {
            "INVALID_CREDENTIALS" -> ApiError.InvalidCredentials
            "EMAIL_TAKEN" -> ApiError.EmailTaken
            "USERNAME_TAKEN" -> ApiError.UsernameTaken
            "WRONG_PASSWORD" -> ApiError.WrongPassword
            "UNAUTHORIZED", "TOKEN_EXPIRED" -> ApiError.SessionExpired
            "NOT_FOUND" -> ApiError.NotFound
            "RATE_LIMITED" -> ApiError.RateLimited
            "UPSTREAM_ERROR", "UPSTREAM_NOT_CONFIGURED" -> ApiError.GameServiceUnavailable
            "VALIDATION_ERROR" -> ApiError.Validation(error?.message)
            else -> fromStatus(statusCode, error?.message)
        }
    }

    /** Fallback when the body is missing or has an unknown code (e.g. an error page from a proxy). */
    private fun fromStatus(statusCode: Int, message: String?): ApiError = when {
        statusCode == 429 -> ApiError.RateLimited
        statusCode == 404 -> ApiError.NotFound
        statusCode >= 500 -> ApiError.Server(message)
        else -> ApiError.Unknown
    }

    private fun parseBody(body: String?): ErrorResponse? {
        if (body.isNullOrBlank()) return null
        return try {
            gson.fromJson(body, ErrorResponse::class.java)
        } catch (e: JsonParseException) {
            null // not JSON (for example an HTML error page)
        }
    }
}