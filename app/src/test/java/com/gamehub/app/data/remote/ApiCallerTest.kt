package com.gamehub.app.data.remote

import java.io.IOException
import java.net.SocketTimeoutException
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

/** Checks that every kind of failure becomes an [ApiResult] instead of crashing the app. */
class ApiCallerTest {

    private var sessionExpiredCalls = 0
    private val caller = ApiCaller(onSessionExpired = { sessionExpiredCalls++ })

    private fun httpError(code: Int, body: String) = HttpException(
        Response.error<Any>(code, body.toResponseBody("application/json".toMediaType()))
    )

    @Test
    fun success_isWrapped() = runTest {
        assertEquals(ApiResult.Success(42), caller.call { 42 })
    }

    @Test
    fun expiredToken_clearsTheSession() = runTest {
        val result = caller.call<Unit> {
            throw httpError(401, """{"error":{"code":"TOKEN_EXPIRED","message":"x"}}""")
        }

        assertEquals(ApiResult.Failure(ApiError.SessionExpired), result)
        assertEquals(1, sessionExpiredCalls)
    }

    @Test
    fun wrongPassword_doesNotClearTheSession() = runTest {
        val result = caller.call<Unit> {
            throw httpError(401, """{"error":{"code":"INVALID_CREDENTIALS","message":"x"}}""")
        }

        assertEquals(ApiResult.Failure(ApiError.InvalidCredentials), result)
        assertEquals(0, sessionExpiredCalls)
    }

    @Test
    fun ioProblem_meansNoConnection() = runTest {
        assertEquals(ApiResult.Failure(ApiError.NoConnection), caller.call<Unit> { throw IOException("offline") })
    }

    @Test
    fun timeout_isReportedSeparately() = runTest {
        assertEquals(ApiResult.Failure(ApiError.Timeout), caller.call<Unit> { throw SocketTimeoutException() })
    }

    @Test
    fun anythingUnexpected_becomesUnknown_insteadOfCrashing() = runTest {
        assertEquals(ApiResult.Failure(ApiError.Unknown), caller.call<Unit> { throw IllegalStateException("bug") })
    }
}