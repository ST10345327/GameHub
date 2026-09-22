package com.gamehub.app.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Covers the sign-up rules from Part 1: 8+ characters, includes a number. */
class ValidatorsTest {

    @Test
    fun email_acceptsNormalAddresses_andTrimsSpaces() {
        assertNull(Validators.validateEmail("player@example.com"))
        assertNull(Validators.validateEmail("  player@example.co.za  "))
    }

    @Test
    fun email_rejectsBlankAndMalformed() {
        assertEquals(FieldError.REQUIRED, Validators.validateEmail("   "))
        assertEquals(FieldError.INVALID_EMAIL, Validators.validateEmail("player"))
        assertEquals(FieldError.INVALID_EMAIL, Validators.validateEmail("player@"))
        assertEquals(FieldError.INVALID_EMAIL, Validators.validateEmail("a b@example.com"))
    }

    @Test
    fun username_enforcesLengthAndCharacters() {
        assertNull(Validators.validateUsername("Gamer_01"))
        assertEquals(FieldError.REQUIRED, Validators.validateUsername(""))
        assertEquals(FieldError.USERNAME_LENGTH, Validators.validateUsername("ab"))
        assertEquals(FieldError.USERNAME_LENGTH, Validators.validateUsername("a".repeat(31)))
        assertEquals(FieldError.USERNAME_CHARACTERS, Validators.validateUsername("bad name!"))
    }

    @Test
    fun newPassword_needsEightCharactersAndANumber() {
        assertNull(Validators.validateNewPassword("abcdefg1"))
        assertEquals(FieldError.REQUIRED, Validators.validateNewPassword(""))
        assertEquals(FieldError.PASSWORD_TOO_SHORT, Validators.validateNewPassword("abc123"))
        assertEquals(FieldError.PASSWORD_NEEDS_NUMBER, Validators.validateNewPassword("abcdefghij"))
        assertEquals(FieldError.PASSWORD_TOO_LONG, Validators.validateNewPassword("a1".repeat(40)))
    }

    @Test
    fun existingPassword_onlyRequiresSomething() {
        assertNull(Validators.validateExistingPassword("x"))
        assertEquals(FieldError.REQUIRED, Validators.validateExistingPassword(""))
    }

    @Test
    fun confirmation_mustMatch() {
        assertNull(Validators.validatePasswordConfirmation("abcdefg1", "abcdefg1"))
        assertEquals(FieldError.PASSWORDS_DO_NOT_MATCH, Validators.validatePasswordConfirmation("abcdefg1", "abcdefg2"))
        assertEquals(FieldError.REQUIRED, Validators.validatePasswordConfirmation("abcdefg1", ""))
    }
}