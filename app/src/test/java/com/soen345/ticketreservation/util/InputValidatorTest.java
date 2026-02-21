package com.soen345.ticketreservation.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("InputValidator")
class InputValidatorTest {

    @Nested
    @DisplayName("isValidEmail")
    class EmailTests {

        @ParameterizedTest(name = "\"{0}\" is a valid email")
        @ValueSource(strings = {"user@example.com", "user.name+tag@domain.co", "test@concordia.ca"})
        void validEmails(String email) {
            assertTrue(InputValidator.isValidEmail(email));
        }

        @ParameterizedTest(name = "\"{0}\" is NOT a valid email")
        @ValueSource(strings = {"notanemail", "@nodomain.com", "missing@", "a b@test.com", ""})
        void invalidEmails(String email) {
            assertFalse(InputValidator.isValidEmail(email));
        }

        @Test
        @DisplayName("returns false for null")
        void nullEmail_isInvalid() {
            assertFalse(InputValidator.isValidEmail(null));
        }
    }

    @Nested
    @DisplayName("isValidPassword")
    class PasswordTests {

        @Test
        @DisplayName("returns true for 6-character password")
        void sixCharPassword_isValid() {
            assertTrue(InputValidator.isValidPassword("abc123"));
        }

        @Test
        @DisplayName("returns true for long password")
        void longPassword_isValid() {
            assertTrue(InputValidator.isValidPassword("ThisIsAVeryLongPassword!99"));
        }

        @Test
        @DisplayName("returns false for 5-character password")
        void fiveCharPassword_isInvalid() {
            assertFalse(InputValidator.isValidPassword("ab1c2"));
        }

        @Test
        @DisplayName("returns false for empty password")
        void emptyPassword_isInvalid() {
            assertFalse(InputValidator.isValidPassword(""));
        }

        @Test
        @DisplayName("returns false for null")
        void nullPassword_isInvalid() {
            assertFalse(InputValidator.isValidPassword(null));
        }
    }

    @Nested
    @DisplayName("passwordsMatch")
    class PasswordMatchTests {

        @Test
        @DisplayName("returns true when both passwords are identical")
        void identicalPasswords_match() {
            assertTrue(InputValidator.passwordsMatch("secret1", "secret1"));
        }

        @Test
        @DisplayName("returns false when passwords differ")
        void differentPasswords_doNotMatch() {
            assertFalse(InputValidator.passwordsMatch("secret1", "Secret1"));
        }

        @Test
        @DisplayName("returns false when confirmPassword is null")
        void nullConfirm_doesNotMatch() {
            assertFalse(InputValidator.passwordsMatch("secret1", null));
        }

        @Test
        @DisplayName("returns false when both are null")
        void bothNull_doesNotMatch() {
            assertFalse(InputValidator.passwordsMatch(null, null));
        }
    }

    @Nested
    @DisplayName("isValidPhoneNumber")
    class PhoneTests {

        @ParameterizedTest(name = "\"{0}\" is a valid E.164 number")
        @ValueSource(strings = {"+15141234567", "+14385550100", "+447911123456", "+33123456789"})
        void validE164Numbers(String phone) {
            assertTrue(InputValidator.isValidPhoneNumber(phone));
        }

        @ParameterizedTest(name = "\"{0}\" is NOT a valid phone number")
        @ValueSource(strings = {"5141234567", "+1", "abc", "", "+1234567"})
        void invalidPhoneNumbers(String phone) {
            assertFalse(InputValidator.isValidPhoneNumber(phone));
        }

        @Test
        @DisplayName("returns false for null")
        void nullPhone_isInvalid() {
            assertFalse(InputValidator.isValidPhoneNumber(null));
        }
    }

    @Nested
    @DisplayName("isValidName")
    class NameTests {

        @Test
        @DisplayName("returns true for a normal name")
        void normalName_isValid() {
            assertTrue(InputValidator.isValidName("Alice Dupont"));
        }

        @Test
        @DisplayName("returns false for empty string")
        void emptyName_isInvalid() {
            assertFalse(InputValidator.isValidName(""));
        }

        @Test
        @DisplayName("returns false for blank (whitespace only)")
        void blankName_isInvalid() {
            assertFalse(InputValidator.isValidName("   "));
        }

        @Test
        @DisplayName("returns false for null")
        void nullName_isInvalid() {
            assertFalse(InputValidator.isValidName(null));
        }

        @Test
        @DisplayName("returns false for name exceeding 60 characters")
        void tooLongName_isInvalid() {
            assertFalse(InputValidator.isValidName("A".repeat(61)));
        }

        @Test
        @DisplayName("returns true for name exactly 60 characters")
        void maxLengthName_isValid() {
            assertTrue(InputValidator.isValidName("A".repeat(60)));
        }
    }

    @Nested
    @DisplayName("isValidOtp")
    class OtpTests {

        @Test
        @DisplayName("returns true for 6-digit OTP")
        void sixDigitOtp_isValid() {
            assertTrue(InputValidator.isValidOtp("123456"));
        }

        @Test
        @DisplayName("returns false for 5-digit OTP")
        void fiveDigitOtp_isInvalid() {
            assertFalse(InputValidator.isValidOtp("12345"));
        }

        @Test
        @DisplayName("returns false for OTP with letters")
        void alphanumericOtp_isInvalid() {
            assertFalse(InputValidator.isValidOtp("12345a"));
        }

        @Test
        @DisplayName("returns false for empty string")
        void emptyOtp_isInvalid() {
            assertFalse(InputValidator.isValidOtp(""));
        }

        @Test
        @DisplayName("returns false for null")
        void nullOtp_isInvalid() {
            assertFalse(InputValidator.isValidOtp(null));
        }
    }
}
