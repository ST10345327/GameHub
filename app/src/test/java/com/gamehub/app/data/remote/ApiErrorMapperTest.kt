package com.gamehub.app.data.remote

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Checks that the backend's error codes become the right [ApiError]. */
class ApiErrorMapperTest {

    private fun body(code: String) = """{"error":{"code":"$code","message":"m"}}"""

    @Test
    fun knownCodes_mapToTheirErrors() {
        assertEquals(ApiError.InvalidCredentials, ApiErrorMapper.fromHttp(401, body("INVALID_CREDENTIALS")))
        assertEquals(ApiError.EmailTaken, ApiErrorMapper.fromHttp(409, body("EMAIL_TAKEN")))
        assertEquals(ApiError.UsernameTaken, ApiErrorMapper.fromHttp(409, body("USERNAME_TAKEN")))
        assertEquals(ApiError.WrongPassword, ApiErrorMapper.fromHttp(400, body("WRONG_PASSWORD")))
        assertEquals(ApiError.GameServiceUnavailable, ApiErrorMapper.fromHttp(502, body("UPSTREAM_ERROR")))
    }

    @Test
    fun bothTokenProblems_meanTheSessionIsOver() {
        assertEquals(ApiError.SessionExpired, ApiErrorMapper.fromHttp(401, body("TOKEN_EXPIRED")))
        assertEquals(ApiError.SessionExpired, ApiErrorMapper.fromHttp(401, body("UNAUTHORIZED")))
    }

    @Test
    fun validationErrors_keepTheServerMessage() {
        val error = ApiErrorMapper.fromHttp(400, """{"error":{"code":"VALIDATION_ERROR","message":"Bad input"}}""")
        assertEquals(ApiError.Validation("Bad input"), error)
    }

    @Test
    fun missingOrBrokenBody_fallsBackToTheStatusCode() {
        assertEquals(ApiError.RateLimited, ApiErrorMapper.fromHttp(429, null))
        assertEquals(ApiError.NotFound, ApiErrorMapper.fromHttp(404, ""))
        assertTrue(ApiErrorMapper.fromHttp(503, "<html>Bad gateway</html>") is ApiError.Server)
        assertEquals(ApiError.Unknown, ApiErrorMapper.fromHttp(418, "not json"))
    }
}