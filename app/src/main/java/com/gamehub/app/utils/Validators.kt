package com.gamehub.app.utils

/** Reasons a form field can be rejected. The UI maps each one to a translated message. */
enum class FieldError {
    REQUIRED,
    INVALID_EMAIL,
    USERNAME_LENGTH,
    USERNAME_CHARACTERS,
    PASSWORD_TOO_SHORT,
    PASSWORD_TOO_LONG,
    PASSWORD_NEEDS_NUMBER,
    PASSWORDS_DO_NOT_MATCH,
    EMAIL_TAKEN,      // set by a ViewModel when the server reports a duplicate email
    USERNAME_TAKEN    // set by a ViewModel when the server reports a duplicate username
}

/**
 * Client-side input validation (Part 1: "8+ characters, includes number").
 * The server enforces the same rules again, so these checks exist for fast, friendly
 * feedback only and are never the sole protection. Pure functions: easy to unit test.
 */
object Validators {
    const val MIN_PASSWORD_LENGTH = 8
    const val MAX_PASSWORD_LENGTH = 72 // bcrypt ignores everything after 72 bytes
    const val MIN_USERNAME_LENGTH = 3
    const val MAX_USERNAME_LENGTH = 30

    private val EMAIL_REGEX = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")
    private val USERNAME_REGEX = Regex("^[A-Za-z0-9_]+$")

    fun validateEmail(email: String): FieldError? {
        val trimmed = email.trim()
        return when {
            trimmed.isEmpty() -> FieldError.REQUIRED
            trimmed.length > 100 || !EMAIL_REGEX.matches(trimmed) -> FieldError.INVALID_EMAIL
            else -> null
        }
    }

    fun validateUsername(username: String): FieldError? {
        val trimmed = username.trim()
        return when {
            trimmed.isEmpty() -> FieldError.REQUIRED
            trimmed.length !in MIN_USERNAME_LENGTH..MAX_USERNAME_LENGTH -> FieldError.USERNAME_LENGTH
            !USERNAME_REGEX.matches(trimmed) -> FieldError.USERNAME_CHARACTERS
            else -> null
        }
    }

    /** Full rules, used when creating or changing a password. */
    fun validateNewPassword(password: String): FieldError? = when {
        password.isEmpty() -> FieldError.REQUIRED
        password.length < MIN_PASSWORD_LENGTH -> FieldError.PASSWORD_TOO_SHORT
        password.length > MAX_PASSWORD_LENGTH -> FieldError.PASSWORD_TOO_LONG
        password.none { it.isDigit() } -> FieldError.PASSWORD_NEEDS_NUMBER
        else -> null
    }

    /** When signing in only presence is checked; the server decides whether it is correct. */
    fun validateExistingPassword(password: String): FieldError? =
        if (password.isEmpty()) FieldError.REQUIRED else null

    fun validatePasswordConfirmation(password: String, confirmation: String): FieldError? = when {
        confirmation.isEmpty() -> FieldError.REQUIRED
        confirmation != password -> FieldError.PASSWORDS_DO_NOT_MATCH
        else -> null
    }
}