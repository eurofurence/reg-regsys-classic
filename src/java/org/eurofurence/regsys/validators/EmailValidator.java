package org.eurofurence.regsys.validators;

/**
 * Validation of an email address
 *
 * @author Jumpy <jumpy@furry.de>
 */
public class EmailValidator {
    private static final String EMAIL_REGEX = "[^\\@\\s]+\\@[^\\@\\s]+";

    public static boolean isValidEmailAddress(String input) {
        if (input == null) {
            input = "";
        }
        return input.matches(EMAIL_REGEX);
    }
}
