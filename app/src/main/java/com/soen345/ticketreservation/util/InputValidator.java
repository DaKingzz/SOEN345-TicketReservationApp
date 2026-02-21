package com.soen345.ticketreservation.util;

import java.util.regex.Pattern;

public final class InputValidator {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$"
    );

    private InputValidator() {}

    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) return false;
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }

    public static boolean passwordsMatch(String password, String confirmPassword) {
        if (password == null || confirmPassword == null) return false;
        return password.equals(confirmPassword);
    }

    /** E.164 format required, e.g. +15141234567 */
    public static boolean isValidPhoneNumber(String phone) {
        if (phone == null || phone.trim().isEmpty()) return false;
        return phone.trim().matches("\\+[0-9]{8,15}");
    }

    public static boolean isValidName(String name) {
        if (name == null) return false;
        String trimmed = name.trim();
        return !trimmed.isEmpty() && trimmed.length() <= 60;
    }

    public static boolean isValidOtp(String otp) {
        if (otp == null) return false;
        return otp.matches("[0-9]{6}");
    }
}
