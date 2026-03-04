package com.soen345.ticketreservation.util;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import com.soen345.ticketreservation.R;

public class AuthTestUtils {

    /**
     * Reusable login method for Espresso tests.
     *
     * @param email    The email to login with.
     * @param password The password to login with.
     */
    public static void login(String email, String password) {
        onView(withId(R.id.email_edit_text))
                .perform(replaceText(email), closeSoftKeyboard());

        onView(withId(R.id.password_edit_text))
                .perform(replaceText(password), closeSoftKeyboard());

        onView(withId(R.id.login_button))
                .perform(click());
    }
}
